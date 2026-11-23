package com.larissafalcao.bff_checkout.domain.model;

import org.jspecify.annotations.NonNull;

import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Card(String holderName, String number, String expiry, String cvv) {

    private static final Pattern NUMBER_FORMAT = Pattern.compile("\\d{13,19}");
    private static final Pattern CVV_FORMAT = Pattern.compile("\\d{3,4}");
    private static final Pattern EXPIRY_FORMAT = Pattern.compile("(0[1-9]|1[0-2])/(\\d{2})");
    private static final int EXPIRY_CENTURY = 2000;
    private static final int VISIBLE_DIGITS = 4;
    private static final String MASK = "****";

    public boolean isValid(YearMonth currentMonth) {
        return hasHolderName() && hasValidNumber() && hasValidCvv() && isNotExpired(currentMonth);
    }

    public String lastDigits() {
        return number.substring(number.length() - VISIBLE_DIGITS);
    }

    public String maskedNumber() {
        boolean hasLastDigits = number != null && number.length() >= VISIBLE_DIGITS;
        return MASK + (hasLastDigits ? lastDigits() : "");
    }

    @Override
    public @NonNull String toString() {
        return "Card[" + maskedNumber() + "]";
    }

    private boolean hasHolderName() {
        return holderName != null && !holderName.isBlank();
    }

    private boolean hasValidNumber() {
        return matches(NUMBER_FORMAT, number);
    }

    private boolean hasValidCvv() {
        return matches(CVV_FORMAT, cvv);
    }

    private boolean isNotExpired(YearMonth currentMonth) {
        if (expiry == null) {
            return false;
        }
        Matcher expiryParts = EXPIRY_FORMAT.matcher(expiry);
        if (!expiryParts.matches()) {
            return false;
        }
        int month = Integer.parseInt(expiryParts.group(1));
        int year = EXPIRY_CENTURY + Integer.parseInt(expiryParts.group(2));
        return !YearMonth.of(year, month).isBefore(currentMonth);
    }

    private static boolean matches(Pattern format, String value) {
        return value != null && format.matcher(value).matches();
    }
}
