package hr.bill.spring_bill.web;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.dto.web.tenant.TenantApiKeyRequest;
import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.mapper.TenantMapper;
import hr.bill.spring_bill.service.TenantApiKeyService;
import hr.bill.spring_bill.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tenant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Tenant", description = "Admin endpoints for a tenant's (issuer) data used on bills")
/** Not behind {@code TenantFilter}: create needs no tenant, the rest bind the {@code tenantId} path variable themselves. */
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;
    private final TenantApiKeyService tenantApiKeyService;

    @GetMapping("/{tenantId}")
    @Operation(summary = "Get the tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant is not configured"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantDto get(@PathVariable UUID tenantId) {
        return TenantContext.callAs(tenantId, () -> tenantMapper.toTenantDto(tenantService.get()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new tenant; the response carries its generated id")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tenant created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tenant data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantDto create(@RequestBody @Validated TenantDto request) {
        return tenantMapper.toTenantDto(tenantService.create(request));
    }

    @PutMapping("/{tenantId}")
    @Operation(summary = "Replace an existing tenant's data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tenant data"),
            @ApiResponse(responseCode = "404", description = "Tenant doesn't exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantDto update(@PathVariable UUID tenantId, @RequestBody @Validated TenantDto request) {
        return TenantContext.callAs(tenantId, () -> tenantMapper.toTenantDto(tenantService.update(request)));
    }

    @PutMapping("/{tenantId}/api-key")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Replace the tenant's API keys for the external clients (keys are write-only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "API keys saved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void saveApiKeys(@PathVariable UUID tenantId, @RequestBody TenantApiKeyRequest request) {
        TenantContext.runAs(tenantId, () -> tenantApiKeyService.save(request));
    }
}
