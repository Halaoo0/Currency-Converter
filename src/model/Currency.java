package model;

import java.util.Objects;

/**
 * Immutable ADT: a currency identified by its three-letter code.
 * Used as the key of the exchange-rate Map in the model.
 */
public final class Currency {

    // Rep:
    private final String code;

    // RI:
    //   code != null and code matches [A-Z]{3}
    //
    // AF(code) = the currency whose international three-letter code is 'code'
    //            (e.g. "SAR" = Saudi Riyal)
    //
    // Rep exposure: the only field is a private final String (immutable).

    /**
     * @param code three capital letters, e.g. "USD"
     * @throws IllegalArgumentException if the code is not three capital letters
     */
    public Currency(String code) {
        Objects.requireNonNull(code, "code");
        if (!code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency code must be three capital letters.");
        }
        this.code = code;
        checkRep();
    }

    private void checkRep() {
        assert code != null && code.matches("[A-Z]{3}");
    }

    public String getCode() {
        return code;
    }

    /**
     * Two currencies are equal when they have the same code. This is required
     * because the controller creates NEW Currency objects from the user's
     * selection, and the model must find the matching entry in its HashMap.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency)) return false;
        return code.equals(((Currency) obj).code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return code;
    }
}
