package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.dashboard.DailyTotalResponse;
import hr.bill.spring_bill.dto.web.dashboard.DashboardSummaryResponse;
import hr.bill.spring_bill.dto.web.dashboard.MonthlySummaryResponse;
import hr.bill.spring_bill.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard metrics and charts")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(summary = "Get current month summary totals for the metric cards")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public DashboardSummaryResponse getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/daily-sales")
    @Operation(summary = "Get daily sales totals for a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daily totals retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<DailyTotalResponse> getDailySales(
            @Parameter(description = "Start date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return dashboardService.getDailySales(from, to);
    }

    @GetMapping("/monthly-summary")
    @Operation(summary = "Get persisted monthly summary totals for the last N completed months")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Monthly summaries retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<MonthlySummaryResponse> getMonthlySummaries(
            @Parameter(description = "Number of months to return") @RequestParam(defaultValue = "12") int months) {
        return dashboardService.getMonthlySummaries(months);
    }
}