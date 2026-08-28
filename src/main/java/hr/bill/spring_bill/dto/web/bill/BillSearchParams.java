package hr.bill.spring_bill.dto.web.bill;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BillSearchParams(
        LocalDate dateFrom,
        LocalDate dateTill
) {
}
