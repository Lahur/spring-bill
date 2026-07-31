package hr.bill.spring_bill.dto.eposlovanje.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum DocumentType {
    CommercialInvoice("380", "Račun"),
    CreditNote("381", "Odobrenje"),
    DebitNote("383", "Zaduženje"),
    SelfBilledInvoice("389", "Samofakturiranje"),
    CorrectedInvoice("384", "Ispravljeni račun");

    private final String code;
    private final String displayName;

    public static DocumentType fromCode(String code) {
        return switch (code.trim()) {
            case "380" -> CommercialInvoice;
            case "381" -> CreditNote;
            case "383" -> DebitNote;
            case "384" -> CorrectedInvoice;
            case "389" -> SelfBilledInvoice;
            default -> null;
        };
    }

    public static List<DocumentTypeEntry> allEntries() {
        return Stream.of(CommercialInvoice, CreditNote, DebitNote, SelfBilledInvoice)
                .map(d -> new DocumentTypeEntry(d.name(), d.getDisplayName()))
                .toList();
    }
}