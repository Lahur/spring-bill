package hr.bill.spring_bill.web;

import hr.bill.spring_bill.config.tenant.TenantContext;
import hr.bill.spring_bill.dto.web.tenant.TenantPropertyDto;
import hr.bill.spring_bill.dto.web.tenant.TenantPropertyValueRequest;
import hr.bill.spring_bill.exception.NotFoundException;
import hr.bill.spring_bill.model.enums.TenantPropety;
import hr.bill.spring_bill.service.TenantPropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenant/{tenantId}/property")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Tenant property", description = "Admin endpoints for a tenant's settings (statement mail-to, document counters)")
/** Not behind {@code TenantFilter}: each endpoint binds the {@code tenantId} path variable itself. */
public class TenantPropertyController {

    private final TenantPropertyService tenantPropertyService;

    @GetMapping
    @Operation(summary = "List the tenant's properties that are set")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Properties retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TenantPropertyDto> getAll(@PathVariable UUID tenantId) {
        return TenantContext.callAs(tenantId, () -> tenantPropertyService.findAll().stream()
                .map(entity -> new TenantPropertyDto(entity.getProperty(), entity.getValue()))
                .toList());
    }

    @GetMapping("/{property}")
    @Operation(summary = "Get one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Property is not set"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantPropertyDto get(@PathVariable UUID tenantId, @PathVariable TenantPropety property) {
        return TenantContext.callAs(tenantId, () -> tenantPropertyService.find(property)
                .map(value -> new TenantPropertyDto(property, value))
                .orElseThrow(() -> new NotFoundException("Tenant property " + property + " is not set")));
    }

    @PutMapping("/{property}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property saved successfully"),
            @ApiResponse(responseCode = "400", description = "Unknown property or invalid value"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void save(@PathVariable UUID tenantId, @PathVariable TenantPropety property,
                     @RequestBody @Validated TenantPropertyValueRequest request) {
        TenantContext.runAs(tenantId, () -> tenantPropertyService.save(property, request.value()));
    }

    @DeleteMapping("/{property}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property removed (or wasn't set)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void delete(@PathVariable UUID tenantId, @PathVariable TenantPropety property) {
        TenantContext.runAs(tenantId, () -> tenantPropertyService.save(property, null));
    }
}
