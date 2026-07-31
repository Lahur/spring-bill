package hr.bill.spring_bill.service;

import pl.allegro.finance.tradukisto.ValueConverters;

import java.math.BigDecimal;
import java.util.Locale;

public class NumberToWordsService {

    private static final ValueConverters CROATIAN =
            ValueConverters.getByLocaleOrDefault(Locale.forLanguageTag("hr"), ValueConverters.ENGLISH_INTEGER);

    public static String asWords(BigDecimal amount) {
        BigDecimal absAmount = amount.abs();
        int euros = absAmount.intValue();
        int cents = absAmount.subtract(BigDecimal.valueOf(euros))
                .multiply(BigDecimal.valueOf(100))
                .intValue();
        String result = CROATIAN.asWords(euros) + " eura";
        if (cents > 0) {
            result += " i " + CROATIAN.asWords(cents) + " centa";
        }
        return result;
    }
}