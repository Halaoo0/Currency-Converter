package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable ADT: an amount of money in one currency.
 */
public final class Money {

    // Rep:
    private final BigDecimal amount;
    private final Currency currency;

    // RI:
    //   amount != null
    //   amount.signum() >= 0
    //   amount.scale() == 2
    //   currency != null
    //
    // AF(amount, currency) = 'amount' units of 'currency'
    //
    // Rep exposure: all fields are private final and of immutable types
    // (BigDecimal, Currency), so nothing mutable is ever shared.

    /**
     * @param amount   a non-negative amount (rounded to 2 decimals, HALF_UP)
     * @param currency the currency of the amount
     * @throws IllegalArgumentException if amount is negative
     */
    public Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Amount must not be negative.");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency;
        checkRep();
    }

    private void checkRep() {
        assert amount != null;
        assert amount.signum() >= 0;
        assert amount.scale() == 2;
        assert currency != null;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money other = (Money) obj;
        return amount.compareTo(other.amount) == 0
                && currency.equals(other.currency);
    }

    @Override
    public int hashCode() {
        // Consistent with equals(): trailing zeros are removed so that equal
        // amounts always give equal hash codes.
        return Objects.hash(amount.stripTrailingZeros(), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
