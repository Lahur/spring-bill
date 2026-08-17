package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.bill_pdf.request.PosTransactionRequest;
import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.model.PosTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PosTransactionMapper {

    @Mapping(source = "bankTransaction.id", target = "bankTransactionId")
    @Mapping(source = "bankTransaction.amount", target = "amount")
    @Mapping(source = "bankTransaction.senderIban", target = "senderIban")
    @Mapping(source = "bankTransaction.receiverIban", target = "receiverIban")
    @Mapping(source = "bankTransaction.reference", target = "reference")
    @Mapping(source = "bankTransaction.additionalRemittanceInfo", target = "additionalRemittanceInfo")
    @Mapping(source = "bankTransaction.transactionDate", target = "transactionDate")
    @Mapping(target = "hasBill", expression = "java(entity.getBillPath() != null)")
    PosTransactionResponse toPosTransactionResponse(PosTransactionEntity entity);

    List<PosTransactionResponse> toPosTransactionResponseList(List<PosTransactionEntity> entities);

    @Mapping(target = "transactionId", expression = "java(entity.getId().toString())")
    @Mapping(target = "bankTransactionId", expression = "java(entity.getBankTransaction().getId().toString())")
    @Mapping(target = "amount", expression = "java(formatAmount(entity.getBankTransaction().getAmount()))")
    @Mapping(source = "bankTransaction.bankStatement.currency", target = "currencyCode")
    @Mapping(source = "bankTransaction.senderIban", target = "senderIban")
    @Mapping(source = "bankTransaction.receiverIban", target = "receiverIban")
    @Mapping(source = "bankTransaction.reference", target = "reference")
    @Mapping(source = "bankTransaction.additionalRemittanceInfo", target = "additionalRemittanceInfo")
    @Mapping(target = "transactionDate", expression = "java(formatDate(entity.getBankTransaction().getTransactionDate()))")
    @Mapping(target = "transactionTime", expression = "java(formatTime(entity.getBankTransaction().getTransactionDate()))")
    @Mapping(target = "hasBill", expression = "java(entity.getBillPath() != null)")
    PosTransactionRequest toPosTransactionRequest(PosTransactionEntity entity);

    default String formatAmount(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    @Named("formatDate")
    default String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
    }

    @Named("formatTime")
    default String formatTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}