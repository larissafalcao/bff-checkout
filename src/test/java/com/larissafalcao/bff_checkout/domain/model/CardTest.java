package com.larissafalcao.bff_checkout.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class CardTest {

    static final YearMonth NOW = YearMonth.of(2026, 10);

    @ParameterizedTest(name = "[{index}] {4}")
    @CsvSource(nullValues = "null", value = {
            "Ana Souza, 4111111111114242,    12/28, 123,  'cartão válido'",
            "Ana Souza, 4111111111114242,    10/26, 123,  'vence no mês corrente'",
            "Ana Souza, 4111111111111,       12/28, 1234, '13 dígitos e CVV de 4'",
            "Ana Souza, 4111111111111111111, 12/28, 123,  '19 dígitos'"
    })
    void acceptsValidCards(String holder, String number, String expiry, String cvv, String scenario) {
        assertThat(new Card(holder, number, expiry, cvv).isValid(NOW)).as(scenario).isTrue();
    }

    @ParameterizedTest(name = "[{index}] {4}")
    @CsvSource(nullValues = "null", value = {
            "null,      4111111111114242,     12/28, 123,  'sem titular'",
            "'  ',      4111111111114242,     12/28, 123,  'titular em branco'",
            "Ana Souza, null,                 12/28, 123,  'sem número'",
            "Ana Souza, 411111111111,         12/28, 123,  'número com 12 dígitos'",
            "Ana Souza, 41111111111111111111, 12/28, 123,  'número com 20 dígitos'",
            "Ana Souza, 4111-1111-1111-4242,  12/28, 123,  'número com separadores'",
            "Ana Souza, 4111111111114242,     null,  123,  'sem validade'",
            "Ana Souza, 4111111111114242,     13/28, 123,  'mês 13'",
            "Ana Souza, 4111111111114242,     00/28, 123,  'mês 00'",
            "Ana Souza, 4111111111114242,     12/2028, 123, 'ano com 4 dígitos'",
            "Ana Souza, 4111111111114242,     09/26, 123,  'vencido no mês anterior'",
            "Ana Souza, 4111111111114242,     01/20, 123,  'vencido há anos'",
            "Ana Souza, 4111111111114242,     12/28, null, 'sem CVV'",
            "Ana Souza, 4111111111114242,     12/28, 12,   'CVV com 2 dígitos'",
            "Ana Souza, 4111111111114242,     12/28, 12345, 'CVV com 5 dígitos'"
    })
    void rejectsInvalidCards(String holder, String number, String expiry, String cvv, String scenario) {
        assertThat(new Card(holder, number, expiry, cvv).isValid(NOW)).as(scenario).isFalse();
    }

    @Test
    void toStringNeverExposesNumberOrCvv() {
        String text = new Card("Ana Souza", "4111111111114242", "12/28", "987").toString();

        assertThat(text).isEqualTo("Card[****4242]").doesNotContain("411111").doesNotContain("987");
        assertThat(new Card("Ana", null, null, null).toString()).isEqualTo("Card[****]");
    }

    @ParameterizedTest(name = "[{index}] {0} → {1}")
    @CsvSource(nullValues = "null", value = {
            "4111111111114242, ****4242",
            "4242,             ****4242",
            "424,              ****",
            "null,             ****"
    })
    void maskedNumberShowsOnlyLastFourDigits(String number, String expected) {
        assertThat(new Card("Ana", number, "12/35", "123").maskedNumber()).isEqualTo(expected);
    }
}
