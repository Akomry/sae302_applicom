package rtgre.chat;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
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
import org.json.JSONObject;
import rtgre.chat.graphisme.ContactListViewCell;
import rtgre.chat.graphisme.PostListViewCell;
import rtgre.chat.graphisme.RoomListViewCell;
import rtgre.chat.net.ChatClient;
import rtgre.modeles.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rtgre.chat.ChatApplication.LOGGER;

public class ChatController implements Initializable {

    private static final Pattern LOGIN_PATTERN = Pattern.compile("^([a-z][a-z0-9]{2,7})$");
    private static final Pattern HOST_PATTERN = Pattern.compile("/^[a-z]*((\\:?)\\d{1,5})?$/gm");
    private final Pattern hostPortPattern = Pattern.compile("^([-.a-zA-Z0-9]+)(?::([0-9]{1,5}))?$");
    public MenuItem hostAddMenuItem;
    public MenuItem avatarMenuItem;
    public MenuItem aboutMenuItem;
    public ComboBox<String> hostComboBox;
    public TextField loginTextField;
    public ToggleButton connectionButton;
    public ImageView avatarImageView;
    public SplitPane exchangeSplitPane;
    public ListView postListView;
    public ListView<Room> roomsListView;
    public ListView<Contact> contactsListView;
    public TextField messageTextField;
    public Button sendButton;
    public Label statusLabel;
    public Label dateTimeLabel;
    public Contact contact;
    private ContactMap contactMap = new ContactMap();
    private ObservableList<Contact> contactObservableList = FXCollections.observableArrayList();
    private ObservableList<Post> postsObservableList = FXCollections.observableArrayList();
    private Validator validatorLogin = new Validator();
    private ChatClient client = null;
    private PostVector postVector;
    private RoomMap roomMap = new RoomMap();
    private ObservableList<Room> roomObservableList = FXCollections.observableArrayList();


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

        statusLabel.setText("Disconnected");

        connectionButton.disableProperty().bind(validatorLogin.containsErrorsProperty());
        connectionButton.selectedProperty().addListener(this::handleConnection);
        loginTextField.disableProperty().bind(connectionButton.selectedProperty());
        hostComboBox.disableProperty().bind(connectionButton.selectedProperty());

        avatarMenuItem.setOnAction(this::handleAvatarChange);
        avatarImageView.setOnMouseClicked(this::handleAvatarChange);
        sendButton.setOnAction(this::onActionSend);
        messageTextField.setOnAction(this::onActionSend);

