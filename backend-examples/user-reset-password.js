// 前台使用者密碼重置 API
// 使用者自行申請忘記密碼

import crypto from 'crypto'
import nodemailer from 'nodemailer'
import bcrypt from 'bcrypt'

// 1. 使用者申請忘記密碼
// POST /api/auth/forgot-password
export async function forgotPassword(req, res) {
  const { email } = req.body

  try {
    // 檢查使用者是否存在
    const user = await db.query('SELECT * FROM users WHERE email = ?', [email])

    // 即使使用者不存在也返回成功訊息（防止帳號探測）
    if (!user) {
      return res.json({
        success: true,
        message: '如果該電子郵件存在於我們的系統中，您將收到密碼重置連結'
      })
    }

    // 檢查是否在短時間內重複申請
    const recentReset = await db.query(
      'SELECT * FROM password_resets WHERE email = ? AND created_at > DATE_SUB(NOW(), INTERVAL 5 MINUTE) AND used_at IS NULL',
      [email]
    )

    if (recentReset) {
      return res.status(429).json({
        error: '請稍後再試，您剛才已申請過密碼重置'
      })
    }

    // 生成重置 token
    const resetToken = crypto.randomBytes(32).toString('hex')
    const hashedToken = crypto.createHash('sha256').update(resetToken).digest('hex')
    const expiresAt = new Date(Date.now() + 60 * 60 * 1000) // 1小時後過期

    // 儲存重置記錄
    await db.query(
      'INSERT INTO password_resets (user_id, email, token, type, expires_at, ip_address) VALUES (?, ?, ?, ?, ?, ?)',
      [user.id, email, hashedToken, 'user_request', expiresAt, req.ip]
    )

    // 發送重置連結郵件
    const resetUrl = `${process.env.FRONTEND_URL}/reset-password?token=${resetToken}`
    await sendPasswordResetEmail(email, user.name, resetUrl)

    // 記錄郵件發送
    await db.query(
      'INSERT INTO email_logs (user_id, email_to, email_type, subject, status, sent_at) VALUES (?, ?, ?, ?, ?, NOW())',
      [user.id, email, 'password_reset', '密碼重置連結', 'sent']
    )

    return res.json({
      success: true,
      message: '如果該電子郵件存在於我們的系統中，您將收到密碼重置連結'
    })

  } catch (error) {
    console.error('忘記密碼處理失敗:', error)
    return res.status(500).json({ error: '處理失敗，請稍後再試' })
  }
}

// 2. 驗證重置 token
// GET /api/auth/verify-reset-token/:token
export async function verifyResetToken(req, res) {
  const { token } = req.params

  try {
    const hashedToken = crypto.createHash('sha256').update(token).digest('hex')

    // 查詢重置記錄
    const reset = await db.query(
      `SELECT pr.*, u.email, u.name
       FROM password_resets pr
       JOIN users u ON pr.user_id = u.id
       WHERE pr.token = ?
       AND pr.expires_at > NOW()
       AND pr.used_at IS NULL`,
      [hashedToken]
    )

    if (!reset) {
      return res.status(400).json({
        error: '重置連結無效或已過期',
        code: 'INVALID_TOKEN'
      })
    }

    return res.json({
      success: true,
      email: reset.email,
      name: reset.name
    })

  } catch (error) {
    console.error('驗證 token 失敗:', error)
    return res.status(500).json({ error: '驗證失敗' })
  }
}

// 3. 執行密碼重置
// POST /api/auth/reset-password
export async function resetPassword(req, res) {
  const { token, newPassword } = req.body

  // 密碼強度驗證
  if (!isValidPassword(newPassword)) {
    return res.status(400).json({
      error: '密碼必須至少 8 個字元，包含大小寫字母和數字'
    })
  }

  try {
    const hashedToken = crypto.createHash('sha256').update(token).digest('hex')

    // 查詢重置記錄
    const reset = await db.query(
      `SELECT pr.*, u.id as user_id
       FROM password_resets pr
       JOIN users u ON pr.user_id = u.id
       WHERE pr.token = ?
       AND pr.expires_at > NOW()
       AND pr.used_at IS NULL`,
      [hashedToken]
    )

    if (!reset) {
      return res.status(400).json({
        error: '重置連結無效或已過期'
      })
    }

    // 加密新密碼
    const hashedPassword = await bcrypt.hash(newPassword, 10)

    // 更新使用者密碼
    await db.query(
      'UPDATE users SET password = ?, password_changed_at = NOW(), force_password_change = FALSE WHERE id = ?',
      [hashedPassword, reset.user_id]
    )

    // 標記 token 已使用
    await db.query(
      'UPDATE password_resets SET used_at = NOW(), ip_address = ? WHERE id = ?',
      [req.ip, reset.id]
    )

    // 發送密碼變更通知郵件
    await sendPasswordChangedNotification(reset.email, reset.name)

    return res.json({
      success: true,
      message: '密碼已成功重置，請使用新密碼登入'
    })

  } catch (error) {
    console.error('重置密碼失敗:', error)
    return res.status(500).json({ error: '重置失敗，請稍後再試' })
  }
}

