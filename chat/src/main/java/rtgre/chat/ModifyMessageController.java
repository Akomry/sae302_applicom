package rtgre.chat;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ModifyMessageController implements Initializable {


    public TextField hostTextField;
    public Button resetButton;
    public Button submitButton;
    private Boolean ok;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetButton.setOnAction(this::onActionReset);
        submitButton.setOnAction(this::onActionSubmit);
        hostTextField.setOnAction(this::onActionSubmit);
    }

    private void onActionReset(ActionEvent actionEvent) {
        hostTextField.setText("");
    }

    private void onActionSubmit(ActionEvent actionEvent) {
        ok = true;
        ((Stage) submitButton.getScene().getWindow()).close();
    }

    public boolean isOk() {
        return ok;
    }


}
