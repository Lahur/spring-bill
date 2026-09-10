package hr.bill.spring_bill.web;

import hr.bill.spring_bill.dto.web.SendBillReportsRequest;
import hr.bill.spring_bill.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Endpoints for generating and sending bill documents")
public class DocumentController {

    // Resend caps a single email at a handful of attachments; keep individual
    // sends small so the mail is actually delivered.
    private static final int MAX_INDIVIDUAL_FILES = 6;

    private final DocumentService documentService;

    @PostMapping("/generate-and-send")
    @Operation(summary = "Generate bill documents and send them by email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents generated and sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> generateAndSend(@RequestBody @Validated SendBillReportsRequest request) {
        if (request.separated() && request.reports().size() > MAX_INDIVIDUAL_FILES) {
            throw new IllegalArgumentException(
                    "Cannot send more than " + MAX_INDIVIDUAL_FILES + " files individually; "
                            + "enable merging or reduce the number of reports");
        }
        documentService.generateAndSendDocuments(request);
        return ResponseEntity.ok().build();
    }
}
