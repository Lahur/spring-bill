package hr.bill.spring_bill.web;

import hr.bill.spring_bill.model.RecipientEntity;
import hr.bill.spring_bill.service.RecipientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recipient")
@RequiredArgsConstructor
@Tag(name = "Recipient", description = "Endpoints for previously used mail recipient addresses")
public class RecipientController {

    private final RecipientService recipientService;

    @GetMapping
    @Operation(summary = "Get all previously used recipient email addresses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recipient emails retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<String> findAll() {
        return recipientService.findAll().stream().map(RecipientEntity::getEmail).toList();
    }
}
