package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.BusinessCheckResponse;
import hr.bill.spring_bill.service.BusinessEntityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/business-entity")
@RequiredArgsConstructor
@Tag(name = "Business Entity", description = "Endpoints for looking up business entities")
public class BusinessEntityController {

    private final BusinessEntityService businessEntityService;

    @GetMapping("/check")
    @Operation(summary = "Check business entity validity by OIB")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Check completed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BusinessCheckResponse checkByOib(@Parameter(description = "OIB of the business entity") @RequestParam String oib) {
        return businessEntityService.checkByOib(oib);
    }

}