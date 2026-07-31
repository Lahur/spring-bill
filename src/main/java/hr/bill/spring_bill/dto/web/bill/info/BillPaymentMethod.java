package hr.bill.spring_bill.dto.web.bill.info;

import hr.bill.spring_bill.dto.eposlovanje.enums.PaymentMeans;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Method of paying a bill, merged across B2B and B2C bills")
@RequiredArgsConstructor
@Getter
public enum BillPaymentMethod {
    Cash("Gotovina"),
    CreditTransfer("Kreditni transfer"),
    BankAccount("Uplata na račun"),
    BankCard("Bankovna kartica"),
    DirectDebit("Direktno terećenje"),
    StandingAgreement("Trajni nalog"),
    SepaCredit("SEPA kreditni transfer"),
    SepaDirectDebit("SEPA direktno terećenje"),
    Other("Ostalo");

    private final String displayName;

    public static BillPaymentMethod fromPaymentMeans(PaymentMeans paymentMeans) {
        if (paymentMeans == null) {
            return null;
        }
        return switch (paymentMeans) {
            case Cash -> Cash;
            case CreditTransfer -> CreditTransfer;
            case BankAccount -> BankAccount;
            case BankCard -> BankCard;
            case DirectDebit -> DirectDebit;
            case StandingAgreement -> StandingAgreement;
            case SepaCredit -> SepaCredit;
            case SepaDirectDebit -> SepaDirectDebit;
        };
    }

    public static BillPaymentMethod fromPaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }
        return switch (paymentMethod) {
            case Cash -> Cash;
            case Card -> BankCard;
            case BankTransfer -> CreditTransfer;
            case Other -> Other;
        };
    }
}