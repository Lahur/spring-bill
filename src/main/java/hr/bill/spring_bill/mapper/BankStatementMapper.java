package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.model.BankStatementEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BankStatementMapper {

    BankStatementResponse toBankStatementResponse(BankStatementEntity entity);

    List<BankStatementResponse> toBankStatementResponseList(List<BankStatementEntity> entities);
}
