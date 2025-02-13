package rtgre.chat;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.synedra.validatorfx.Check;
import net.synedra.validatorfx.Validator;
import org.json.JSONObject;
import rtgre.chat.graphisme.ContactListViewCell;
import rtgre.chat.graphisme.PostListViewCell;
import rtgre.chat.graphisme.RoomListViewCell;
import rtgre.chat.net.ChatClient;
import rtgre.modeles.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Controller de l'application de chat
 *
 */
public class ChatController implements Initializable {

    /** Pattern associé à une regex pour vérifier si le login est correct */
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^([a-z][a-z0-9]{2,7})$");
    /** Pattern associé à une regex pour récupérer le nom d'hôte et du port à partir d'un socket */
    private final Pattern hostPortPattern = Pattern.compile("^([-.a-zA-Z0-9]+)(?::([0-9]{1,5}))?$");
    /** Menu `Add Host` */
    public MenuItem hostAddMenuItem;
    /** Menu `Change avatar` */
    public MenuItem avatarMenuItem;
    /** Menu `About` */
    public MenuItem aboutMenuItem;
    /** Liste déroulante des serveurs disponibles */
    public ComboBox<String> hostComboBox;
    /** Zone de saisie du login */
    public TextField loginTextField;
    /** Bouton de connexion */
    public ToggleButton connectionButton;
    /** ImageView de l'avatar */
    public ImageView avatarImageView;
    /** Séparateur des messages/destainataires */
    public SplitPane exchangeSplitPane;
    /** Vue des messages */
    public ListView<Post> postListView;
    /** Vue des salons */
    public ListView<Room> roomsListView;
    /** Vue des contacts */
    public ListView<Contact> contactsListView;
    /** Zone de saisie du message */
    public TextField messageTextField;
    /** Bouton d'envoi du message */
    public Button sendButton;
    /** Statut */
    public Label statusLabel;
    /** Horodatage */
    public Label dateTimeLabel;
    /** Contact associé à l'utilisateur courant */
    public Contact contact;
    /** Séparateur des salons/contacts */
    public SplitPane senderSplitPane;
    /** Annuaire des contacts */
    private ContactMap contactMap = new ContactMap();
    /** Liste observable associée aux contacts */
    private ObservableList<Contact> contactObservableList = FXCollections.observableArrayList();
    /** Liste observable associée aux messages */
    private ObservableList<Post> postsObservableList = FXCollections.observableArrayList();
    /** Instance de validateur permettant de vérifier la validité du login à partir du pattern LOGIN_PATTERN */
    private Validator validatorLogin = new Validator();
    /** Client de connexion */
    private ChatClient client = null;
    /** Liste des messages */
    private PostVector postVector;
    /** Liste des salons */
    private RoomMap roomMap = new RoomMap();
    /** Liste observable associée aux salons */
    private ObservableList<Room> roomObservableList = FXCollections.observableArrayList();
    /** ResourceBundle contenant les textes en fonction des langues */
    private ResourceBundle i18nBundle;
    /** Propriétés chargées à partir d'un fichier `config.properties` */
    private Properties properties = new Properties();
    /** Menu contextuel associé à un clic droit sur un message */
    private ContextMenu contextMenu = new ContextMenu();
    /** Menu `removeItem` */
    private MenuItem removeMenuItem;
    /** Menu `editMenuItem` */
    private MenuItem editMenuItem;
    /** Menu `cancelMenuItem` */
    private MenuItem cancelMenuItem;

