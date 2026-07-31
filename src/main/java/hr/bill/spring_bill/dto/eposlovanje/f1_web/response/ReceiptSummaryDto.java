package hr.bill.spring_bill.dto.eposlovanje.f1_web.response;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.FiscalStatus;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.ReceiptType;

public record ReceiptSummaryDto(
        Integer id,
        Integer receiptNumber,
        String formattedReceiptNumber,
        String issueDateTime,
        Double grandTotal,
        PaymentMethod paymentMethod,
        String paymentMethodDisplay,
        FiscalStatus fiscalStatus,
        String fiscalStatusDisplay,
        Boolean isFiscalized,
        Integer itemCount,
        ReceiptType receiptType,
        Boolean isCreditNote,
        Boolean hasCreditNote,
        String referencedReceiptFormattedNumber,
        Boolean canDeleteCreditNote,
        Boolean canFiscalize,
        String lastError,
        Boolean hasRestrictiveError,
        Integer emailSentCount,
        Boolean hasZki
) {}
