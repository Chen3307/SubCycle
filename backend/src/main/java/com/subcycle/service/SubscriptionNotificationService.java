package com.subcycle.service;

import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 訂閱通知服務
 * 自動檢查即將到期的訂閱並發送郵件通知
 */
@Service
@Slf4j
public class SubscriptionNotificationService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EmailService emailService;

    /**
     * 每天上午 9:00 執行，檢查即將到期的訂閱並發送通知
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendUpcomingPaymentNotifications() {
        log.info("開始檢查即將到期的訂閱...");

        LocalDate today = LocalDate.now();

        // 查找所有活躍的訂閱
        List<Subscription> allSubscriptions = subscriptionRepository.findByStatus("active");

        int notificationsSent = 0;

        for (Subscription subscription : allSubscriptions) {
            // 檢查是否需要發送通知
            if (shouldSendNotification(subscription, today)) {
                try {
                    sendPaymentReminderEmail(subscription);

                    // 標記為已發送
                    subscription.setReminderSent(true);
                    subscriptionRepository.save(subscription);

                    notificationsSent++;
                } catch (Exception e) {
                    log.error("發送通知失敗: 訂閱ID={}, 錯誤={}",
                            subscription.getId(), e.getMessage(), e);
                }
            }
        }

        log.info("通知檢查完成，共發送 {} 封郵件", notificationsSent);
    }

    /**
     * 判斷是否應該發送通知
     */
    private boolean shouldSendNotification(Subscription subscription, LocalDate today) {
        // 1. 檢查訂閱是否啟用通知
        if (subscription.getNotificationEnabled() == null || !subscription.getNotificationEnabled()) {
            return false;
        }

        // 2. 檢查是否已經發送過提醒
        if (subscription.getReminderSent() != null && subscription.getReminderSent()) {
            return false;
        }

        // 3. 檢查下次付款日期是否存在
        LocalDate nextPaymentDate = subscription.getNextPaymentDate();
        if (nextPaymentDate == null) {
            return false;
        }

        // 4. 獲取用戶的提醒天數設置
        User user = subscription.getUser();
        Integer notificationDays = user.getNotificationDays();
        if (notificationDays == null || notificationDays < 0) {
            notificationDays = 7; // 默認7天
        }

        // 5. 計算通知日期（下次付款日期前N天）
        LocalDate notificationDate = nextPaymentDate.minusDays(notificationDays);

        // 6. 判斷今天是否是通知日期或之後（防止錯過通知）
        return !today.isBefore(notificationDate) && !today.isAfter(nextPaymentDate);
    }

    /**
     * 發送付款提醒郵件
     */
    private void sendPaymentReminderEmail(Subscription subscription) throws Exception {
        User user = subscription.getUser();
        LocalDate nextPaymentDate = subscription.getNextPaymentDate();
        Double amount = subscription.getPrice() != null ? subscription.getPrice().doubleValue() : 0.0;

        emailService.sendSubscriptionRenewalReminder(
                user.getEmail(),
                user.getName(),
                subscription.getName(),
                nextPaymentDate,
                amount
        );

        log.info("已發送付款提醒郵件: 用戶={}, 訂閱={}", user.getEmail(), subscription.getName());
    }

    /**
     * 重置已發送的提醒標記
     * 當訂閱的下次付款日期更新時調用
     */
    public void resetReminderFlag(Subscription subscription) {
        subscription.setReminderSent(false);
        subscriptionRepository.save(subscription);
    }
}
