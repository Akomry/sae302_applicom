package rtgre.server;

import javafx.scene.chart.PieChart;
import org.json.JSONException;
import org.json.JSONObject;
import rtgre.chat.ChatController;
import rtgre.chat.net.ChatClient;
import rtgre.modeles.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Programme serveur qui renvoie les chaines de caractères lues jusqu'à recevoir le message "fin"
 */
public class ChatServer {

    /** Liste des clients connectés */
    private Vector<ChatClientHandler> clientList;
    /** Liste des messages */
    private PostVector postVector;
    /** Annuaire des contacts */
    private ContactMap contactMap;
    /** Liste des salons */
    private RoomMap roomMap;
    /** Connexion à la base de données */
    private DatabaseApi database;
    /** Socket passif en écoute */
    private ServerSocket passiveSock;


    static {
        try {
            InputStream is = ChatController.class
                    .getResource("logging.properties").openStream();
            LogManager.getLogManager().readConfiguration(is);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot read configuration file", e);
        }
    }

    /**
     * Le programme principal : instancie un serveur en écoute sur le port 2024 et le place en attente de clients.
     * @param args Arguments du programme principal
     * @throws IOException en cas de problème de connexion ou de base de données
     */
    public static void main(String[] args) throws IOException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IOException("Cannot connect to database");
        }
        ChatServer server = new ChatServer(2024);
        //daisyConnect();
        server.acceptClients();
    }


    /**
     * Constructeur : initialisation du serveur, en écoute sur le port fourni
     * @param port Le port de connexion
     * @throws IOException si la connexion ne peut être établie
     */
    public ChatServer(int port) throws IOException {
        passiveSock = new ServerSocket(port);
        LOGGER.info("Serveur en écoute " + passiveSock);
        clientList = new Vector<>();
        contactMap = new ContactMap();
        postVector = new PostVector();
        roomMap = new RoomMap();
        contactMap.loadDefaultContacts();
        roomMap.loadDefaultRooms();
        roomMap.setLoginSets();
        postVector.loadPosts();
    }

    /**
     * Getter de `PostVector`
     * @return La liste des posts
     */
    public PostVector getPostVector() {
        return postVector;
    }

    /**
     * Getter de `roomMap`
     * @return La liste des salons
     */
    public RoomMap getRoomMap() {
        return roomMap;
    }

    /**
     * Ferme la connexion du serveur, en fermant la connexion auprès de tous ses clients, puis en fermant son socket en écoute passive.
     * @throws IOException si la connexion
     */
    public void close() throws IOException {
        for (ChatClientHandler client : clientList) {
            client.close();
        }
        passiveSock.close();
    }

    /**
     * Boucle d'attente des clients
     */
    public void acceptClients() {
        int clientCounter = 1;
        while (true) {
            try {
                LOGGER.info("Attente du client n°%02d".formatted(clientCounter));
                Socket sock = passiveSock.accept();
                LOGGER.info("[%s:%d] Connexion établie (client n°%02d)"
                        .formatted(sock.getInetAddress().getHostAddress(), sock.getPort(), clientCounter));
                handleNewClient(sock);
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
            clientCounter++;
        }
    }

    /**
     * Retire `client` de la liste des clients connectés `clientList`
     * @param client client à retirer de la liste `clientList`
     */
    public void removeClient(ChatClientHandler client) {
        clientList.remove(client);
        LOGGER.fine("Client [%s] retiré de la liste (%d clients connectés)"
                .formatted(client.getIpPort(), clientList.size()));
    }

    /**
     * Getter de `clientList`
     * @return La liste des clients
     */
    public Vector<ChatClientHandler> getClientList() {
        return clientList;
    }

    /**
     * Getter de passiveSocket
     * @return Le socket en écoute passive du serveur
     */
    public ServerSocket getPassiveSocket() {
        return passiveSock;
    }

    /**
     * Prise en charge d'un nouveau client
     *
     * @param sock socket connecté au client
     * @throws IOException
     */
    private void handleNewClient(Socket sock) throws IOException {
        ChatClientHandler client = new ChatClientHandler(sock);
        Thread clientLoop = new Thread(client::eventReceiveLoop);
        clientLoop.start();
        clientList.add(client);
        LOGGER.fine("Ajout du client [%s] dans la liste (%d clients connectés)"
                .formatted(client.getIpPort(), clientList.size()));
        //client.echoLoop();
    }

    /**
     * Renvoie le client de connexion (objet ChatServer.ChatClientHandler) associé à un contact
     * @param contact Le contact recherché
     * @return Le client de connexion associé ou `null` si le contact n'existe pas
     */
    public ChatClientHandler findClient(Contact contact) {
        for (ChatClientHandler user: clientList) {
            if (user.user.equals(contact)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Envoi d'un évènement event à un contact donné, sous réserve qu'il soit connecté. Si l'envoi échoue, ferme la connexion avec le contact.
     * @param contact Le contact destinataire
     * @param event L'évènement à envoyer
     */
    public void sendEventToContact(Contact contact, Event event) {
        ChatClientHandler user = findClient(contact);
        if (!(user == null)) {
            try {
                user.send(event.toJson());
            } catch (Exception e) {
                LOGGER.warning("!!Erreur de l'envoi d'Event à %s, fermeture de la connexion".formatted(user.user.getLogin()));
                user.close();
            }
        }
    }

    /**
     * Envoi d'un évènement à tous les contacts connectés
     * @param event L'évènement à envoyer
     */
    public void sendEventToAllContacts(Event event) {
        for (Contact contact: contactMap.values()) {
            if (contact.isConnected()) {
                sendEventToContact(contact, event);
            }
        }
    }

    /**
     * Getter de contactMap
     * @return La liste des contacts
     */
    public ContactMap getContactMap() {
        return contactMap;
    }

    /**
     * Temporaire : connecte daisy pour test
     * @throws IOException si la connexion ne peut être établie
     */
    public static void daisyConnect() throws IOException {
        ChatClient client = new ChatClient("localhost", 2024, null);
        client.sendAuthEvent(new Contact("daisy", null));
    }

    /**
     * Gestion du dialogue avec un client TCP
     */
    private class ChatClientHandler {
        /** Message de fin d'une connexion */
        public static final String END_MESSAGE = "fin";
        /**
         * Socket connecté au client
         */
        private Socket sock;
        /**
         * Flux de caractères en sortie
         */
        private PrintStream out;
        /**
         * Flux de caractères en entrée
         */
        private BufferedReader in;
        /**
         * Chaine de caractères "ip:port" du client
         */
        private String ipPort;
        /** Contact associé au client courant */
        private Contact user;

        /**
         * Initialise les attributs {@link #sock} (socket connecté au client),
         * {@link #out} (flux de caractères UTF-8 en sortie) et
         * {@link #in} (flux de caractères UTF-8 en entrée).
         *
         * @param sock socket connecté au client
         * @throws IOException si la connexion ne peut être établie ou si les flux ne peuvent être récupérés
         */
        public ChatClientHandler(Socket sock) throws IOException {
            this.sock = sock;
            this.ipPort = "%s:%d".formatted(sock.getInetAddress().getHostAddress(), sock.getPort());
            OutputStream os = sock.getOutputStream();
            InputStream is = sock.getInputStream();
            this.out = new PrintStream(os, true, StandardCharsets.UTF_8);
            this.in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        }

        /**
         * Boucle écho : renvoie tous les messages reçus.
         */
        public void echoLoop() {
            try {
                String message = null;
                while (!END_MESSAGE.equals(message)) {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    LOGGER.info("[%s] Réception de : %s".formatted(ipPort, message));
                    LOGGER.info("[%s] Envoi de : %s".formatted(ipPort, message));
                    //out.println(message);
                    sendAllOtherClients(this, message);

                }
            } catch (IOException e) {
                LOGGER.severe("[%s] %s".formatted(ipPort, e));
            }
            close();
        }

        /**
         * Boucle de réception d'évènement : réceptionne les messages reçus et les délèguent à `handleEvent(java.lang.String)` pour les interpréter
         */
        public void eventReceiveLoop() {
            try {
                String message = null;
                while (!END_MESSAGE.equals(message)) {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    LOGGER.info("[%s] Réception de : %s".formatted(ipPort, message));
                    try {
                        if (!handleEvent(message)) {
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.severe(e.getMessage());
                        break;
                    }
                }
            } catch (IOException e) {
                LOGGER.severe("[%s] %s".formatted(ipPort, e));
            }
            close();
        }

        /**
         * Traitement d'un évènement. Ventile vers les méthodes traitant chaque type d'évènement.
         * @param message objet évènement sous la forme d'une chaine JSON brute de réception
         * @return `false` si l'évènement est de type Event.QUIT , `true` pour tous les autres types.
         * @throws JSONException si l'objet JSON n'est pas conforme
         * @throws IllegalStateException si l'authentification n'est pas effectuée
         */
        private boolean handleEvent(String message) throws JSONException, IllegalStateException {
            Event event = Event.fromJson(message);
            if (event.getType().equals(Event.AUTH)) {
                doLogin(event.getContent());
                LOGGER.finest("Login successful");
                return true;
            } else if (event.getType().equals(Event.LIST_CONTACTS)) {
                doListContact(event.getContent());
                LOGGER.finest("Sending contacts");
                return true;
            } else if (event.getType().equals(Event.MESG)) {
                doMessage(event.getContent());
                LOGGER.info("Receiving message");
                return true;
            } else if (event.getType().equals(Event.LIST_POSTS)) {
                doListPost(event.getContent());
                LOGGER.info("Sending Posts");
                return true;
            } else if (event.getType().equals(Event.LIST_ROOMS)) {
                doListRoom(event.getContent());
                LOGGER.info("Sending Rooms");
                return true;
            } else if (event.getType().equals(Event.JOIN)) {
                doJoin(event.getContent());
                LOGGER.info("New user joining room!");
                return true;
            } else if (event.getType().equals(Event.QUIT)) {
                LOGGER.info("Déconnexion");
                return false;
            } else if (event.getType().equals(Event.CONT)) {
                doCont(event.getContent());
                LOGGER.info("Update de contact");
                return true;
            } else if (event.getType().equals(Event.POST)) {
                doPost(event.getContent());
                LOGGER.info("Post edited");
                return true;
            } else {
                LOGGER.warning("Unhandled event type: " + event.getType());
                return false;
            }
        }

        /**
         * Met à jour un Post en fonction de son UUID
         * @param content le contenu d'un évènement "POST"
         */
        private void doPost(JSONObject content) {
            database = new DatabaseApi();
            database.removePost(Post.fromJson(content));
            database.addPost(Post.fromJson(content));
            database.close();
            postVector.removeIf(post -> post.getId().equals(Post.fromJson(content).getId()));
            postVector.add(Post.fromJson(content));
            sendEventToAllContacts(new Event(Event.POST, content));
            LOGGER.info("didpost");
        }

        /**
         * Met à jour un contact et envoie à tous les autres utilisateurs la mise à jour
         * @param content Le contenu d'un évènement "CONT"
         */
        private void doCont(JSONObject content) {
            if (user.isConnected()) {
                sendEventToAllContacts(new Event("CONT", content));
                contactMap.getContact(content.getString("login")).setAvatar(Contact.base64ToImage(content.getString("avatar")));
            }
        }

        /**
         * Gère l'arrivée à un utilisateur dans un salon donné dans le contenu du message.
         * @param content Le contenu d'un évènement "JOIN"
         */
        private void doJoin(JSONObject content) {
            if (content.getString("room").isEmpty()) {
                user.setCurrentRoom(null);
            }
            if (user.getLogin().isEmpty()) {
                user.setCurrentRoom(null);
                return;
            }
            if (roomMap.get(content.getString("room")).getLoginSet() == null) {
                user.setCurrentRoom(content.getString("room"));
            }
            else if (roomMap.get(content.getString("room")).getLoginSet().contains(user.getLogin())) {
                user.setCurrentRoom(content.getString("room"));
            }
        }

        /**
         * Gère la demande d'envoi de la liste des salons : récupère tous les posts dont l'utilisateur est autorisé à accéder, puis les envoie un par un au client via des évènements "ROOM".
         * @param content Le contenu d'un évènement "LSTR"
         */
        private void doListRoom(JSONObject content) {
            if (contactMap.getContact(user.getLogin()).isConnected()) {
                for (Room room: roomMap.values()) {
                    if (room.getLoginSet().contains(user.getLogin())) {
                        try {
                            send(new Event("ROOM", room.toJsonObject()).toJson());
                        } catch (IOException e) {
                            throw new IllegalStateException();
                        }
                    }
                }
            }
        }

        /**
         * Gère la demande d'envoi de la liste des posts : récupère tous les posts ayant trait au login ou au salon indiqué dans content et étant postérieur au timestamp indiqué dans content, puis les envoie un par un au client via des évènements "POST".
         * @param content Le contenu d'un évènement "LSTP"
         * @throws JSONException si le format JSON n'est pas respecté
         * @throws IllegalStateException si le login ou le salon demandé n'existent pas
         */
        private void doListPost(JSONObject content) throws JSONException, IllegalStateException {
            if (contactMap.getContact(user.getLogin()).isConnected()) {
                if (!contactMap.containsKey(content.getString("select")) && !roomMap.containsKey(content.getString("select"))) {
                    System.out.println("!select");
                    throw new IllegalStateException();
                }
                if (!content.getString("select").contains("#")) {
                    System.out.println("!#");
                    for (Post post : postVector.getPostsSince(content.getLong("since"))) {
                        if (post.getTo().equals(content.getString("select")) ||
                                post.getFrom().equals(content.getString("select"))) {
                            sendEventToContact(contactMap.getContact(user.getLogin()), new Event(Event.POST, post.toJsonObject()));
                        }
                    }
                } else if (user.getCurrentRoom().equals(content.getString("select"))) {
                    System.out.println("#");
                    for (Post post: postVector.getPostsSince(content.getLong("since"))) {
                        if (post.getTo().equals(content.getString("select"))) {
                            sendEventToContact(contactMap.getContact(user.getLogin()), new Event(Event.POST, post.toJsonObject()));
                        }
                    }
                }
            }
        }

        /**
         * Gère la réception d'un message, en créant le Post associé et en l'envoyant à son destinataire privé ou aux membres d'un salon de discussion public
         * @param content Le contenu JSON représentant un message
         * @throws JSONException si le format JSON n'est pas respecté
         * @throws IllegalStateException si un évènement destiné à un contact ne peut être envoyé
         */
        private void doMessage(JSONObject content) throws JSONException, IllegalStateException {
            if (contactMap.getContact(user.getLogin()).isConnected()) {
                if (content.getString("to").equals(user.getLogin()) ||
                    (!contactMap.containsKey(content.getString("to"))) && !roomMap.containsKey(content.getString("to"))) {
                    throw new IllegalStateException("IllegalStateException! Cannot Post");
                } if(!content.getString("to").contains("#")) {
                    Post post = new Post(
                            user.getLogin(),
                            Message.fromJson(content)
                    );
                    Event postEvent = new Event("POST", post.toJsonObject());

                    sendEventToContact(contactMap.getContact(post.getFrom()), postEvent);
                    sendEventToContact(contactMap.getContact(post.getTo()), postEvent);

                    postVector.add(post);

                    database = new DatabaseApi();
                    database.addPost(post);
                    database.close();

                    LOGGER.info("Fin de doMessage:dm");
                } else {
                    Post post = new Post(
                            user.getLogin(),
                            Message.fromJson(content)
                    );
                    Event postEvent = new Event("POST", post.toJsonObject());

                    for (ChatClientHandler client: clientList) {
                        if (client.user.getCurrentRoom() != null) {
                            if (client.user.getCurrentRoom().equals(content.getString("to"))) {
                                sendEventToContact(client.user, postEvent);
                            }
                        }
                    }
                    postVector.add(post);
                    LOGGER.info("Fin de doMessage:room");

                }
            }
        }

        /**
         * Gère la demande de la liste des contacts : les contacts sont envoyés un par un au client sous la forme d'évènement "CONT"
         * @param content Le contenu de la demande de la liste des contacts
         * @throws JSONException si le format JSON n'est pas respecté
         * @throws IllegalStateException si un évènement destiné à un contact ne peut être envoyé
         */
        private void doListContact(JSONObject content) throws JSONException, IllegalStateException {
            for (Contact contact: contactMap.values()) {
                if (contactMap.getContact(user.getLogin()).isConnected()) {
                    sendEventToContact(user, new Event(Event.CONT, contact.toJsonObject()));
                }
            }
        }

        /**
         * Gère l'authentification d'un client en :
         * *    récupérant son login dans content.
         * *    en vérifiant qu'il fait partie des contacts autorisés dans l'annuaire des contacts.
         * *    en modifiant son état de connexion dans l'annuaire des contacts.
         * *    en informant les autres clients de la connexion.
         * Si aucun login n'est fourni, si le client n'est pas autorisé à se connecter, ou si le client s'authentifie alors qu'il est déjà connecté, une exception IllegalStateException est levée.
         * @param content Le contenu de la demande
         * @throws JSONException si le format JSON n'est pas respecté
         * @throws IllegalStateException si l'utilisateur n'est pas autorisé à se connecter ou s'il est déjà connecté
         */
        private void doLogin(JSONObject content) throws JSONException, IllegalStateException {
            String login = content.getString("login");
            if (login.isEmpty()) {
                LOGGER.warning("Aucun login fourni");
                throw new JSONException("Aucun login fourni");
            } else if (!contactMap.containsKey(login)) {
                LOGGER.warning("Login non-authorisé");
                throw new IllegalStateException("Login non-authorisé");
            } else {
                LOGGER.info("Connexion de " + login);
                contactMap.getContact(login).setConnected(true);
                this.user = contactMap.getContact(login);
                System.out.println(user.isConnected());
                sendAllOtherClients(
                        findClient(contactMap.getContact(login)),
                        new Event("CONT", user.toJsonObject()).toJson()
                );
            }
        }

        /**
         * Envoie une chaine de caractères
         * @param message Chaine de caractères à transmettre
         * @throws IOException lorsqu'une erreur sur le flux de sortie est détectée
         */
        public void send(String message) throws IOException {
            LOGGER.finest("send: %s".formatted(message));
            out.println(message);
            if (out.checkError()) {
                throw new IOException("Output stream error");
            }
        }

        /**
         * Getter de ipPort
         * @return L'IP et le port du client
         */
        public String getIpPort() {
            return ipPort;
        }

        /**
         * Envoie un message à tous les autres clients que le client courant
         * @param fromClient Le client courant
         * @param message Le message à envoyer
         */
        public void sendAllOtherClients(ChatClientHandler fromClient, String message) {
            for (ChatClientHandler client : clientList) {
                if (!client.equals(fromClient)) {
                    LOGGER.fine(clientList.toString());
                    LOGGER.fine("Envoi vers [%s] : %s".formatted(client.getIpPort(), message));
                    try {
                        client.send(message);
                    } catch (Exception e) {
                        LOGGER.severe("[%s] %s".formatted(client.getIpPort(), e));
                        client.close();
                    }
                }
            }
        }

        /**
         * Attente d'une chaine de caractères en entrée.
         * @return chaine de caractères reçue
         * @throws IOException lorsque la fin du flux est atteinte
         */
        public String receive() throws IOException {
            String message = in.readLine();
            LOGGER.info("receive: %s".formatted(message));
            if (message == null) {
                throw new IOException("End of the stream has been reached");
            }
            return message;
        }

        /**
         * Ferme la connexion TCP
         */
        public void close() {
            LOGGER.info("[%s] Fermeture de la connexion".formatted(ipPort));
            try {
                sock.close();
                removeClient(this);
                user.setConnected(false);
                contactMap.get(user.getLogin()).setConnected(false);
                sendEventToAllContacts(new Event(Event.CONT, user.toJsonObject()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}