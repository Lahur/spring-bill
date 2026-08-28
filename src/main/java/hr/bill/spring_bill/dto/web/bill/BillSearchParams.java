package hr.bill.spring_bill.dto.web.bill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Past;
import lombok.Builder;

import java.time.LocalDate;

@Schema(description = "Date range filter for searching bills")
@Builder
public record BillSearchParams(

        @Schema(description = "Start date (inclusive); omit for no lower bound", example = "2026-06-01")
        LocalDate dateFrom,

        @Schema(description = "End date (inclusive); omit for no upper bound", example = "2026-06-30")
        @Past(message = "dateTill must be before today")
        LocalDate dateTill
) {
    @AssertTrue(message = "dateFrom must not be after dateTill")
    @Schema(hidden = true)
    public boolean isValidRange() {
        return dateFrom == null || dateTill == null || !dateFrom.isAfter(dateTill);
    }
}
