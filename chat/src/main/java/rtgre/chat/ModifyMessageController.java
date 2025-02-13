package rtgre.chat;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Interface graphique permettant à l'utilisateur de modifier un message.
 */
public class ModifyMessageController implements Initializable {

    /** Le champ de saisie de l'hôte */
    public TextField hostTextField;
    /** Bouton de réinitialisation du camp de saisie */
    public Button resetButton;
    /** Bouton Submit */
    public Button submitButton;
    /** Si la vue possède une valeur de retour */
    private Boolean ok;


    /**
     * Initialisation du composant graphique
     * @param url L'url
     * @param resourceBundle Le ResourceBundle contenant les textes relatifs aux langues
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        resetButton.setOnAction(this::onActionReset);
        submitButton.setOnAction(this::onActionSubmit);
        hostTextField.setOnAction(this::onActionSubmit);
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
