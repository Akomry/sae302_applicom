package rtgre.chat;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.synedra.validatorfx.Check;
import net.synedra.validatorfx.TooltipWrapper;
import net.synedra.validatorfx.Validator;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatHostAddController implements Initializable {


    public TextField hostTextField;
    public Button resetButton;
    public HBox submitWrapper;
    public Button submitButton;
    private boolean ok = false;
    public static final Pattern HOST_PORT_REGEX = Pattern.compile("^([-.a-zA-Z0-9]+)(?::([0-9]{1,5}))?$");
    private Validator validatorHost = new Validator();
    private ResourceBundle i18nBundle;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        submitButton.setOnAction(this::onActionSubmit);
        resetButton.setOnAction(this::onActionReset);
        this.i18nBundle = resourceBundle;

        submitButton.disableProperty().bind(validatorHost.containsErrorsProperty());
        TooltipWrapper<Button> submitWrapper = new TooltipWrapper<>(
                submitButton,
                validatorHost.containsErrorsProperty(),
                Bindings.concat(i18nBundle.getString("cannotSubmit"), validatorHost.createStringBinding())
        );
        this.submitWrapper.getChildren().add(submitWrapper);

        validatorHost.createCheck()
                .dependsOn("host", hostTextField.textProperty())
                .withMethod(this::checkHost)
                .decorates(hostTextField)
                .immediate();
    }

    private void checkHost(Check.Context context) {
        String host = context.get("host");
        if (!HOST_PORT_REGEX.matcher(host).matches()) {
            context.error(i18nBundle.getString("hostError"));
        }
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
