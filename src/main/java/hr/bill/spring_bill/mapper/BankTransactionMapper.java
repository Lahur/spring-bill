package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.banktransaction.BankTransactionResponse;
import hr.bill.spring_bill.model.BankTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankTransactionMapper {

    @Mapping(source = "bankStatement.id", target = "bankStatementId")
    BankTransactionResponse toBankTransactionResponse(BankTransactionEntity entity);

    List<BankTransactionResponse> toBankTransactionResponseList(List<BankTransactionEntity> entities);
}
