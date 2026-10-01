package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.service.BankStatementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bank-statement")
@RequiredArgsConstructor
@Tag(name = "Bank Statement", description = "Endpoints for importing bank statements and reconciling paid bills")
public class BankStatementController {

    private final BankStatementService bankStatementService;

    @GetMapping
    @Operation(summary = "Get all imported bank statements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank statements retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankStatementResponse> findAll() {
        return bankStatementService.findAll();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload one or more camt.053 bank statement files (.xml or .zip archives of .xml files)",
            description = "Parses each statement, saves its transactions, and marks any matching bills as fully paid. "
                    + "When an email address is supplied, the freshly imported statements are rendered to a single "
                    + "PDF and emailed to that address.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statements imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or unparseable file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankStatementResponse> upload(
            @Parameter(description = "Bank statement files (.xml) or .zip archives containing them")
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Optional email address to send the imported statements to as a PDF")
            @RequestParam(value = "email", required = false) String email) {
        return bankStatementService.importStatements(files, email);
    }

    @PostMapping("/fetch")
    @Operation(summary = "Import bank statements from the AIS open banking API",
            description = "Fetches booked transactions of the linked bank account and saves one statement per booking day "
                    + "between dateFrom and dateTill (inclusive), then marks any matching bills as paid. "
                    + "Days that are already imported are skipped. dateTill must be before today.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statements imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "404", description = "No linked bank account"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankStatementResponse> fetch(
            @Parameter(description = "First booking day to import (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = "Last booking day to import (yyyy-MM-dd), inclusive")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTill) {
        return bankStatementService.getTransactions(dateFrom, dateTill);
    }
}
