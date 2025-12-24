package com.subcycle.service;

import com.subcycle.entity.Subscription;
import com.subcycle.repository.SubscriptionRepository;
import com.subcycle.entity.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 排程：自動將已過期的訂閱扣款日往後推至下一次扣款日。
 */
@Service
@Slf4j
@SuppressWarnings("null")
public class SubscriptionScheduleService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * 每日淩晨執行，將所有 nextPaymentDate 已過期的訂閱往後推到下一次扣款日。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void rollOverExpiredNextPaymentDates() {
        LocalDate today = LocalDate.now();
        List<Subscription> expired = subscriptionRepository.findByNextPaymentDateBefore(today);

        if (expired.isEmpty()) {
            return;
        }

        int updated = rollOverToFuture(expired, today);

        if (updated > 0) {
            log.info("Subscription schedule: rolled over {} subscriptions to next cycle", updated);
        }
    }

    /**
     * 提供手動觸發：只處理單一用戶的過期訂閱。
     * @return 更新筆數
     */
    @Transactional
    public int rollOverExpiredForUser(User user) {
        LocalDate today = LocalDate.now();
        List<Subscription> expired = subscriptionRepository.findByUserAndNextPaymentDateBefore(user, today);
        if (expired.isEmpty()) {
            return 0;
        }
        int updated = rollOverToFuture(expired, today);
        if (updated > 0) {
            log.info("Manual rollover: user {} rolled over {} subscriptions", user.getId(), updated);
        }
        return updated;
    }

    private int rollOverToFuture(List<Subscription> subs, LocalDate today) {
        int updatedCount = 0;

        for (Subscription sub : subs) {
            LocalDate nextDate = sub.getNextPaymentDate();
            LocalDate endDate = sub.getEndDate();
            if (nextDate == null) {
                continue;
            }

            String cycle = sub.getBillingCycle() != null ? sub.getBillingCycle() : "monthly";

            boolean statusChanged = false;
            boolean dateCleared = false;
            LocalDate originalNext = nextDate;

            // 持續往後推直到日期不再落於今天之前
            while (nextDate.isBefore(today)) {
                LocalDate candidate = getNextPaymentDate(nextDate, cycle);

                // 若設定到期日且下一個扣款日會超過到期日，標記為已到期，不再更新下一次扣款日
                if (endDate != null && candidate.isAfter(endDate)) {
                    if (!"expired".equalsIgnoreCase(sub.getStatus())) {
                        sub.setStatus("expired");
                        statusChanged = true;
                    }
                    sub.setNextPaymentDate(null);
                    dateCleared = true;
                    nextDate = null; // 不更新 nextPaymentDate
                    break;
                }
                nextDate = candidate;
            }

            if (nextDate != null && !nextDate.equals(originalNext)) {
                sub.setNextPaymentDate(nextDate);
                sub.setReminderSent(false);
                updatedCount++;
            } else if (statusChanged || dateCleared) {
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            subscriptionRepository.saveAll(subs);
        }
        return updatedCount;
    }

    private LocalDate getNextPaymentDate(LocalDate current, String cycle) {
        return switch (cycle.toLowerCase()) {
            case "daily" -> current.plusDays(1);
            case "weekly" -> current.plusWeeks(1);
            case "monthly" -> current.plusMonths(1);
            case "quarterly" -> current.plusMonths(3);
            case "yearly" -> current.plusYears(1);
            default -> current.plusMonths(1);
        };
    }
}