        initContactListView();
        initPostListView();
        initRoomListView();
        contactsListView.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, previous, selected) -> handleContactSelection((Contact) selected));
        roomsListView.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, previous, selected) -> handleRoomSelection((Room) selected));

        validatorLogin.createCheck()
                .dependsOn("login", loginTextField.textProperty())
                .withMethod(this::checkLogin)
                .decorates(loginTextField)
                .immediate();

        ObservableValue<Boolean> canSendCondition = connectionButton.selectedProperty().not()
                .or(
                        roomsListView.getSelectionModel().selectedItemProperty().isNull()
                        .and(contactsListView.getSelectionModel().selectedItemProperty().isNull())
                );

        sendButton.disableProperty().bind(canSendCondition);
        messageTextField.disableProperty().bind(canSendCondition);

        /* /!\ Set-up d'environnement de test /!\ */
        /* -------------------------------------- */
        loginTextField.setText("riri");
        connectionButton.setSelected(true);
        /* -------------------------------------- */
    }

    private void initRoomListView() {
        try {
            roomsListView.setCellFactory(roomListView -> new RoomListViewCell());
            roomsListView.setItems(roomObservableList);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private void onActionSend(ActionEvent actionEvent) {
        String login = null;
        if (!(getSelectedContactLogin() == null)) {
            login = getSelectedContactLogin();
        } else if (!(getSelectedRoomName() == null)) {
            login = getSelectedRoomName();
        }
        if (login != null) {
            Message message = new Message(login, messageTextField.getText());
            LOGGER.info("Sending " + message);
            client.sendMessageEvent(message);
            this.messageTextField.setText("");
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
            contact.setAvatarFromFile(selectedFile);
        }
        client.sendEvent(new rtgre.modeles.Event("CONT", this.contact.toJsonObject()));
    }


    private void handleConnection(Observable observable) {
        if (connectionButton.isSelected()) {
            java.awt.Image img = SwingFXUtils.fromFXImage(this.avatarImageView.getImage(), null);
            this.contact = new Contact(loginTextField.getText(), img);
            contactMap.put(this.contact.getLogin(), this.contact);
            LOGGER.info("Nouveau contact : " + contact);
            LOGGER.info(contactMap.toString());
            Matcher matcher = hostPortPattern.matcher(hostComboBox.getValue());
            matcher.matches();
            String host = matcher.group(1);
            int port = (matcher.group(2) != null) ? Integer.parseInt(matcher.group(2)) : 2024;
            try {
                LOGGER.info(host + ":" + port);
                this.client = new ChatClient(host, port, this);
                initContactListView();
                initPostListView();
                clearLists();
                contactMap.add(this.contact);
                this.contact.setConnected(true);

                client.sendAuthEvent(contact);
                client.sendListRoomEvent();
                client.sendEvent(new rtgre.modeles.Event(rtgre.modeles.Event.LIST_CONTACTS, new JSONObject()));
                client.sendEvent(new rtgre.modeles.Event(rtgre.modeles.Event.CONT, contact.toJsonObject()));
                initContactListView();
                initPostListView();
                this.statusLabel.setText("Connected to %s@%s:%s".formatted(this.contact.getLogin(), host, port));
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "Erreur de connexion").showAndWait();
                connectionButton.setSelected(false);
            }
        } else if (!connectionButton.isSelected()) {
            this.client.sendQuitEvent();
            clearLists();
            if (this.client.isConnected()) {
                this.contact.setConnected(false);
            }
            statusLabel.setText("Disconnected");
        }
    }

    private void clearLists() {
        this.contactMap = new ContactMap();
        this.postVector = new PostVector();
        this.roomMap = new RoomMap();
        contactObservableList.clear();
        postsObservableList.clear();
        roomObservableList.clear();
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
            Contact fifi = new Contact("fifi", true, avatars);
            contactObservableList.add(fifi);
            contactMap.add(fifi);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }
    private void initPostListView() {
        try {
            postListView.setCellFactory(postListView -> new PostListViewCell(this));
            postListView.setItems(postsObservableList);
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

    public String getSelectedRoomName() {
        Room room;
        String roomName;
        try {
            room = (Room) roomsListView.getSelectionModel().getSelectedItem();
            roomName = room.getRoomName();
        } catch (Exception e) {
            roomName = null;
        }
        LOGGER.info("Selected room: " + roomName);
        return roomName;
    }

    public Contact getContact() {
        return contact;
    }

    public ContactMap getContactsMap() {
        return contactMap;
    }

    void handleRoomSelection(Room roomSelected) {

        if (roomSelected != null) {
            LOGGER.info("Clic sur " + roomSelected);
        }

        if (!contactsListView.getSelectionModel().isEmpty()) {
            contactsListView.getSelectionModel().clearSelection();
        }
        contact.setCurrentRoom(roomSelected.getRoomName());

        roomSelected.getUnreadCount().setUnreadCount(0);
        roomsListView.refresh();

        Post postSys = new Post("system", loginTextField.getText(), "Bienvenue dans le salon " + roomSelected);
        postsObservableList.clear();
        postsObservableList.add(postSys);
        client.sendEvent(new rtgre.modeles.Event("JOIN", new JSONObject().put("room", roomSelected.getRoomName())));
        client.sendListPostEvent(0, roomSelected.toString());
        postListView.refresh();
    }

    void handleContactSelection(Contact contactSelected) {
        if (contactSelected != null) {
            LOGGER.info("Clic sur " + contactSelected);
        }

        if (!roomsListView.getSelectionModel().isEmpty()) {
            roomsListView.getSelectionModel().clearSelection();
        }

        contactSelected.getUnreadCount().setUnreadCount(0);

        Post postSys = new Post("system", loginTextField.getText(), "Bienvenue dans la discussion avec " + contactSelected.getLogin());
        postsObservableList.clear();
        postsObservableList.add(postSys);
        client.sendListPostEvent(0, contactSelected.getLogin());
        postListView.refresh();
    }

    public void handleEvent(rtgre.modeles.Event event) {
        LOGGER.info("Received new event! : " + event);
        LOGGER.info(event.getType());
        if (event.getType().equals("CONT")) {
            handleContEvent(event.getContent());
        } else if (event.getType().equals(rtgre.modeles.Event.POST)) {
            handlePostEvent(event.getContent());
        } else if (event.getType().equals(rtgre.modeles.Event.ROOM)) {
            handleRoomEvent(event.getContent());
        } else {
            LOGGER.warning("Unhandled event type: " + event.getType());
            this.client.close();
        }
    }

    private void handleRoomEvent(JSONObject content) {
        LOGGER.info(content.toString());
        Room room = new Room(content.getString("room"));
        roomMap.add(room);
        roomObservableList.add(room);
        roomsListView.refresh();
    }

    private void handlePostEvent(JSONObject content) {

        System.out.println("Selected: " + roomsListView.getSelectionModel().getSelectedItem());
        System.out.println("From: " + content.getString("from"));
        System.out.println("To: " + content.getString("to"));

        try {
            if (!content.getString("to").contains("#")) {
                LOGGER.info("New message to contact!");
                if (contactsListView.getSelectionModel().getSelectedItem().getLogin().equals(content.getString("to"))) {
                    LOGGER.info("New message! to:dm, from:" + content.getString("from"));
                    postVector.add(Post.fromJson(content));
                    postsObservableList.add(Post.fromJson(content));
                    postListView.refresh();
                }
                if (contact.getLogin().equals(content.getString("to"))) {
                    if (contactsListView.getSelectionModel().getSelectedItem().getLogin().equals(content.getString("from"))) {
                        LOGGER.info("New message! to:dm, from:myself");
                        postVector.add(Post.fromJson(content));
                        postsObservableList.add(Post.fromJson(content));
                        postListView.refresh();

                    } else {
                        contactMap.getContact(content.getString("from")).getUnreadCount().incrementUnreadCount();
                        contactsListView.refresh();
                        LOGGER.info("New unread message ! from:" + content.getString("from"));
                        LOGGER.info("%d".formatted(contactsListView.getSelectionModel().getSelectedItem().getUnreadCount().getUnreadCount()));
                    }
                }

            } else {
                LOGGER.info("New message to room!");
                if (roomsListView.getSelectionModel().getSelectedItem().getRoomName().equals(content.getString("to"))) {
                    LOGGER.info("New message! to:room, from:myself");
                    postVector.add(Post.fromJson(content));
                    postsObservableList.add(Post.fromJson(content));
                    postListView.refresh();
                } else {
                    roomMap.get(content.getString("to")).getUnreadCount().incrementUnreadCount();
                    roomsListView.refresh();
                    LOGGER.info("New unread message ! from:" + content.getString("from"));
                    LOGGER.info("%d".formatted(contactsListView.getSelectionModel().getSelectedItem().getUnreadCount().getUnreadCount()));
                }
            }
        } catch (Exception e) {
            if (content.getString("to").contains("#")) {
                roomMap.get(content.getString("to")).getUnreadCount().incrementUnreadCount();
                roomsListView.refresh();
                LOGGER.info("New message to room + nothing sel");
            } else {
                contactMap.getContact(content.getString("from")).getUnreadCount().incrementUnreadCount();
                contactsListView.refresh();
                LOGGER.info("New message to contact + nothing sel");
            }
        }
    }
    private void handleContEvent(JSONObject content) {
        Contact contact = contactMap.getContact(content.getString("login"));
        java.awt.Image avatar = null;
        if (!content.getString("avatar").isEmpty()) {
            avatar = Contact.base64ToImage(content.getString("avatar"));
        }
        System.out.println(avatar);
        if (contact != null) {
            LOGGER.info(contactMap.toString());
            contactMap.getContact(content.getString("login")).setConnected(content.getBoolean("connected"));
            if (avatar != null) {
                contactMap.getContact(content.getString("login")).setAvatar(avatar);
            }
            contactsListView.refresh();
            LOGGER.info(contactMap.toString());
        } else {
            System.out.println(content);
            LOGGER.info(contactMap.toString());
            Contact user = Contact.fromJSON(
                    content,
                    new File("chat/src/main/resources/rtgre/chat/avatars.png")
            );
            if (avatar != null) {
                user.setAvatar(avatar);
            }
            contactMap.add(user);
            contactObservableList.add(user);
            LOGGER.info(contactMap.toString());
        }
    }

}