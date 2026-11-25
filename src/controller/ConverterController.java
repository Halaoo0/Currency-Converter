package controller;

import model.ConverterModel;
import model.Currency;
import model.Money;
import view.ConverterView;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The Controller. Registers the event handlers of the View, parses and
 * validates the raw user input, and calls the model methods. Errors are
 * reported through the view.
 */
public class ConverterController {

    private final ConverterModel model;
    private final ConverterView view;

    public ConverterController(ConverterModel model, ConverterView view) {
        this.model = Objects.requireNonNull(model);
        this.view = Objects.requireNonNull(view);

        view.addConvertListener(e -> onConvert());
        view.addSwapListener(e -> onSwap());
        view.addClearListener(e -> onClear());
        view.addUpdateRateListener(e -> onUpdateRate());
    }

    private void onConvert() {
        run(() -> {
            BigDecimal amount = parseDecimal(view.getAmountText(), "Amount");
            Currency source = new Currency(view.getSourceCode());
            Currency target = new Currency(view.getTargetCode());
            model.convert(new Money(amount, source), target);
        });
    }

    private void onSwap() {
        view.swapSelection();
        if (!view.getAmountText().isBlank()) {
            onConvert();
        } else {
            view.clearError();
        }
    }

    private void onClear() {
        view.clearInputs();
        view.clearError();
        model.clearResult();   // observers refresh the Data View
    }

    private void onUpdateRate() {
        run(() -> {
            BigDecimal rate = parseDecimal(view.getRateText(), "Rate");
            model.setRate(new Currency(view.getRateCode()), rate);
            view.clearRateField();
        });
    }

    /** Runs an action and shows any validation error instead of crashing. */
    private void run(Runnable action) {
        try {
            action.run();
            view.clearError();
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        }
    }

    private static BigDecimal parseDecimal(String text, String name) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number.");
        }
    }
}
