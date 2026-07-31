package hr.bill.spring_bill.dto.eposlovanje.eposlovanje.response;

public record AccountBalanceResponse(
        String accountType,
        Double balance,
        String balanceExpiresOn
) {}