// 4. 使用者變更密碼（已登入狀態）
// POST /api/user/change-password
export async function changePassword(req, res) {
  const { currentPassword, newPassword } = req.body
  const userId = req.user.id

  try {
    // 取得使用者資料
    const user = await db.query('SELECT * FROM users WHERE id = ?', [userId])

    if (!user) {
      return res.status(404).json({ error: '使用者不存在' })
    }

    // 驗證目前密碼
    const isValidPassword = await bcrypt.compare(currentPassword, user.password)
    if (!isValidPassword) {
      return res.status(401).json({ error: '目前密碼錯誤' })
    }

    // 檢查新密碼強度
    if (!isValidPassword(newPassword)) {
      return res.status(400).json({
        error: '密碼必須至少 8 個字元，包含大小寫字母和數字'
      })
    }

    // 檢查新密碼是否與舊密碼相同
    const isSamePassword = await bcrypt.compare(newPassword, user.password)
    if (isSamePassword) {
      return res.status(400).json({
        error: '新密碼不能與目前密碼相同'
      })
    }

    // 更新密碼
    const hashedPassword = await bcrypt.hash(newPassword, 10)
    await db.query(
      'UPDATE users SET password = ?, password_changed_at = NOW(), force_password_change = FALSE WHERE id = ?',
      [hashedPassword, userId]
    )

    // 發送密碼變更通知
    await sendPasswordChangedNotification(user.email, user.name)

    return res.json({
      success: true,
      message: '密碼已成功變更'
    })

  } catch (error) {
    console.error('變更密碼失敗:', error)
    return res.status(500).json({ error: '變更失敗，請稍後再試' })
  }
}

// 5. 清理過期的重置記錄（排程任務）
export async function cleanupExpiredResets() {
  try {
    const result = await db.query(
      'DELETE FROM password_resets WHERE expires_at < NOW() OR used_at IS NOT NULL'
    )
    console.log(`已清理 ${result.affectedRows} 筆過期的密碼重置記錄`)
  } catch (error) {
    console.error('清理失敗:', error)
  }
}

// 輔助函數
function isValidPassword(password) {
  // 至少 8 個字元，包含大小寫字母和數字
  const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
  return regex.test(password)
}

async function sendPasswordResetEmail(email, name, resetUrl) {
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
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
        <div style="text-align: center; margin-bottom: 30px;">
          <h1 style="color: #409EFF; margin: 0;">SubCycle</h1>
        </div>

        <h2 style="color: #333;">密碼重置請求</h2>
        <p>親愛的 ${name}，</p>
        <p>我們收到了您的密碼重置請求。請點擊下方按鈕重置您的密碼：</p>

        <div style="text-align: center; margin: 40px 0;">
          <a href="${resetUrl}"
             style="background-color: #409EFF; color: white; padding: 15px 40px;
                    text-decoration: none; border-radius: 5px; display: inline-block;
                    font-weight: bold; font-size: 16px;">
            重置密碼
          </a>
        </div>

        <p>或複製以下連結至瀏覽器：</p>
        <p style="word-break: break-all; background-color: #f5f5f5; padding: 10px;
                  border-radius: 5px; color: #666; font-size: 14px;">
          ${resetUrl}
        </p>

        <div style="background-color: #fff3cd; border-left: 4px solid #F56C6C;
                    padding: 15px; margin: 30px 0;">
          <p style="margin: 0; font-weight: bold; color: #F56C6C;">安全提醒：</p>
          <ul style="margin: 10px 0; padding-left: 20px; color: #856404;">
            <li>此連結將在 <strong>1 小時</strong>後失效</li>
            <li>如非本人操作，請忽略此郵件並確保帳號安全</li>
            <li>請勿與他人分享此連結</li>
          </ul>
        </div>

        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">

        <p style="color: #999; font-size: 12px; text-align: center;">
          此郵件由 SubCycle 系統自動發送，請勿直接回覆<br>
          如有問題請聯繫客服：support@subcycle.com
        </p>
      </div>
    `
  }

  await transporter.sendMail(mailOptions)
}

async function sendPasswordChangedNotification(email, name) {
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
    subject: 'SubCycle - 密碼已變更',
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
        <div style="text-align: center; margin-bottom: 30px;">
          <h1 style="color: #409EFF; margin: 0;">SubCycle</h1>
        </div>

        <h2 style="color: #333;">密碼變更通知</h2>
        <p>親愛的 ${name}，</p>
        <p>您的帳戶密碼已成功變更。</p>

        <div style="background-color: #f0f9ff; border-left: 4px solid #409EFF;
                    padding: 15px; margin: 20px 0;">
          <p style="margin: 0;"><strong>變更時間：</strong>${new Date().toLocaleString('zh-TW', { timeZone: 'Asia/Taipei' })}</p>
        </div>

        <div style="background-color: #fff3cd; border-left: 4px solid #F56C6C;
                    padding: 15px; margin: 30px 0;">
          <p style="margin: 0; font-weight: bold; color: #F56C6C;">如果這不是您本人的操作：</p>
          <ul style="margin: 10px 0; padding-left: 20px; color: #856404;">
            <li>請立即聯繫客服：support@subcycle.com</li>
            <li>建議檢查帳號是否有異常登入記錄</li>
          </ul>
        </div>

        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">

        <p style="color: #999; font-size: 12px; text-align: center;">
          此郵件由 SubCycle 系統自動發送，請勿直接回覆
        </p>
      </div>
    `
  }

  await transporter.sendMail(mailOptions)

  // 記錄郵件發送
  await db.query(
    'INSERT INTO email_logs (email_to, email_type, subject, status, sent_at) VALUES (?, ?, ?, ?, NOW())',
    [email, 'notification', '密碼已變更', 'sent']
  )
}
