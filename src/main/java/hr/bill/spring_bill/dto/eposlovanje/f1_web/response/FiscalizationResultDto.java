package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

public record FiscalizationResultDto(
        Boolean isSuccess,
        String jir,
        String zki,
        String errorMessage,
        String errorCode,
        Boolean canRetry,
        ReceiptDto receipt
) {}
