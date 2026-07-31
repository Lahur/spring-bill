package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

public record CreditNoteReadinessDto(
        Boolean canCreate,
        String reason,
        Boolean isFiscalized,
        Boolean hasExistingCreditNote
) {}