    /**
     * Initialisation du composant graphique
     * @param url L'url
     * @param resourceBundle Le ResourceBundle contenant les textes relatifs aux langues
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation de l'interface graphique");
        Image image = new Image(Objects.requireNonNull(ChatController.class.getResourceAsStream("anonymous.png")));
        this.avatarImageView.setImage(image);
        this.i18nBundle = resourceBundle;

        try {
            InputStream in = ChatController.class.getResourceAsStream("config.properties");
            System.out.println(ChatController.class.getResource("config.properties").getPath());
            properties.load(in);
            if (contact != null) {
                this.contact.setAvatar(Contact.base64ToImage(properties.getProperty("avatar")));
            }
            this.avatarImageView.setImage(SwingFXUtils.toFXImage(Contact.base64ToBufferedImage(properties.getProperty("avatar")), null));
            for (String host : (String[]) properties.getProperty("hosts").split(",")) {
                host = host.replace("[", "").replace("]", "").replace(" ", "");
                hostComboBox.getItems().addAll(host);
            }
            hostComboBox.setValue(!properties.getProperty("lasthost").isEmpty() ? properties.getProperty("lasthost") : hostComboBox.getItems().get(0));
            loginTextField.setText(!properties.getProperty("login").isEmpty() ? properties.getProperty("login") : "");
            if (!properties.getProperty("split2").isEmpty()) {
                exchangeSplitPane.setDividerPositions(Double.parseDouble(properties.getProperty("split2")));
            }
            if (!properties.getProperty("split1").isEmpty()) {
                exchangeSplitPane.setDividerPositions(Double.parseDouble(properties.getProperty("split2")));
            }

        } catch (IOException e) {
            LOGGER.warning("Impossible de charger le fichier de configuration! Configuration par défaut chargée");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            LOGGER.warning("Impossible de charger le fichier de configuration! Configuration par défaut chargée");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        Thread dateTimeLoop = new Thread(this::dateTimeLoop);
        dateTimeLoop.setDaemon(true);
        dateTimeLoop.start();

        hostComboBox.setOnAction(this::statusNameUpdate);

        statusLabel.setText(i18nBundle.getString("disconnected"));

        connectionButton.disableProperty().bind(validatorLogin.containsErrorsProperty());
        connectionButton.selectedProperty().addListener(this::handleConnection);
        loginTextField.disableProperty().bind(connectionButton.selectedProperty());
        hostComboBox.disableProperty().bind(connectionButton.selectedProperty());

        hostAddMenuItem.setOnAction(this::handleHostAdd);
        avatarMenuItem.setOnAction(this::handleAvatarChange);
        avatarImageView.setOnMouseClicked(this::handleAvatarChange);
        sendButton.setOnAction(this::onActionSend);
        messageTextField.setOnAction(this::onActionSend);

        initContextMenu();
        postListView.setOnContextMenuRequested(this::handleContextMenu);

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

    }

    /**
     * Affiche le menu contextuel lors d'un clic droit sur un message dans la PostListView
     *
     * @param e L'évènement associé à un clic droit sur la PostListView
     */
    private void handleContextMenu(ContextMenuEvent e) {
        if (postListView.getSelectionModel().getSelectedItem().getFrom().equals(contact.getLogin())) {
            contextMenu.show(postListView, e.getScreenX(), e.getScreenY());
        }
    }

    /**
     * Initialise le menu de contexte lors d'un clic droit sur un message dans la PostListView
     *
     */
    private void initContextMenu() {
        this.removeMenuItem = new MenuItem();
        this.editMenuItem = new MenuItem();
        this.cancelMenuItem = new MenuItem();

        removeMenuItem.setText("Remove message");
        editMenuItem.setText("Edit message");
        cancelMenuItem.setText("Cancel");

        removeMenuItem.setOnAction(this::onMessageRemove);
        editMenuItem.setOnAction(this::onMessageEdit);
        cancelMenuItem.setOnAction(e -> contextMenu.hide());

        contextMenu.getItems().addAll(removeMenuItem, editMenuItem, cancelMenuItem);
    }


    /**
     * Supprime le message sélectionné par le menu contextuel, affiche une vue de modification du message, puis envoie
     * un événement POST associé au nouveau message.
     * Conserve l'`UUID`, le `timestamp`, les champs `from` et `to`
     * @param actionEvent L'évènement lié au bouton "Edit message" dans le menu
     */
    private void onMessageEdit(ActionEvent actionEvent) {
        UUID postUUID = postListView.getSelectionModel().getSelectedItem().getId();
        long timestamp = postListView.getSelectionModel().getSelectedItem().getTimestamp();
        String from = postListView.getSelectionModel().getSelectedItem().getFrom();
        String to = postListView.getSelectionModel().getSelectedItem().getTo();
        try {
            ModifyMessageController controller = showNewStage(i18nBundle.getString("messageEdit"), "modifymessage-view.fxml");

            Post post = new Post(postUUID, timestamp, from, to, controller.hostTextField.getText());
            client.sendPostEvent(post);
            postVector.remove(postListView.getSelectionModel().getSelectedItem());
            postsObservableList.remove(postListView.getSelectionModel().getSelectedItem());
            postListView.refresh();

        } catch (IOException e) {
            LOGGER.warning("Can't open modify message view!");
        }
    }

