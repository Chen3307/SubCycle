package com.subcycle.service;

import com.subcycle.dto.DashboardStatistics;
import com.subcycle.entity.Subscription;
import com.subcycle.entity.User;
import com.subcycle.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 儀表板統計服務
 */
@Service
public class DashboardService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * 獲取儀表板統計數據
     */
    public DashboardStatistics getDashboardStatistics(User user) {
        List<Subscription> subscriptions = subscriptionRepository.findByUser(user);

        DashboardStatistics stats = new DashboardStatistics();

        // 基本統計
        stats.setTotalSubscriptions(subscriptions.size());
        stats.setMonthlyAverage(calculateMonthlyAverage(subscriptions));
        stats.setNext30DaysTotal(calculateNext30DaysTotal(subscriptions));

        // 本月統計
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        stats.setCurrentMonthPaid(calculateMonthPayments(subscriptions, currentMonth, true));
        stats.setCurrentMonthTotal(calculateMonthPayments(subscriptions, currentMonth, false));
        stats.setCurrentMonthRemaining(stats.getCurrentMonthTotal().subtract(stats.getCurrentMonthPaid()));

        // 計算進度百分比
        if (stats.getCurrentMonthTotal().compareTo(BigDecimal.ZERO) > 0) {
            stats.setCurrentMonthProgress(
                stats.getCurrentMonthPaid()
                    .divide(stats.getCurrentMonthTotal(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
            );
        } else {
            stats.setCurrentMonthProgress(0.0);
        }

        // 上月統計（環比數據）
        YearMonth lastMonth = currentMonth.minusMonths(1);
        stats.setLastMonthTotal(calculateMonthPayments(subscriptions, lastMonth, false));
        stats.setMonthOverMonthChange(stats.getCurrentMonthTotal().subtract(stats.getLastMonthTotal()));

        // 環比變化百分比
        if (stats.getLastMonthTotal().compareTo(BigDecimal.ZERO) > 0) {
            stats.setMonthOverMonthChangePercent(
                stats.getMonthOverMonthChange()
                    .divide(stats.getLastMonthTotal(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
            );
        } else {
            stats.setMonthOverMonthChangePercent(0.0);
        }

        // Top 5 訂閱（按本月花費排序）
        stats.setTopSubscriptions(getTopSubscriptions(subscriptions, 5));

        return stats;
    }

    /**
     * 計算月均支出
     */
    private BigDecimal calculateMonthlyAverage(List<Subscription> subscriptions) {
        LocalDate now = LocalDate.now();

        return subscriptions.stream()
            .filter(sub -> {
                LocalDate endDate = sub.getEndDate();
                LocalDate startDate = sub.getStartDate();
                // 如果設定了到期日，且已經過期，則不計入月均支出
                if (endDate != null && endDate.isBefore(now)) {
                    return false;
                }
                return startDate == null || !startDate.isAfter(now);
            })
            .map(this::getMonthlyAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 計算下個月應付總額
     */
    private BigDecimal calculateNext30DaysTotal(List<Subscription> subscriptions) {
        LocalDate now = LocalDate.now();
        YearMonth nextMonth = YearMonth.from(now).plusMonths(1);
        LocalDate nextMonthStart = nextMonth.atDay(1);
        LocalDate nextMonthEnd = nextMonth.atEndOfMonth();

        return subscriptions.stream()
            .filter(sub -> {
                LocalDate nextPayment = sub.getNextPaymentDate();
                LocalDate endDate = sub.getEndDate();
                LocalDate startDate = sub.getStartDate();

                // 如果沒有下次付款日期，跳過
                if (nextPayment == null) {
                    return false;
                }

                // 如果設定了到期日，且下次付款日超過到期日，則不計入
                if (endDate != null && nextPayment.isAfter(endDate)) {
                    return false;
                }

                if (startDate != null && startDate.isAfter(nextMonthEnd)) {
                    return false;
                }

                // 計算下個月（從下個月1日到下個月最後一天）的所有付款
                return !nextPayment.isBefore(nextMonthStart) && !nextPayment.isAfter(nextMonthEnd);
            })
            .map(Subscription::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 計算指定月份的支付金額
     * @param subscriptions 訂閱列表
     * @param month 目標月份
     * @param onlyPast 是否只計算已過去的扣款（true=已支出，false=預計總支出）
     */
    private BigDecimal calculateMonthPayments(List<Subscription> subscriptions, YearMonth month, boolean onlyPast) {
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();
        LocalDate now = LocalDate.now();

        BigDecimal total = BigDecimal.ZERO;

        for (Subscription sub : subscriptions) {
            LocalDate nextPayment = sub.getNextPaymentDate();
            LocalDate endDate = sub.getEndDate();
            LocalDate startDate = sub.getStartDate();
            Boolean includeHistorical = sub.getIncludeHistoricalPayments();
            if (nextPayment == null) continue;
            if (startDate != null && startDate.isAfter(monthEnd)) continue;

            // 從下次扣款日期開始計算
            LocalDate currentDate = nextPayment;

            // 如果不包含歷史支出，且該月在今天之前，跳過
            if (Boolean.FALSE.equals(includeHistorical) && monthEnd.isBefore(now)) {
                continue;
            }

            // 往前推到月初之前
            while (currentDate.isAfter(monthStart)) {
                currentDate = getPreviousPaymentDate(currentDate, sub.getBillingCycle());
            }

            // 從月初往後計算所有在該月的扣款
            while (currentDate.isBefore(monthEnd.plusDays(1))) {
                if (!currentDate.isBefore(monthStart) && !currentDate.isAfter(monthEnd)) {
                    LocalDate paymentDate = currentDate;

                    // 如果這筆扣款在 startDate 之前，檢查是否需要調整到 startDate
                    if (startDate != null && currentDate.isBefore(startDate)) {
                        LocalDate nextPaymentDate = getNextPaymentDate(currentDate, sub.getBillingCycle());
                        if (nextPaymentDate.isAfter(startDate)) {
                            // 這是跨越 startDate 的扣款，調整到 startDate
                            paymentDate = startDate;
                        } else {
                            // 還沒到 startDate，跳過
                            currentDate = getNextPaymentDate(currentDate, sub.getBillingCycle());
                            continue;
                        }
                    }

                    // 檢查是否超過到期日
                    boolean isAfterEndDate = endDate != null && paymentDate.isAfter(endDate);

                    // 如果沒有超過到期日，才計入
                    if (!isAfterEndDate && !paymentDate.isBefore(monthStart) && !paymentDate.isAfter(monthEnd)) {
                        // 如果不包含歷史支出，只計算今天之後的扣款
                        if (Boolean.FALSE.equals(includeHistorical) && paymentDate.isBefore(now)) {
                            currentDate = getNextPaymentDate(currentDate, sub.getBillingCycle());
                            continue;
                        }

                        // 如果是計算已支出，只計算今天之前的
                        if (!onlyPast || !paymentDate.isAfter(now)) {
                            total = total.add(sub.getPrice());
                        }
                    }
                }
                currentDate = getNextPaymentDate(currentDate, sub.getBillingCycle());
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 獲取下次扣款日期
     */
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

    /**
     * 獲取上次扣款日期
     */
    private LocalDate getPreviousPaymentDate(LocalDate current, String cycle) {
        return switch (cycle.toLowerCase()) {
            case "daily" -> current.minusDays(1);
            case "weekly" -> current.minusWeeks(1);
            case "monthly" -> current.minusMonths(1);
            case "quarterly" -> current.minusMonths(3);
            case "yearly" -> current.minusYears(1);
            default -> current.minusMonths(1);
        };
    }

    /**
     * 轉換為月均金額
     */
    private BigDecimal getMonthlyAmount(Subscription sub) {
        BigDecimal price = sub.getPrice();
        return switch (sub.getBillingCycle().toLowerCase()) {
            case "daily" -> price.multiply(BigDecimal.valueOf(30));
            case "weekly" -> price.multiply(BigDecimal.valueOf(4.33));
            case "monthly" -> price;
            case "quarterly" -> price.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case "yearly" -> price.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            default -> price;
        };
    }

    /**
     * 獲取支出最高的訂閱
     */
    private List<DashboardStatistics.TopSubscription> getTopSubscriptions(List<Subscription> subscriptions, int limit) {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);

        return subscriptions.stream()
            // 過濾已過期的訂閱
            .filter(sub -> {
                LocalDate endDate = sub.getEndDate();
                // 如果設定了到期日且已經過期，則不列入 Top 5
                return endDate == null || !endDate.isBefore(now);
            })
            .sorted(Comparator.comparing(
                sub -> calculateMonthPayments(List.of(sub), currentMonth, false),
                Comparator.reverseOrder()
            ))
            .limit(limit)
            .map(sub -> new DashboardStatistics.TopSubscription(
                sub.getId(),
                sub.getName(),
                sub.getPrice(),
                sub.getBillingCycle(),
                calculateMonthPayments(List.of(sub), currentMonth, false)
            ))
            .collect(Collectors.toList());
    }
}
