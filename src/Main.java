import controller.ConverterController;
import model.ConverterModel;
import view.ConverterView;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ConverterModel model = new ConverterModel();
            ConverterView view = new ConverterView(model);
            model.addObserver(view);                       // Observer registration
            new ConverterController(model, view);
            view.setVisible(true);
        });
    }
}
