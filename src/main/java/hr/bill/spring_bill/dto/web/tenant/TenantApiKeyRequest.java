package hr.bill.spring_bill.dto.web.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "API keys the tenant uses for the external clients; replaces all of them")
public record TenantApiKeyRequest(

        @Schema(description = "eposlovanje API key")
        String eposlovanjeApiKey,

        @Schema(description = "F1 web API key")
        String f1WebApiKey,

        @Schema(description = "Pondi (eposlovanje util) API key")
        String pondiApiKey,

        @Schema(description = "AIS eposlovanje API key")
        String aisEposlovanjeApiKey,

        @Schema(description = "Tenant id in hub-bill, selects the tenant's PDF templates; blank uses hub-bill's bundled ones",
                example = "tehnomodus")
        String hubTenantId
) {
}
