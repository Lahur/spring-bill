package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.bill_pdf.request.BillRequest;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.response.ReceiptDto;
import hr.bill.spring_bill.service.NumberToWordsService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring", imports = {NumberToWordsService.class, LocalDateTime.class, LocalDate.class, ChronoUnit.class})
public interface ReceiptBillRequestMapper {

    @Mapping(target = "recipientName", source = "receiptDto.buyerName")
    @Mapping(target = "recipientAddress", source = "receiptDto.buyerAddress")
    @Mapping(target = "recipientPost", source = "receiptDto.buyerPostalCode")
    @Mapping(target = "recipientCity", source = "receiptDto.buyerCity")
    @Mapping(target = "recipientOib", source = "receiptDto.buyerOib")
    @Mapping(target = "billNumber", source = "receiptDto.formattedReceiptNumber")
    @Mapping(target = "billDate", expression = "java(formatDate(receiptDto.issueDateTime()))")
    @Mapping(target = "billTime", expression = "java(formatTime(receiptDto.issueDateTime()))")
    @Mapping(target = "billProjectDescription", expression = "java(receiptDto.items().getFirst().description())")
    @Mapping(target = "billProjectName", expression = "java(receiptDto.items().getFirst().name())")
    @Mapping(target = "billBasePrice", expression = "java(formatAmount(receiptDto.totalAmount()))")
    @Mapping(target = "billPdvPrice", expression = "java(formatAmount(receiptDto.taxAmount()))")
    @Mapping(target = "billTotalPrice", expression = "java(formatAmount(receiptDto.grandTotal()))")
    @Mapping(target = "billTotalText", expression = "java(NumberToWordsService.asWords(java.math.BigDecimal.valueOf(receiptDto.grandTotal())))")
    @Mapping(target = "billReverseCharge", constant = "false")
    @Mapping(target = "paymentDays", expression = "java(paymentDays(receiptDto))")
    @Mapping(target = "zki", source = "receiptDto.zki")
    @Mapping(target = "jir", source = "receiptDto.jir")
    BillRequest toBillRequest(ReceiptDto receiptDto, String pdf417Image);

    default String formatAmount(Double amount) {
        return amount == null ? null : BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    @Named("formatDate")
    default String formatDate(String isoDateTime) {
        return isoDateTime == null ? null : LocalDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
    }

    @Named("formatTime")
    default String formatTime(String isoDateTime) {
        return isoDateTime == null ? null : LocalDateTime.parse(isoDateTime).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    default Long paymentDays(ReceiptDto receiptDto) {
        if (receiptDto.paymentDueDate() == null || receiptDto.issueDateTime() == null) {
            return 0L;
        }
        LocalDate issueDate = LocalDateTime.parse(receiptDto.issueDateTime()).toLocalDate();
        LocalDate dueDate = LocalDateTime.parse(receiptDto.paymentDueDate()).toLocalDate();
        return ChronoUnit.DAYS.between(dueDate, issueDate);
    }
}