package hr.bill.spring_bill.dto.eposlovanje.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum VatCategory {
    Pdv25("S", "HR:PDV25", BigDecimal.valueOf(25), null, "PDV 25%"),
    ReverseCharge("AE", "HR:AE", BigDecimal.ZERO, "članak 75. stavka 3.", "Prijenos porezne obveze");

    private final String id;
    private final String vatName;
    private final BigDecimal rate;
    private final String taxExemptionReason;
    private final String displayName;

    public static VatCategory fromId(String id) {
        return switch (id) {
            case "S" -> Pdv25;
            case "AE" -> ReverseCharge;
            default -> null;
        };
    }

    public static List<VatCategoryEntry> allEntries() {
        return Stream.of(values())
                .map(v -> new VatCategoryEntry(v.name(), v.getDisplayName()))
                .toList();
    }
}