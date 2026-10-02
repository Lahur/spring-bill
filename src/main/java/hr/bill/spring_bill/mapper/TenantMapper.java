package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.model.TenantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    TenantDto toTenantDto(TenantEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "properties", ignore = true)
    void updateTenantEntity(TenantDto dto, @MappingTarget TenantEntity entity);
}
