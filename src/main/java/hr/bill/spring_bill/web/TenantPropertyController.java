package hr.bill.spring_bill.web;

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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenant-property")
@RequiredArgsConstructor
@Tag(name = "Tenant property", description = "Endpoints for the tenant's settings (statement mail-to, document counters)")
public class TenantPropertyController {

    private final TenantPropertyService tenantPropertyService;

    @GetMapping
    @Operation(summary = "List the tenant's properties that are set")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Properties retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<TenantPropertyDto> getAll() {
        return tenantPropertyService.findAll().stream()
                .map(entity -> new TenantPropertyDto(entity.getProperty(), entity.getValue()))
                .toList();
    }

    @GetMapping("/{property}")
    @Operation(summary = "Get one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Property is not set"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TenantPropertyDto get(@PathVariable TenantPropety property) {
        return tenantPropertyService.find(property)
                .map(value -> new TenantPropertyDto(property, value))
                .orElseThrow(() -> new NotFoundException("Tenant property " + property + " is not set"));
    }

    @PutMapping("/{property}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property saved successfully"),
            @ApiResponse(responseCode = "400", description = "Unknown property or invalid value"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void save(@PathVariable TenantPropety property,
                     @RequestBody @Validated TenantPropertyValueRequest request) {
        tenantPropertyService.save(property, request.value());
    }

    @DeleteMapping("/{property}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove one of the tenant's properties")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Property removed (or wasn't set)"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public void delete(@PathVariable TenantPropety property) {
        tenantPropertyService.save(property, null);
    }
}
