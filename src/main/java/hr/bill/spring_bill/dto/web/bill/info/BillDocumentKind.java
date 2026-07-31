package hr.bill.spring_bill.dto.web.bill.info;

import hr.bill.spring_bill.dto.eposlovanje.enums.DocumentType;
import hr.bill.spring_bill.dto.eposlovanje.f1_web.common.ReceiptType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "Kind of bill document, merged across B2B and B2C bills")
@RequiredArgsConstructor
@Getter
public enum BillDocumentKind {
    CommercialInvoice("Račun"),
    CreditNote("Odobrenje"),
    DebitNote("Zaduženje"),
    SelfBilledInvoice("Samofakturiranje"),
    CorrectedInvoice("Ispravljeni račun"),
    Training("Testni"),
    Copy("Kopija");

    private final String displayName;

    public static BillDocumentKind fromDocumentType(DocumentType documentType) {
        if (documentType == null) {
            return null;
        }
        return switch (documentType) {
            case CommercialInvoice -> CommercialInvoice;
            case CreditNote -> CreditNote;
            case DebitNote -> DebitNote;
            case SelfBilledInvoice -> SelfBilledInvoice;
            case CorrectedInvoice -> CorrectedInvoice;
        };
    }

    public static BillDocumentKind fromReceiptType(ReceiptType receiptType) {
        if (receiptType == null) {
            return null;
        }
        return switch (receiptType) {
            case Standard -> CommercialInvoice;
            case Training -> Training;
            case Copy -> Copy;
            case CreditNote -> CreditNote;
        };
    }
}