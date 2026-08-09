package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.cashwithdrawal.CashWithdrawalBalanceResponse;
import hr.bill.spring_bill.model.AccountsStatementEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CashWithdrawalBalanceMapper {

    @Mapping(source = "bankTransaction.id", target = "bankTransactionId")
    @Mapping(source = "bankTransaction.amount", target = "amount")
    @Mapping(source = "bankTransaction.additionalRemittanceInfo", target = "additionalRemittanceInfo")
    @Mapping(source = "bankTransaction.transactionDate", target = "transactionDate")
    @Mapping(source = "accountsStatements", target = "accountsStatementIds")
    CashWithdrawalBalanceResponse toCashWithdrawalBalanceResponse(CashWithdrawalBalanceEntity entity);

    List<CashWithdrawalBalanceResponse> toCashWithdrawalBalanceResponseList(List<CashWithdrawalBalanceEntity> entities);

    default UUID mapAccountsStatementId(AccountsStatementEntity accountsStatement) {
        return accountsStatement.getId();
    }
}