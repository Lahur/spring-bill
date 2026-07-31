package hr.bill.spring_bill.dto.eposlovanje.f1_web.request;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import lombok.Builder;

@Builder
public record GetReceiptsQuery(
        String dateFrom,
        String dateTo,
        FiscalStatus fiscalStatus,
        PaymentMethod paymentMethod,
        Integer cashRegisterId,
        String searchTerm,
        Integer page,
        Integer pageSize,
        String sortBy,
        Boolean sortDescending
) {}
