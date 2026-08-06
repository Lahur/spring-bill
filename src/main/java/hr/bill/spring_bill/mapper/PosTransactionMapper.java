package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.model.PosTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
}