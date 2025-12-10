

// 後台管理員重置使用者密碼 API
// POST /admin/api/users/:userId/reset-password

import crypto from 'crypto'
import nodemailer from 'nodemailer'
import bcrypt from 'bcrypt'

// 1. 管理員發起密碼重置
export async function adminResetUserPassword(req, res) {
  const { userId } = req.params
  const { resetType, notifyUser } = req.body // resetType: 'temp_password' 或 'reset_link'
  const adminId = req.admin.id

  try {
    // 檢查使用者是否存在
    const user = await db.query('SELECT * FROM users WHERE id = ?', [userId])
    if (!user) {
      return res.status(404).json({ error: '使用者不存在' })
    }

    if (resetType === 'temp_password') {
      // 生成臨時密碼
      const tempPassword = generateTempPassword()
      const hashedPassword = await bcrypt.hash(tempPassword, 10)

      // 更新使用者密碼並強制下次登入修改
      await db.query(
        'UPDATE users SET password = ?, force_password_change = TRUE, password_changed_at = NOW() WHERE id = ?',
        [hashedPassword, userId]
      )

      // 記錄操作日誌
      await db.query(
        'INSERT INTO system_logs (admin_user_id, user_id, action, module, description, ip_address) VALUES (?, ?, ?, ?, ?, ?)',
        [adminId, userId, 'reset_password', 'user_management', `管理員重置使用者 ${user.email} 的密碼`, req.ip]
      )

      // 發送臨時密碼郵件
      if (notifyUser) {
        await sendTempPasswordEmail(user.email, user.name, tempPassword)
      }

      return res.json({
        success: true,
        message: '密碼已重置',
        tempPassword: notifyUser ? '已寄送至使用者信箱' : tempPassword
      })

    } else if (resetType === 'reset_link') {
      // 生成重置連結
      const resetToken = crypto.randomBytes(32).toString('hex')
      const expiresAt = new Date(Date.now() + 60 * 60 * 1000) // 1小時後過期

      // 儲存重置記錄
      await db.query(
        'INSERT INTO password_resets (user_id, email, token, reset_by_admin_id, type, expires_at) VALUES (?, ?, ?, ?, ?, ?)',
        [userId, user.email, resetToken, adminId, 'admin_reset', expiresAt]
      )

      // 記錄操作日誌
      await db.query(
        'INSERT INTO system_logs (admin_user_id, user_id, action, module, description, ip_address) VALUES (?, ?, ?, ?, ?, ?)',
        [adminId, userId, 'send_reset_link', 'user_management', `管理員發送密碼重置連結給 ${user.email}`, req.ip]
      )

      // 發送重置連結郵件
      if (notifyUser) {
        const resetUrl = `${process.env.FRONTEND_URL}/reset-password?token=${resetToken}`
        await sendResetLinkEmail(user.email, user.name, resetUrl)
      }

      return res.json({
        success: true,
        message: '重置連結已產生',
        resetUrl: notifyUser ? '已寄送至使用者信箱' : `${process.env.FRONTEND_URL}/reset-password?token=${resetToken}`
      })
    }

  } catch (error) {
    console.error('重置密碼失敗:', error)
    return res.status(500).json({ error: '重置密碼失敗' })
  }
}

// 2. 批次重置密碼
export async function batchResetPasswords(req, res) {
  const { userIds, resetType } = req.body
  const adminId = req.admin.id

  try {
    const results = []

    for (const userId of userIds) {
      try {
        const user = await db.query('SELECT * FROM users WHERE id = ?', [userId])
        if (!user) continue

        if (resetType === 'temp_password') {
          const tempPassword = generateTempPassword()
          const hashedPassword = await bcrypt.hash(tempPassword, 10)

          await db.query(
            'UPDATE users SET password = ?, force_password_change = TRUE WHERE id = ?',
            [hashedPassword, userId]
          )

          await sendTempPasswordEmail(user.email, user.name, tempPassword)
        } else {
          const resetToken = crypto.randomBytes(32).toString('hex')
          const expiresAt = new Date(Date.now() + 60 * 60 * 1000)

          await db.query(
            'INSERT INTO password_resets (user_id, email, token, reset_by_admin_id, type, expires_at) VALUES (?, ?, ?, ?, ?, ?)',
            [userId, user.email, resetToken, adminId, 'admin_reset', expiresAt]
          )

          const resetUrl = `${process.env.FRONTEND_URL}/reset-password?token=${resetToken}`
          await sendResetLinkEmail(user.email, user.name, resetUrl)
        }

        results.push({ userId, email: user.email, success: true })
      } catch (error) {
        results.push({ userId, success: false, error: error.message })
      }
    }

    return res.json({
      success: true,
      message: `已處理 ${results.length} 位使用者`,
      results
    })

  } catch (error) {
    console.error('批次重置失敗:', error)
    return res.status(500).json({ error: '批次重置失敗' })
  }
}

