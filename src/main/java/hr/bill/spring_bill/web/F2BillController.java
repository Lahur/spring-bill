package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.bill.BillResponse;
import hr.bill.spring_bill.dto.web.bill.BillReviewResponse;
import hr.bill.spring_bill.dto.web.bill.b2b.F2BillRequest;
import hr.bill.spring_bill.dto.web.bill.info.BillInfoResponse;
import hr.bill.spring_bill.service.document.F2OutgoingStrategy;
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
@RequestMapping("/bill/f2")
@RequiredArgsConstructor
@Tag(name = "F2 Bill", description = "Endpoints for managing F2 bills")
public class F2BillController {

    private final F2OutgoingStrategy f2OutgoingStrategy;

    @GetMapping
    @Operation(summary = "Get all bills")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bills retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BillResponse> getBills() {
        return f2OutgoingStrategy.getBills();
    }

    @PostMapping
    @Operation(summary = "Create a new bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillResponse createBill(@RequestBody @Validated F2BillRequest request) {
        return f2OutgoingStrategy.createBill(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bill details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillInfoResponse getBillInfo(@Parameter(description = "Bill ID") @PathVariable Long id) {
        return f2OutgoingStrategy.getBillInfo(String.valueOf(id));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancels a bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Bill not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillResponse cancelBill(
            @Parameter(description = "ID of the bill to cancel") @RequestParam Integer originalId,
            @Parameter(description = "ID of the new cancellation document") @RequestParam Integer newId) {
        return f2OutgoingStrategy.cancel(originalId.toString(), newId.toString());
    }

    @PostMapping("/review")
    @Operation(summary = "Preview a bill before creating it")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bill preview generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BillReviewResponse reviewBill(@RequestBody @Validated F2BillRequest request) {
        return f2OutgoingStrategy.reviewBill(request);
    }

}