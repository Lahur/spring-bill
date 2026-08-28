package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.cashwithdrawal.AccountsStatementResponse;
import hr.bill.spring_bill.dto.web.cashwithdrawal.CreateAccountsStatementRequest;
import hr.bill.spring_bill.service.AccountsStatementService;
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
import java.util.UUID;

@RestController
@RequestMapping("/accounts-statement")
@RequiredArgsConstructor
@Tag(name = "Accounts Statement", description = "Endpoints for accounts statements")
public class AccountsStatementController {

    private final AccountsStatementService accountsStatementService;

    @GetMapping
    @Operation(summary = "Get all accounts statements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts statements retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<AccountsStatementResponse> findAll() {
        return accountsStatementService.findAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new accounts statement", description = "Accepts the accounts statement request alongside an optional PDF bill")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts statement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or non-PDF file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public AccountsStatementResponse create(
            @RequestPart("request") @Validated CreateAccountsStatementRequest request,
            @Parameter(description = "PDF bill for the accounts statement")
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return accountsStatementService.create(request, file);
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF bill for an existing accounts statement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or non-PDF file"),
            @ApiResponse(responseCode = "404", description = "Accounts statement not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public AccountsStatementResponse upload(
            @PathVariable UUID id,
            @Parameter(description = "PDF file of the accounts statement")
            @RequestParam("file") MultipartFile file) {
        return accountsStatementService.upload(id, file);
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync accounts statements with cash withdrawal balances",
            description = "Automatically links the given accounts statements to available cash withdrawal balances, deducting each statement's amount in date order")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Accounts statements synced successfully"),
            @ApiResponse(responseCode = "400", description = "Not enough cash withdrawal balance to cover the requested accounts statements"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<AccountsStatementResponse> syncWithCashWithdrawals(@RequestBody List<UUID> accountsStatementIds) {
        return accountsStatementService.syncWithCashWithdrawals(accountsStatementIds);
    }
}