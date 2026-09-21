package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.BillSearchParams;
import hr.bill.spring_bill.dto.web.bill.b2c.F1BillRequest;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.service.document.F1OutgoingStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bill/f1")
@RequiredArgsConstructor
@Tag(name = "F1 Bill", description = "Endpoints for managing F1 bills")
public class F1BillController {

    private final F1OutgoingStrategy f1OutgoingStrategy;

    @GetMapping
    @Operation(summary = "Get all bills")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BillResponse> getBills() {
        return f1OutgoingStrategy.getBills();
    }

    @PostMapping("/filter")
    @Operation(summary = "Get bills filtered by date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BillResponse> getBillsFilter(@RequestBody @Validated BillSearchParams params) {
        return f1OutgoingStrategy.getBillsFilter(params);
    }

    @PostMapping
    @Operation(summary = "Create a new bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillResponse createBill(@RequestBody @Validated F1BillRequest request) {
        return f1OutgoingStrategy.createBill(request);
    }

    @PostMapping("/full-refresh")
    @Operation(summary = "Fully refresh bills for the current month",
            description = "Re-syncs all receipts issued from the start of the current month until now, ignoring the last-sync cursor. Returns the number of bills synced")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills refreshed successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public int fullRefresh() {
        return f1OutgoingStrategy.fullRefresh();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillInfoResponse getBillInfo(@Parameter(description = "Bill ID") @PathVariable int id) {
        return f1OutgoingStrategy.getBillInfo(String.valueOf(id));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancels a bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillResponse cancelBill(
            @Parameter(description = "ID of the bill to cancel") @RequestParam Integer originalId) {
        return f1OutgoingStrategy.cancel(originalId.toString(), null);
    }

    @PostMapping("/review")
    @Operation(summary = "Preview a bill before creating it")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill preview generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillReviewResponse reviewBill(@RequestBody @Validated F1BillRequest request) {
        return f1OutgoingStrategy.reviewBill(request);
    }

}