package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum DocumentStatus {
    NaCekanju(1, "Na čekanju"),
    NaSlanju(2, "Na slanju"),
    Poslan(3, "Poslan"),
    Isporucen(4, "Isporučen"),
    Odobren(5, "Odobren"),
    Odbijen(6, "Odbijen"),
    PlacenUPotpunosti(7, "Plaćen u potpunosti"),
    ParcijalnoPlacen(8, "Parcijalno plaćen"),
    NeuspjesnaIsporuka(9, "Neuspješna isporuka");

    private final int value;
    private final String displayName;

    @JsonCreator
    public static DocumentStatus fromValue(String value) {
        int intValue = Integer.parseInt(value);
        for (DocumentStatus s : values()) {
            if (s.value == intValue) return s;
        }
        throw new IllegalArgumentException("Invalid DocumentStatus value: " + value);
    }

    public static List<DocumentStatusEntry> allEntries() {
        return Stream.of(values())
                .map(s -> new DocumentStatusEntry(s.getValue(), s.getDisplayName()))
                .toList();
    }
}