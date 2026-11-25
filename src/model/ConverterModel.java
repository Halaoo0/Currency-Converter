package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The Model (and the Subject of the Observer pattern).
 * Stores the exchange rates of four fixed currencies, performs the
 * conversion and validates its inputs. It has no dependency on Swing.
 *
 * Rates are stored as "units of the currency per 1 USD".
 */
public class ConverterModel {

    public static final Currency BASE = new Currency("USD");
    private static final int RATE_SCALE = 8;

    // Rep:
    private final Map<Currency, BigDecimal> rates = new LinkedHashMap<>();
    private final List<ModelObserver> observers = new ArrayList<>();
    private Money lastSource;       // last successful conversion request
    private Currency lastTarget;

    // RI:
    //   rates has exactly 4 entries and rates.get(BASE) equals 1
    //   every value of rates is > 0
    //   (lastSource == null) == (lastTarget == null)
    //   if lastSource != null, its currency and lastTarget are keys of rates
    //     and they are different
    //
    // AF: a converter for the four currencies in 'rates' (units per 1 USD)
    //     that remembers the last requested conversion.

    public ConverterModel() {
        rates.put(BASE, BigDecimal.ONE);
        rates.put(new Currency("SAR"), new BigDecimal("3.75"));
        rates.put(new Currency("AED"), new BigDecimal("3.67"));
        rates.put(new Currency("EUR"), new BigDecimal("0.92"));
        checkRep();
    }

    private void checkRep() {
        assert rates.size() == 4;
        assert rates.get(BASE).compareTo(BigDecimal.ONE) == 0;
        for (BigDecimal r : rates.values()) {
            assert r.signum() > 0;
        }
        assert (lastSource == null) == (lastTarget == null);
        if (lastSource != null) {
            assert rates.containsKey(lastSource.getCurrency());
            assert rates.containsKey(lastTarget);
            assert !lastSource.getCurrency().equals(lastTarget);
        }
    }

    // ------------------------------------------------------------------
    // Observer pattern (Subject side)
    // ------------------------------------------------------------------

    public void addObserver(ModelObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    public void removeObserver(ModelObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (ModelObserver o : new ArrayList<>(observers)) {
            o.modelChanged();
        }
    }

    // ------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------

    /**
     * Converts 'source' into the 'target' currency, remembers the request
     * and notifies the observers.
     *
     * @throws IllegalArgumentException if a currency is unknown or the two
     *                                  currencies are the same
     */
    public Money convert(Money source, Currency target) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        BigDecimal rate = getCrossRate(source.getCurrency(), target);
        Money result = new Money(source.getAmount().multiply(rate), target);

        lastSource = source;
        lastTarget = target;
        checkRep();
        notifyObservers();
        return result;
    }

    /** Forgets the last requested conversion and notifies the observers. */
    public void clearResult() {
        lastSource = null;
        lastTarget = null;
        checkRep();
        notifyObservers();
    }

    /**
     * Sets the rate (units per 1 USD) of a currency.
     *
     * @throws IllegalArgumentException if the currency is unknown, is the
     *                                  base currency, or rate <= 0
     */
    public void setRate(Currency currency, BigDecimal rate) {
        requireKnown(currency);
        if (currency.equals(BASE)) {
            throw new IllegalArgumentException(
                    "The base currency " + BASE + " cannot be changed.");
        }
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Rate must be greater than zero.");
        }
        rates.put(currency, rate);
        checkRep();
        notifyObservers();
    }

    // ------------------------------------------------------------------
    // Queries (used by the View)
    // ------------------------------------------------------------------

    /** @return the four supported currencies (a copy) */
    public List<Currency> getCurrencies() {
        return List.copyOf(rates.keySet());
    }

    /** @return how many units of 'to' one unit of 'from' is worth */
    public BigDecimal getCrossRate(Currency from, Currency to) {
        requireKnown(from);   // the lookups below rely on Currency.equals/hashCode
        requireKnown(to);
        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "Source and target currencies must be different.");
        }
        return rates.get(to).divide(rates.get(from), RATE_SCALE, RoundingMode.HALF_UP);
    }

    /** @return the last requested amount, if any */
    public Optional<Money> getLastSource() {
        return Optional.ofNullable(lastSource);
    }

    /**
     * @return the result of the last requested conversion computed with the
     *         CURRENT rates, so it follows rate changes automatically
     */
    public Optional<Money> getCurrentResult() {
        if (lastSource == null) {
            return Optional.empty();
        }
        BigDecimal rate = getCrossRate(lastSource.getCurrency(), lastTarget);
        return Optional.of(new Money(lastSource.getAmount().multiply(rate), lastTarget));
    }

    // ------------------------------------------------------------------

    private void requireKnown(Currency currency) {
        if (currency == null || !rates.containsKey(currency)) {
            throw new IllegalArgumentException("Unknown currency: " + currency);
        }
    }
}