// 3. 查詢密碼重置記錄
export async function getPasswordResetLogs(req, res) {
  const { userId, page = 1, limit = 20 } = req.query
  const offset = (page - 1) * limit

  try {
    let query = `
      SELECT
        pr.*,
        u.email as user_email,
        u.name as user_name,
        au.username as admin_username
      FROM password_resets pr
      LEFT JOIN users u ON pr.user_id = u.id
      LEFT JOIN admin_users au ON pr.reset_by_admin_id = au.id
    `
    const params = []

    if (userId) {
      query += ' WHERE pr.user_id = ?'
      params.push(userId)
    }

    query += ' ORDER BY pr.created_at DESC LIMIT ? OFFSET ?'
    params.push(parseInt(limit), parseInt(offset))

    const logs = await db.query(query, params)
    const totalQuery = userId
      ? 'SELECT COUNT(*) as total FROM password_resets WHERE user_id = ?'
      : 'SELECT COUNT(*) as total FROM password_resets'
    const [{ total }] = await db.query(totalQuery, userId ? [userId] : [])

    return res.json({
      logs,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        totalPages: Math.ceil(total / limit)
      }
    })

  } catch (error) {
    console.error('查詢失敗:', error)
    return res.status(500).json({ error: '查詢失敗' })
  }
}

// 輔助函數
function generateTempPassword() {
  // 生成 8 位包含大小寫字母和數字的臨時密碼
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789'
  let password = ''
  for (let i = 0; i < 8; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return password
}

async function sendTempPasswordEmail(email, name, tempPassword) {
  const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: process.env.SMTP_PORT,
    secure: true,
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS
    }
  })

  const mailOptions = {
    from: process.env.SMTP_FROM,
    to: email,
    subject: 'SubCycle - 您的臨時密碼',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>密碼重置通知</h2>
        <p>親愛的 ${name}，</p>
        <p>您的帳戶密碼已由系統管理員重置。</p>
        <div style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;">
          <p style="margin: 0;"><strong>臨時密碼：</strong></p>
          <p style="font-size: 24px; color: #409EFF; font-family: 'Courier New', monospace; margin: 10px 0;">
            ${tempPassword}
          </p>
        </div>
        <p style="color: #F56C6C;"><strong>重要提醒：</strong></p>
        <ul>
          <li>請在登入後立即修改密碼</li>
          <li>請勿與他人分享此臨時密碼</li>
          <li>如非本人操作，請立即聯繫客服</li>
        </ul>
        <p>如有任何問題，請聯繫我們的客服團隊。</p>
        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
        <p style="color: #999; font-size: 12px;">此郵件由系統自動發送，請勿直接回覆。</p>
      </div>
    `
  }

  await transporter.sendMail(mailOptions)

  // 記錄郵件發送
  await db.query(
    'INSERT INTO email_logs (email_to, email_type, subject, status, sent_at) VALUES (?, ?, ?, ?, NOW())',
    [email, 'password_reset', '您的臨時密碼', 'sent']
  )
}

async function sendResetLinkEmail(email, name, resetUrl) {
  const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: process.env.SMTP_PORT,
    secure: true,
    auth: {
      user: process.env.SMTP_USER,
      pass: process.env.SMTP_PASS
    }
  })

  const mailOptions = {
    from: process.env.SMTP_FROM,
    to: email,
    subject: 'SubCycle - 密碼重置連結',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2>密碼重置請求</h2>
        <p>親愛的 ${name}，</p>
        <p>我們收到了您的密碼重置請求。請點擊下方按鈕重置您的密碼：</p>
        <div style="text-align: center; margin: 30px 0;">
          <a href="${resetUrl}"
             style="background-color: #409EFF; color: white; padding: 12px 30px;
                    text-decoration: none; border-radius: 5px; display: inline-block;">
            重置密碼
          </a>
        </div>
        <p>或複製以下連結至瀏覽器：</p>
        <p style="word-break: break-all; color: #666; font-size: 14px;">
          ${resetUrl}
        </p>
        <p style="color: #F56C6C;"><strong>安全提醒：</strong></p>
        <ul>
          <li>此連結將在 1 小時後失效</li>
          <li>如非本人操作，請忽略此郵件</li>
          <li>請勿與他人分享此連結</li>
        </ul>
        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
        <p style="color: #999; font-size: 12px;">此郵件由系統自動發送，請勿直接回覆。</p>
      </div>
    `
  }

  await transporter.sendMail(mailOptions)

  // 記錄郵件發送
  await db.query(
    'INSERT INTO email_logs (email_to, email_type, subject, status, sent_at) VALUES (?, ?, ?, ?, NOW())',
    [email, 'password_reset', '密碼重置連結', 'sent']
  )
}
