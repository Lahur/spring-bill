package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.BankStatementResponse;
import hr.bill.spring_bill.service.BankStatementImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/bank-statement")
@RequiredArgsConstructor
@Tag(name = "Bank Statement", description = "Endpoints for importing bank statements and reconciling paid bills")
public class BankStatementController {

    private final BankStatementImportService bankStatementImportService;

    @GetMapping
    @Operation(summary = "Get all imported bank statements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bank statements retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankStatementResponse> findAll() {
        return bankStatementImportService.findAll();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload one or more camt.053 bank statement files (.xml or .zip archives of .xml files)",
            description = "Parses each statement, saves its transactions, and marks any matching bills as fully paid.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statements imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or unparseable file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<BankStatementResponse> upload(
            @Parameter(description = "Bank statement files (.xml) or .zip archives containing them")
            @RequestParam("files") List<MultipartFile> files) {
        return bankStatementImportService.importStatements(files);
    }
}
