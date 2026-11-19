package hr.bill.spring_bill.dto.web.bill;

import hr.bill.spring_bill.validation.ValidOib;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseBillRequest {

    @Schema(description = "Date when bill was created", example = "2026-06-26")
    @NotNull(message = "Bill date can't be null")
    private LocalDate billDate;

    @Schema(description = "Time when bill was created", example = "11:00:00")
    @NotNull(message = "Bill time can't be null")
    private LocalTime billTime;

    @Schema(description = "Date until bill should be paid", example = "2026-07-15")
    @NotNull(message = "Due date can't be null")
    private LocalDate dueDate;

    @Schema(description = "Additional note for the bill", example = "Some amount added")
    private String note;

    @Schema(description = "Buyers OIB", example = "73660371074")
    @NotNull(message = "Buyer OIB can't be null")
    @ValidOib
    private String buyerOib;

    @Schema(description = "Items (lines) of the bill")
    @NotEmpty(message = "Bill must have at least one item")
    private List<@Valid @NotNull BillItemRequest> items;
}