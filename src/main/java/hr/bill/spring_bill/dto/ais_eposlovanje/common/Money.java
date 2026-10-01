package hr.bill.spring_bill.dto.ais_eposlovanje.common;

/** Berlin Group PSD2 amount shape, passed through by the bank verbatim. */
public record Money(String amount, String currency) {}
