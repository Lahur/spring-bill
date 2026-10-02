package hr.bill.spring_bill.web;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.dto.web.tenant.StatementMailToDto;
import hr.bill.spring_bill.dto.web.tenant.TenantApiKeyRequest;
import hr.bill.spring_bill.dto.web.tenant.TenantDto;
import hr.bill.spring_bill.mapper.TenantMapper;
import hr.bill.spring_bill.model.enums.TenantPropety;
import hr.bill.spring_bill.service.TenantApiKeyService;
import hr.bill.spring_bill.service.TenantPropertyService;
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
import java.util.function.Supplier;

@RestController
@RequestMapping("/tenant/{tenantId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Tenant", description = "Admin endpoints for a tenant's (issuer) data used on bills")
/** Not behind {@code TenantFilter}: each endpoint binds the {@code tenantId} path variable itself. */
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;
    private final TenantApiKeyService tenantApiKeyService;
    private final TenantPropertyService tenantPropertyService;

    @GetMapping
    @Operation(summary = "Get the tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Tenant is not configured"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantDto get(@PathVariable UUID tenantId) {
        return asTenant(tenantId, () -> tenantMapper.toTenantDto(tenantService.get()));
    }

    @PutMapping
    @Operation(summary = "Create or replace the tenant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tenant saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tenant data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantDto save(@PathVariable UUID tenantId, @RequestBody @Validated TenantDto request) {
        return asTenant(tenantId, () -> tenantMapper.toTenantDto(tenantService.save(request)));
    }

    @PutMapping("/api-key")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Replace the tenant's API keys for the external clients (keys are write-only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "API keys saved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void saveApiKeys(@PathVariable UUID tenantId, @RequestBody TenantApiKeyRequest request) {
        TenantContext.runAs(tenantId, () -> tenantApiKeyService.save(request));
    }

    @GetMapping("/statement-mail-to")
    @Operation(summary = "Get where the tenant's imported bank statements are mailed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statement mail-to retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public StatementMailToDto getStatementMailTo(@PathVariable UUID tenantId) {
        return asTenant(tenantId, () ->
                new StatementMailToDto(tenantPropertyService.find(TenantPropety.STATEMENT_MAIL_TO).orElse(null)));
    }

    @PutMapping("/statement-mail-to")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set where the tenant's imported bank statements are mailed (blank clears it)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Statement mail-to saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void saveStatementMailTo(@PathVariable UUID tenantId, @RequestBody @Validated StatementMailToDto request) {
        TenantContext.runAs(tenantId, () -> tenantPropertyService.save(TenantPropety.STATEMENT_MAIL_TO, request.mailTo()));
    }

    private static <T> T asTenant(UUID tenantId, Supplier<T> action) {
        try (TenantContext.Scope ignored = TenantContext.bind(tenantId)) {
            return action.get();
        }
    }
}
