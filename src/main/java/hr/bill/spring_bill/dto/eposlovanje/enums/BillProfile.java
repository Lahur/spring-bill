package hr.bill.spring_bill.dto.eposlovanje.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum BillProfile {
    P1("P1 - Fakturiranje isporuka po narudžbama na temelju ugovora"),
    P2("P2 - Periodično izdavanje računa"),
    P3("P3 - Izdavanje računa za isporuke prema samostalnoj narudžbi");

    private final String displayName;

    public static List<BillProfileEntry> allEntries() {
        return Stream.of(values())
                .map(p -> new BillProfileEntry(p.name(), p.getDisplayName()))
                .toList();
    }
}
