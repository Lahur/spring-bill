package hr.bill.spring_bill.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NumberToWordsServiceTest {

    @Test
    void convertsWholeEuros() {
        assertThat(NumberToWordsService.asWords(new BigDecimal("123.00"))).isEqualTo("sto dvadeset tri eura");
    }

    @Test
    void convertsWithCents() {
        assertThat(NumberToWordsService.asWords(new BigDecimal("123.45"))).isEqualTo("sto dvadeset tri eura i četrdeset pet centa");
    }
}