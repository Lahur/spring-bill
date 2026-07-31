package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

public record ReceiptStatisticsDto(
        Integer totalCount,
        Integer fiscalizedCount,
        Integer pendingCount,
        Integer failedCount,
        Double totalRevenue,
        Double totalTax,
        Double todayRevenue,
        Integer todayCount,
        Double averageReceiptAmount
) {}
