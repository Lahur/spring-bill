package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillSearchParams;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.service.document.IngoingStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bill/ingoing")
@RequiredArgsConstructor
@Tag(name = "Ingoing Bill", description = "Endpoints for managing ingoing bills")
public class IngoingBillController {

    private final IngoingStrategy ingoingStrategy;

    @GetMapping
    @Operation(summary = "Get all bills")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BillResponse> getBills() {
        return ingoingStrategy.getBills();
    }

    @PostMapping("/filter")
    @Operation(summary = "Get bills filtered by date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BillResponse> getBillsFilter(@RequestBody BillSearchParams params) {
        return ingoingStrategy.getBillsFilter(params);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillInfoResponse getBillInfo(@Parameter(description = "Bill ID") @PathVariable Long id) {
        return ingoingStrategy.getBillInfo(String.valueOf(id));
    }

    @GetMapping("/{id}/pdf417")
    @Operation(summary = "Generate PDF417 barcode for a bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Barcode generated successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public String generatePdf417(@Parameter(description = "Bill ID") @PathVariable Long id) {
        return ingoingStrategy.generatePdf417Ingoing(String.valueOf(id)).orElse(null);
    }

    @PostMapping("/{id}/mark-as-paid")
    @Operation(summary = "Mark a bill as paid")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill marked as paid successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> markAsPaid(@Parameter(description = "Bill ID") @PathVariable Long id) {
        ingoingStrategy.markDocumentAsPaid(String.valueOf(id));
        return ResponseEntity.ok().build();
    }

}