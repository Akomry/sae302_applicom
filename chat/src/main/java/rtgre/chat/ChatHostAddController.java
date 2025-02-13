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
import java.util.regex.Pattern;

/**
 * Interface graphique permettant à l'utilisateur de choisir un serveur (hote:port) sur lequel il se souhaite se connecter.
 */
public class ChatHostAddController implements Initializable {

    /** Le champ de saisie de l'hôte */
    public TextField hostTextField;
    /** Bouton de réinitialisation du camp de saisie */
    public Button resetButton;
    /** Wrapper du bouton Submit */
    public HBox submitWrapper;
    /** Bouton Submit */
    public Button submitButton;
    /** Si la vue possède une valeur de retour */
    private boolean ok = false;
    /** Le pattern à satisfaire par un serveur `hote:port` */
    public static final Pattern HOST_PORT_REGEX = Pattern.compile("^([-.a-zA-Z0-9]+)(?::([0-9]{1,5}))?$");
    /** Objet Validator permettant de vérifier la validité d'un serveur `hote:port` */
    private Validator validatorHost = new Validator();
    /** ResourceBundle contenant les textes relatifs aux langues */
    private ResourceBundle i18nBundle;

    /**
     * Initialisation du composant graphique
     * @param url L'url
     * @param resourceBundle Le ResourceBundle contenant les textes relatifs aux langues
     */
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

    /**
     * Vérifie si la valeur de `hostTextField` est conforme.
     * @param context Le contexte de vérification
     */
    private void checkHost(Check.Context context) {
        String host = context.get("host");
        if (!HOST_PORT_REGEX.matcher(host).matches()) {
            context.error(i18nBundle.getString("hostError"));
        }
    }

    /**
     * Callback sur le bouton `Reset`
     * Efface le contenu saisi dans le champ `hostTextField`
     * @param actionEvent L'évènement associé au clic sur le bouton Reset
     */
    private void onActionReset(ActionEvent actionEvent) {
        hostTextField.setText("");
    }

    /**
     * Callback sur le bouton `Add`
     * Ferme la fenêtre pour revenir dans l'application graphique appelante.
     * @param actionEvent L'évènement associé au clic sur le bouton Add
     */
    private void onActionSubmit(ActionEvent actionEvent) {
        ok = true;
        ((Stage) submitButton.getScene().getWindow()).close();
    }

    /**
     * Getter du mode de fermeture de la fenêtre
     * @return la fermeture s'est-elle finie par un clic sur le bouton Send ?
     */
    public boolean isOk() {
        return ok;
    }
}