    /**
     * Supprime le message sélectionné par le menu contextuel, et envoie un nouvel évènement POST associé au message
     * de remplacement
     * Conserve l'`UUID`, le `timestamp`, les champs `from` et `to`
     * @param actionEvent L'évènement lié au bouton "Delete message" dans le menu
     */
    private void onMessageRemove(ActionEvent actionEvent) {
        UUID postUUID = postListView.getSelectionModel().getSelectedItem().getId();
        long timestamp = postListView.getSelectionModel().getSelectedItem().getTimestamp();
        String from = postListView.getSelectionModel().getSelectedItem().getFrom();
        String to = postListView.getSelectionModel().getSelectedItem().getTo();
        client.sendPostEvent(new Post(postUUID, timestamp, from, to, "Ce message a été supprimé."));
        postVector.remove(postListView.getSelectionModel().getSelectedItem());
        postsObservableList.remove(postListView.getSelectionModel().getSelectedItem());
        postListView.refresh();
    }

    /**
     * Ouvre une fenêtre de dialogue permettant d'ajouter un hôte à la liste des serveurs.
     * @param actionEvent L'évènement lié au bouton "Add Host" dans le menu
     */
    private void handleHostAdd(ActionEvent actionEvent) {
        try {
            ChatHostAddController controller = showNewStage(i18nBundle.getString("addHost"), "chathostadd-view.fxml");
            if (controller.isOk()) {
                hostComboBox.getItems().add(controller.hostTextField.getText());
                hostComboBox.setValue(controller.hostTextField.getText());
                properties.setProperty("hosts", hostComboBox.getItems().toString());
                properties.store(new FileOutputStream(getClass().getResource("config.properties").getPath()), null);
            }
        } catch (IOException e) {
            LOGGER.warning("Impossible d'ouvrir la fenêtre de dialogue: fxml introuvable \n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ouvre une fenêtre modale et attend qu'elle se ferme.
     *
     * @param title titre de la fenêtre
     * @param fxmlFileName nom du fichier FXML décrivant l'interface graphique
     * @return l'objet contrôleur associé à la fenêtre
     * @throws IOException si le fichier FXML n'est pas trouvé dans les ressources
     */
    public <T> T showNewStage(String title, String fxmlFileName) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource(fxmlFileName), i18nBundle);
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
        return fxmlLoader.getController();
    }

    /**
     * Initialise RoomListView avec sa CelFactory et sa liste observable
     */
    private void initRoomListView() {
        try {
            roomsListView.setCellFactory(roomListView -> new RoomListViewCell());
            roomsListView.setItems(roomObservableList);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Envoie au serveur un message à destination du contact sélectionné, contenant le texte du champ éditable des messages
     * @param actionEvent L'évènement lié au bouton Send ou à l'appui sur `Entrée` dans le champ éditable des messages
     */
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

    /**
     * Ouvre une fenêtre de dialogue permettant de choisir son avatar à partir d'un fichier image
     * @param event L'évènement lié au clic sur l'avatar ou sur le bouton `"Change avatar"` dans le menu
     */
    private void handleAvatarChange(Event event) {
        try {
            FileChooser fileChooser = new FileChooser();
            Stage stage = (Stage) avatarImageView.getScene().getWindow();
            fileChooser.setTitle(i18nBundle.getString("changeAvatar"));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                avatarImageView.setImage(new Image(selectedFile.toURI().toString()));
                if (contact != null) {
                    contact.setAvatar(ImageIO.read(selectedFile));
                }
                properties.setProperty("avatar", Contact.imageToBase64(ImageIO.read(selectedFile)));
                properties.store(new FileOutputStream(getClass().getResource("config.properties").getPath()), null);

            }
        } catch (IOException e) {
            LOGGER.warning("Impossible de lire l'image!");
        }
        try {
            client.sendEvent(new rtgre.modeles.Event("CONT", this.contact.toJsonObject()));
        } catch (Exception e) {
            LOGGER.warning("Impossible d'envoyer l'évenement CONT! L'utilisateur est-il connecté?");
        }
    }

    /**
     * Connexion au serveur
     * @param observable L'évènement lié au clic sur le bouton Connexion/Déconnexion
     */
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

                client.sendAuthEvent(contact);
                this.contact.setConnected(true);
                client.sendListRoomEvent();
                client.sendEvent(new rtgre.modeles.Event(rtgre.modeles.Event.LIST_CONTACTS, new JSONObject()));
                client.sendEvent(new rtgre.modeles.Event(rtgre.modeles.Event.CONT, contact.toJsonObject()));
                initContactListView();
                initPostListView();
                this.statusLabel.setText("%s%s@%s:%s".formatted(i18nBundle.getString("connected"), this.contact.getLogin(), host, port));
                this.connectionButton.setText(i18nBundle.getString("disconnect"));

                try {
                    properties.setProperty("login", loginTextField.getText());
                    properties.store(new FileOutputStream(getClass().getResource("config.properties").getPath()), null);
                } catch (Exception e) {
                    LOGGER.warning("Unable to store login in config!");
                }

            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, i18nBundle.getString("connectionError")).showAndWait();
                connectionButton.setSelected(false);
            }
        } else if (!connectionButton.isSelected()) {
            this.client.sendQuitEvent();
            clearLists();
            if (this.client.isConnected()) {
                this.contact.setConnected(false);
            }
            statusLabel.setText(i18nBundle.getString("disconnected"));
            this.connectionButton.setText(i18nBundle.getString("connect"));
        }
    }

    /**
     * Vide toutes les maps, les vecteurs et les listes observables.
     */
    private void clearLists() {
        this.contactMap = new ContactMap();
        this.postVector = new PostVector();
        this.roomMap = new RoomMap();
        contactObservableList.clear();
        postsObservableList.clear();
        roomObservableList.clear();
    }

    /**
     * Vérifie si le login est conforme ou s'il n'est pas égal à `"system"`
     * @param context Le contexte de vérification
     */
    private void checkLogin(Check.Context context) {
        String login = context.get("login");
        if (!LOGIN_PATTERN.matcher(login).matches()) {
            context.error(i18nBundle.getString("loginError"));
        }
        if (login.equals("system")) {
            context.error(i18nBundle.getString("systemError"));
        }


    }

    /**
     * Met à jour le label de statut situé en bas à gauche de l'application en fonction de si
     * l'utilisateur est connecté à un serveur, et si oui, son login et le socket de connexion du serveur
     * @param event L'évènement lié à l'interaction avec la HostComboBox
     */
    private void statusNameUpdate(Event event) {
        statusLabel.setText("not connected to " + hostComboBox.getValue());

        properties.setProperty("lasthost", hostComboBox.getValue());
        try {
            properties.store(new FileOutputStream(getClass().getResource("config.properties").getPath()), null);
        } catch (IOException e) {
            LOGGER.warning("Unable to write last host to config!");
        }
    }

    /**
     * Gestion de l'horloge : affichage de la date courante toutes les secondes dans le label dateTimeLabel`
     *
     */
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

    /**
     * Initialise la CellFactory et la liste observable associée à la ContactListView
     */
    private void initContactListView() {
        try {
            contactsListView.setCellFactory(contactListView -> new ContactListViewCell());
            contactsListView.setItems(contactObservableList);
            //File avatars = new File(getClass().getResource("avatars.png").toURI());
            //Contact fifi = new Contact("fifi", true, avatars);
            //contactObservableList.add(fifi);
            //contactMap.add(fifi);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Initialise la CellFactory et la liste observable associée à la PostListView
     */
    private void initPostListView() {
        try {
            postListView.setCellFactory(postListView -> new PostListViewCell(this));
            postListView.setItems(postsObservableList);
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    /**
     * Le contact sélectionné dans la ListView
     * @return Le login associé au contact
     */
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

    /**
     * Le salon sélectionné dans la ListView
     * @return Le nom du salon
     */
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

    /**
     * Le contact utilisant l'application
     *
     * @return Le contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Getter de l'annuaire des contacts
     * @return L'annuaire des contacts
     */
    public ContactMap getContactsMap() {
        return contactMap;
    }

    /**
     * Vide la vue des messages puis envoie un évènement JOIN et un évènement LSTP en fonction du salon sélectionné
     * @param roomSelected Le salon sélectionné
     */
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

        Post postSys = new Post("system", loginTextField.getText(), i18nBundle.getString("systemHelloRoom") + roomSelected);
        postsObservableList.clear();
        postsObservableList.add(postSys);
        client.sendEvent(new rtgre.modeles.Event("JOIN", new JSONObject().put("room", roomSelected.getRoomName())));
        client.sendListPostEvent(0, roomSelected.toString());
        postListView.refresh();
    }

    /**
     * Vide la vue des messages puis envoie un évènement LSTP en fonction du contact sélectionné
     * @param contactSelected Le contact sélectionné
     */
    void handleContactSelection(Contact contactSelected) {
        if (contactSelected != null) {
            LOGGER.info("Clic sur " + contactSelected);
        }

        if (!roomsListView.getSelectionModel().isEmpty()) {
            roomsListView.getSelectionModel().clearSelection();
        }

        contactSelected.getUnreadCount().setUnreadCount(0);

        Post postSys = new Post("system", loginTextField.getText(), i18nBundle.getString("systemHelloContact") + contactSelected.getLogin());
        postsObservableList.clear();
        postsObservableList.add(postSys);
        client.sendListPostEvent(0, contactSelected.getLogin());
        postListView.refresh();
    }


    /**
     * Callback gérant les évènements réseaux reçus en provenance du serveur, en fonction du type de l'évènement
     * @param event L'évènement reçu
     */
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

    /**
     * Traite les évènements de type "CONT" informant de l'état d'un contact
     * @param content Le contenu d'un évènement `"CONT"`
     */
    private void handleRoomEvent(JSONObject content) {
        LOGGER.info(content.toString());
        Room room = new Room(content.getString("room"));
        roomMap.add(room);
        roomObservableList.add(room);
        roomsListView.refresh();
    }

    /**
     * Traite la réception d'un post
     * @param content Le contenu d'un évènement `"POST"`
     */
    private void handlePostEvent(JSONObject content) {

        System.out.println("Selected: " + roomsListView.getSelectionModel().getSelectedItem());
        System.out.println("From: " + content.getString("from"));
        System.out.println("To: " + content.getString("to"));

        try {
            if (!content.getString("to").contains("#")) {
                LOGGER.info("New message to contact!");
                if (contactsListView.getSelectionModel().getSelectedItem().getLogin().equals(content.getString("to"))) {
                    LOGGER.info("New message! to:dm, from:" + content.getString("from"));
                    postVector.remove(Post.fromJson(content));
                    postsObservableList.remove(Post.fromJson(content));
                    postVector.add(Post.fromJson(content));
                    postsObservableList.add(Post.fromJson(content));

                    postListView.refresh();
                }
                if (contact.getLogin().equals(content.getString("to"))) {
                    if (contactsListView.getSelectionModel().getSelectedItem().getLogin().equals(content.getString("from"))) {
                        LOGGER.info("New message! to:dm, from:myself");
                        postVector.remove(Post.fromJson(content));
                        postsObservableList.remove(Post.fromJson(content));
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
                    postVector.remove(Post.fromJson(content));
                    postsObservableList.remove(Post.fromJson(content));
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
        } finally {
            postListView.getItems().sort((o1,o2)->{
                if(o1.equals(o2)) return 0;
                if(o1.getTimestamp() > o2.getTimestamp())
                    return 1;
                else
                    return 0;
            });
            postListView.refresh();
        }
    }

    /**
     *
     * Traite les évènements de type "CONT" informant de l'état d'un contact
     * @param content Le contenu d'un évènement `"CONT"`
     */
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