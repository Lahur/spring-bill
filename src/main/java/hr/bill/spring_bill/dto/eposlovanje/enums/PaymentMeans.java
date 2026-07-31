package hr.bill.spring_bill.dto.eposlovanje.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Getter
public enum PaymentMeans {
    Cash("10", "Gotovina"),
    CreditTransfer("30", "Kreditni transfer"),
    BankAccount("42", "Uplata na račun"),
    BankCard("48", "Bankovna kartica"),
    DirectDebit("49", "Direktno terećenje"),
    StandingAgreement("57", "Trajni nalog"),
    SepaCredit("58", "SEPA kreditni transfer"),
    SepaDirectDebit("59", "SEPA direktno terećenje");

    private final String code;
    private final String displayName;

    public static PaymentMeans fromCode(String code) {
        return switch (code.trim()) {
            case "10" -> Cash;
            case "30" -> CreditTransfer;
            case "42" -> BankAccount;
            case "48" -> BankCard;
            case "49" -> DirectDebit;
            case "57" -> StandingAgreement;
            case "58" -> SepaCredit;
            case "59" -> SepaDirectDebit;
            default -> null;
        };
    }

    public static List<PaymentMeansEntry> allEntries() {
        return Stream.of(values())
                .map(p -> new PaymentMeansEntry(p.name(), p.getDisplayName()))
                .toList();
    }
}