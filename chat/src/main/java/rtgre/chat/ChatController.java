package rtgre.chat;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.synedra.validatorfx.Check;
import net.synedra.validatorfx.TooltipWrapper;
import net.synedra.validatorfx.Validator;
import rtgre.chat.graphisme.ContactListViewCell;
import rtgre.modeles.Contact;
import rtgre.modeles.ContactMap;
import rtgre.modeles.Message;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static rtgre.chat.ChatApplication.LOGGER;

public class ChatController implements Initializable {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^([a-z][a-z0-9]{2,7})$");
    private static final Pattern HOST_PATTERN = Pattern.compile("/^[a-z]*((\\:?)\\d{1,5})?$/gm");
    public MenuItem hostAddMenuItem;
    public MenuItem avatarMenuItem;
    public MenuItem aboutMenuItem;
    public ComboBox hostComboBox;
    public TextField loginTextField;
    public ToggleButton connectionButton;
    public ImageView avatarImageView;
    public SplitPane exchangeSplitPane;
    public ListView postListView;
    public ListView roomsListView;
    public ListView contactsListView;
    public TextField messageTextField;
    public Button sendButton;
    public Label statusLabel;
    public Label dateTimeLabel;
    public Contact contact;
    private ContactMap contactMap = new ContactMap();
    private ObservableList<Contact> contactObservableList = FXCollections.observableArrayList();
    Validator validatorLogin = new Validator();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation de l'interface graphique");

        Image image = new Image(Objects.requireNonNull(ChatController.class.getResourceAsStream("anonymous.png")));
        this.avatarImageView.setImage(image);

        Thread dateTimeLoop = new Thread(this::dateTimeLoop);
        dateTimeLoop.setDaemon(true);
        dateTimeLoop.start();

        hostComboBox.getItems().addAll("localhost:2024");
        hostComboBox.getItems().addAll("localhost:2025");
        hostComboBox.setValue("localhost:2024");
        hostComboBox.setOnAction(this::statusNameUpdate);

        statusLabel.setText("not connected to " + hostComboBox.getValue());

        connectionButton.disableProperty().bind(validatorLogin.containsErrorsProperty());
        connectionButton.selectedProperty().addListener(this::handleConnection);
        loginTextField.disableProperty().bind(connectionButton.selectedProperty());
        hostComboBox.disableProperty().bind(connectionButton.selectedProperty());

        avatarMenuItem.setOnAction(this::handleAvatarChange);
        avatarImageView.setOnMouseClicked(this::handleAvatarChange);
        sendButton.setOnAction(this::onActionSend);

        initContactListView();

        validatorLogin.createCheck()
                .dependsOn("login", loginTextField.textProperty())
                .withMethod(this::checkLogin)
                .decorates(loginTextField)
                .immediate();


        /* /!\ Set-up d'environnement de test /!\ */
        /* -------------------------------------- */
        loginTextField.setText("riri");
        connectionButton.setSelected(true);
        /* -------------------------------------- */
    }

    private void onActionSend(ActionEvent actionEvent) {
        String login = getSelectedContactLogin();
        if (login != null) {
            Message message = new Message(login, messageTextField.getText());
            LOGGER.info(message.toString());
        }
    }

    private void handleAvatarChange(Event event) {
        /**
         * Ouvre une fenêtre de dialogue permettant de choisir son avatar
         */
        FileChooser fileChooser = new FileChooser();
        Stage stage = (Stage) avatarImageView.getScene().getWindow();
        fileChooser.setTitle("Select Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            avatarImageView.setImage(new Image(selectedFile.toURI().toString()));
        }
    }



    private void handleConnection(Observable observable) {
        /**
         *
         */
        if (connectionButton.isSelected()) {
            java.awt.Image img = SwingFXUtils.fromFXImage(this.avatarImageView.getImage(), null);
            this.contact = new Contact(loginTextField.getText(), img);
            contactMap.put(this.contact.getLogin(), this.contact);
            LOGGER.info("Nouveau contact : " + contact);
            LOGGER.info(contactMap.toString());
        }
    }

    private void checkLogin(Check.Context context) {
        String login = context.get("login");
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            context.error("Format de login non respecté");
        }
        if (login.equals("system")) {
            context.error("Le login ne peut pas être system");
        }


    }

    private void statusNameUpdate(Event event) {
        statusLabel.setText("not connected to " + hostComboBox.getValue());
    }


    private void dateTimeLoop() {
        while (true) {
            try {
                String datetime = "%1$ta %1$te %1$tb %1$tY - %1$tH:%1$tM".formatted(new Date());
                Platform.runLater(() -> dateTimeLabel.setText(datetime));
                Thread.sleep(60000);
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }
        }

    }

    private void initContactListView() {
        try {
            contactsListView.setCellFactory(contactListView -> new ContactListViewCell());
            contactsListView.setItems(contactObservableList);
            File avatars = new File(getClass().getResource("avatars.png").toURI());
            Contact riri = new Contact("riri", false, avatars);
            Contact fifi = new Contact("fifi", true, avatars);
            contactObservableList.add(riri);
            contactMap.add(riri);
            contactObservableList.add(fifi);
            contactMap.add(fifi);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public String getSelectedContactLogin() {
        Contact contact;
        String login;
        try {
            contact = (Contact) contactsListView.getSelectionModel().getSelectedItem();
            login = contact.getLogin();
        } catch (Exception e) {
            login = null;
        }
        LOGGER.info("Selected login: " + login);
        return login;
    }
}