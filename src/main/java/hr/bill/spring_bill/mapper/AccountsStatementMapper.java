package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementRequest;
import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementResponse;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CreateAccountsStatementRequest;
import hr.bill.spring_bill.model.AccountsStatementEntity;
import hr.bill.spring_bill.model.CashWithdrawalBalanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AccountsStatementMapper {

    @Mapping(source = "request.cashWithdrawalIds", target = "cashWithdrawalBalances")
    @Mapping(source = "billPath", target = "billPath")
    @Mapping(target = "sentCount", ignore = true)
    AccountsStatementEntity toAccountsStatementEntity(AccountsStatementRequest request, String billPath);

    @Mapping(source = "billPath", target = "billPath")
    @Mapping(target = "sentCount", ignore = true)
    AccountsStatementEntity toAccountsStatementEntity(CreateAccountsStatementRequest request, String billPath);

    @Mapping(source = "cashWithdrawalBalances", target = "cashWithdrawalBalanceIds")
    @Mapping(target = "hasBill", expression = "java(entity.getBillPath() != null)")
    AccountsStatementResponse toAccountsStatementResponse(AccountsStatementEntity entity);

    List<AccountsStatementResponse> toAccountsStatementResponseList(List<AccountsStatementEntity> entities);

    default Set<CashWithdrawalBalanceEntity> mapCashWithdrawalIds(Map<Integer, UUID> cashWithdrawalIds) {
        return cashWithdrawalIds.values().stream()
                .map(id -> CashWithdrawalBalanceEntity.builder().id(id).build())
                .collect(Collectors.toSet());
    }

    default UUID mapCashWithdrawalBalanceId(CashWithdrawalBalanceEntity cashWithdrawalBalance) {
        return cashWithdrawalBalance.getId();
    }
}