package hr.bill.spring_bill.dto.eposlovanje.f1_web.request;

import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.*;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateReceiptDto(
        Integer id,
        String issueDateTime,
        PaymentMethod paymentMethod,
        ReceiptType receiptType,
        String notes,
        String paymentDueDate,
        String buyerName,
        String buyerOib,
        String buyerAddress,
        String buyerCity,
        String buyerPostalCode,
        String buyerEmail,
        List<ReceiptItemDto> items,
        Double marginAmount,
        Double vatExemptAmount,
        Double nonTaxableAmount,
        String paragonReceiptNumber,
        String specialPurpose,
        List<ConsumptionTaxDto> consumptionTaxes,
        List<OtherTaxDto> otherTaxes,
        List<SurchargeDto> surcharges
) {}
