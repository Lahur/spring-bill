package hr.bill.spring_bill.mapper;

import hr.bill.spring_bill.dto.web.tenant.TenantApiKeyRequest;
import hr.bill.spring_bill.model.TenantApiKeyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantApiKeyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    void updateTenantApiKeyEntity(TenantApiKeyRequest request, @MappingTarget TenantApiKeyEntity entity);
}
