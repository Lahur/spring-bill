package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.banktransaction.BankTransactionResponse;
import hr.bill.spring_bill.service.BankTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bank-transaction")
@RequiredArgsConstructor
@Tag(name = "Bank Transaction", description = "Endpoints for bank transactions imported from bank statements")
public class BankTransactionController {

    private final BankTransactionService bankTransactionService;

    @GetMapping
    @Operation(summary = "Get all bank transactions, newest first")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank transactions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankTransactionResponse> findAll() {
        return bankTransactionService.findAll();
    }

    @GetMapping("/bank-statement/{bankStatementId}")
    @Operation(summary = "Get the bank transactions of a bank statement, in booking order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank transactions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankTransactionResponse> findAllByBankStatementId(
            @Parameter(description = "ID of the bank statement")
            @PathVariable UUID bankStatementId) {
        return bankTransactionService.findAllByBankStatementId(bankStatementId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a bank transaction by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank transaction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bank transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public BankTransactionResponse findById(@PathVariable UUID id) {
        return bankTransactionService.findById(id);
    }
}
