package view;

import model.Currency;
import model.ConverterModel;
import model.ModelObserver;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * The View (the Observer). A very simple Swing window that shows the state of
 * the model and redraws itself whenever modelChanged() is called. It only
 * READS the model and never changes it; user actions are handled by the
 * Controller.
 */
public class ConverterView extends JFrame implements ModelObserver {

    private static final Color BACKGROUND = new Color(240, 240, 240);

    private final ConverterModel model;

    // Data Entry
    private final JTextField amountField = new JTextField(10);
    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();

    // Actions
    private final JButton convertButton = new JButton("Convert");
    private final JButton swapButton = new JButton("Swap");
    private final JButton clearButton = new JButton("Clear");

    // Data View
    private final JLabel resultLabel = new JLabel(" ");
    private final JLabel rateLabel = new JLabel(" ");
    private final JLabel errorLabel = new JLabel(" ");

    // Update Exchange Rate
    private final JComboBox<String> rateCurrencyBox = new JComboBox<>();
    private final JTextField editRateField = new JTextField(8);
    private final JButton updateRateButton = new JButton("Update Rate");

    public ConverterView(ConverterModel model) {
        super("Currency Converter");
        this.model = Objects.requireNonNull(model);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // The four currencies are fixed, so the lists are filled once.
        for (Currency c : model.getCurrencies()) {
            fromBox.addItem(c.getCode());
            toBox.addItem(c.getCode());
            if (!c.equals(ConverterModel.BASE)) {
                rateCurrencyBox.addItem(c.getCode());   // base rate is fixed
            }
        }
        fromBox.setSelectedItem("USD");
        toBox.setSelectedItem("SAR");

        // ---- 1. Data Entry ---------------------------------------------
        JPanel entryPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Data Entry"),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        entryPanel.add(new JLabel("Enter Amount:"));
        entryPanel.add(amountField);
        entryPanel.add(new JLabel("From Currency:"));
        entryPanel.add(fromBox);
        entryPanel.add(new JLabel("To Currency:"));
        entryPanel.add(toBox);

        // ---- 2. Actions -------------------------------------------------
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.add(convertButton);
        actionPanel.add(swapButton);
        actionPanel.add(clearButton);

        // ---- 3. Data View -----------------------------------------------
        errorLabel.setForeground(Color.RED);
        JPanel viewPanel = new JPanel(new GridLayout(0, 2, 6, 6));
        viewPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Data View"),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        viewPanel.add(new JLabel("Converted Amount:"));
        viewPanel.add(resultLabel);
        viewPanel.add(new JLabel("Exchange Rate:"));
        viewPanel.add(rateLabel);
        viewPanel.add(new JLabel("Message:"));
        viewPanel.add(errorLabel);

        // ---- 4. Update Exchange Rate ------------------------------------
        JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        ratePanel.setBorder(BorderFactory.createTitledBorder(
                "Update Exchange Rate (units per 1 " + ConverterModel.BASE + ")"));
        ratePanel.add(rateCurrencyBox);
        ratePanel.add(editRateField);
        ratePanel.add(updateRateButton);

        // ---- put the sections together (BorderLayout) --------------------
        JPanel lower = new JPanel(new BorderLayout(0, 6));
        lower.add(viewPanel, BorderLayout.NORTH);
        lower.add(ratePanel, BorderLayout.CENTER);

        JPanel middle = new JPanel(new BorderLayout(0, 6));
        middle.add(actionPanel, BorderLayout.NORTH);
        middle.add(lower, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(entryPanel, BorderLayout.NORTH);
        root.add(middle, BorderLayout.CENTER);

        setBackgroundRecursively(root);
        setContentPane(root);

        // The rate shown in the Data View follows the selected pair.
        ActionListener selectionChanged = e -> refresh();
        fromBox.addActionListener(selectionChanged);
        toBox.addActionListener(selectionChanged);

        refresh();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private static void setBackgroundRecursively(JPanel panel) {
        panel.setBackground(BACKGROUND);
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                setBackgroundRecursively((JPanel) c);
            }
        }
    }

    // ------------------------------------------------------------------
    // Observer: called by the model after every change
    // ------------------------------------------------------------------

    @Override
    public void modelChanged() {
        refresh();
    }

    private void refresh() {
        resultLabel.setText(model.getCurrentResult().isPresent()
                ? model.getCurrentResult().get().toString()
                : " ");

        String from = (String) fromBox.getSelectedItem();
        String to = (String) toBox.getSelectedItem();
        if (from == null || to == null || from.equals(to)) {
            rateLabel.setText("Choose two different currencies.");
        } else {
            BigDecimal rate = model.getCrossRate(new Currency(from), new Currency(to));
            rateLabel.setText("1 " + from + " = "
                    + rate.stripTrailingZeros().toPlainString() + " " + to);
        }
    }

    // ------------------------------------------------------------------
    // Raw user input (parsed and validated by the Controller)
    // ------------------------------------------------------------------

    public String getAmountText() {
        return amountField.getText();
    }

    public String getSourceCode() {
        return (String) fromBox.getSelectedItem();
    }

    public String getTargetCode() {
        return (String) toBox.getSelectedItem();
    }

    public String getRateCode() {
        return (String) rateCurrencyBox.getSelectedItem();
    }

    public String getRateText() {
        return editRateField.getText();
    }

    // ------------------------------------------------------------------
    // View-only state
    // ------------------------------------------------------------------

    public void swapSelection() {
        Object from = fromBox.getSelectedItem();
        Object to = toBox.getSelectedItem();
        fromBox.setSelectedItem(to);
        toBox.setSelectedItem(from);
    }

    public void clearInputs() {
        amountField.setText("");
        fromBox.setSelectedItem("USD");
        toBox.setSelectedItem("SAR");
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    public void clearError() {
        errorLabel.setText(" ");
    }

    public void clearRateField() {
        editRateField.setText("");
    }

    // ------------------------------------------------------------------
    // Hooks for the Controller
    // ------------------------------------------------------------------

    public void addConvertListener(ActionListener l) {
        convertButton.addActionListener(l);
        amountField.addActionListener(l);   // Enter key
    }

    public void addSwapListener(ActionListener l) {
        swapButton.addActionListener(l);
    }

    public void addClearListener(ActionListener l) {
        clearButton.addActionListener(l);
    }

    public void addUpdateRateListener(ActionListener l) {
        updateRateButton.addActionListener(l);
    }
}
