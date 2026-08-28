package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementRequest;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CashWithdrawalBalanceResponse;
import hr.bill.spring_bill.service.CashWithdrawalBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cash-withdrawal-balance")
@RequiredArgsConstructor
@Tag(name = "Cash Withdrawal Balance", description = "Endpoints for cash withdrawal balances")
public class CashWithdrawalBalanceController {

    private final CashWithdrawalBalanceService cashWithdrawalBalanceService;

    @GetMapping
    @Operation(summary = "Get all cash withdrawal balances")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cash withdrawal balances retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<CashWithdrawalBalanceResponse> findAll() {
        return cashWithdrawalBalanceService.findAll();
    }

    @PostMapping(value = "/accounts-statement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create an accounts statement", description = "Accepts the accounts statement request alongside its PDF bill, deducting the amount from the referenced cash withdrawal balances")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts statement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or non-PDF file"),
            @ApiResponse(responseCode = "404", description = "Cash withdrawal balance not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<CashWithdrawalBalanceResponse> createAccountsStatement(
            @RequestPart("request") @Validated AccountsStatementRequest request,
            @Parameter(description = "PDF bill for the accounts statement")
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return cashWithdrawalBalanceService.createAccountsStatement(request, file);
    }
}