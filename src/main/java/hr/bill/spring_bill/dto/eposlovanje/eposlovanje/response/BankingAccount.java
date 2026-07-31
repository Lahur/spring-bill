package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

public record BankingAccount(
        String id,
        String name,
        String iban,
        String currency,
        String bankName,
        String lastTransactionRetrievedOn,
        String accessExpiresOn,
        Double balance
) {}
