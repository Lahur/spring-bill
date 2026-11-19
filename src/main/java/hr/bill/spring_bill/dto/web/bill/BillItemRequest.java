package hr.bill.spring_bill.dto.web.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
@Schema(description = "Single item (line) of a bill")
public record BillItemRequest(

        @Schema(description = "Base amount for bill item", example = "1000.44")
        @NotNull(message = "Base amount can't be null")
        @Positive(message = "Base amount must be positive")
        BigDecimal baseAmount,

        @Schema(description = "Name of the bill item", example = "Parking lot")
        @NotNull(message = "Bill item name must not be null")
        String billItemName,

        @Schema(description = "Description of bill item", example = "Parking lot for Pevex")
        @NotNull(message = "Bill item description must not be null")
        String billItemDescription
) {
}
