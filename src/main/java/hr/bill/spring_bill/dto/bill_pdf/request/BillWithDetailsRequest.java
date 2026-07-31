package hr.bill.spring_bill.dto.bill_pdf.request;

import hr.bill.spring_bill.dto.bill_pdf.common.BillDetailsDto;
import lombok.Builder;

@Builder
public record BillWithDetailsRequest(
        String recipientName,
        String recipientAddress,
        String recipientPost,
        String recipientCity,
        String recipientOib,
        String billNumber,
        String billDate,
        String billTime,
        String billProjectDescription,
        String billProjectName,
        String billBasePrice,
        String billPdvPrice,
        String billTotalPrice,
        String billTotalText,
        Boolean billReverseCharge,
        Long paymentDays,
        String pdf417Image,
        String zki,
        String jir,
        BillDetailsDto details
) {}
