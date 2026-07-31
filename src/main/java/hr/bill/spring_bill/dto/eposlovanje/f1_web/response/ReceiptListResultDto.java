package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

import java.util.List;

public record ReceiptListResultDto(
        List<ReceiptSummaryDto> items,
        Integer totalCount,
        Integer page,
        Integer pageSize,
        Integer totalPages,
        Boolean hasPreviousPage,
        Boolean hasNextPage
) {}
