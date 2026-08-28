package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.pos.PosTransactionGenerateAndSendRequest;
import hr.bill.spring_bill.dto.web.pos.PosTransactionResponse;
import hr.bill.spring_bill.service.PosTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pos-transaction")
@RequiredArgsConstructor
@Tag(name = "POS Transaction", description = "Endpoints for POS transactions")
public class PosTransactionController {

    private final PosTransactionService posTransactionService;

    @GetMapping
    @Operation(summary = "Get all POS transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "POS transactions retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<PosTransactionResponse> findAll() {
        return posTransactionService.findAll();
    }

    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF bill for an existing POS transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or non-PDF file"),
            @ApiResponse(responseCode = "404", description = "POS transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public PosTransactionResponse upload(
            @PathVariable UUID id,
            @Parameter(description = "PDF file of the POS receipt/bill")
            @RequestParam("file") MultipartFile file) {
        return posTransactionService.upload(id, file);
    }
}