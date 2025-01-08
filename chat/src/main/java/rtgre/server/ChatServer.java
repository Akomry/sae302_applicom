package rtgre.server;

import org.json.JSONException;
import org.json.JSONObject;
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

/**
 * Programme serveur qui renvoie les chaines de caractères lues jusqu'à recevoir le message "fin"
 */
public class ChatServer {

    private static final Logger LOGGER = Logger.getLogger(ChatServer.class.getCanonicalName());

    private Vector<ChatClientHandler> clientList;
    private PostVector postVector;
    private ContactMap contactMap;
    private RoomMap roomMap;

    static {
        try {
            InputStream is = ChatServer.class.getClassLoader()
                    .getResource("logging.properties").openStream();
            LogManager.getLogManager().readConfiguration(is);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot read configuration file", e);
        }
    }


    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(2024);
        daisyConnect();
        server.acceptClients();
    }

    /**
     * Socket passif en écoute
     */
    private ServerSocket passiveSock;

    public ChatServer(int port) throws IOException {
        passiveSock = new ServerSocket(port);
        LOGGER.info("Serveur en écoute " + passiveSock);
        clientList = new Vector<>();
        contactMap = new ContactMap();
        postVector = new PostVector();
        roomMap = new RoomMap();
        contactMap.loadDefaultContacts();
        roomMap.loadDefaultRooms();
    }

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

    public void removeClient(ChatClientHandler client) {
        clientList.remove(client);
        LOGGER.fine("Client [%s] retiré de la liste (%d clients connectés)"
                .formatted(client.getIpPort(), clientList.size()));
    }

    public Vector<ChatClientHandler> getClientList() {
        return clientList;
    }

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

    public ChatClientHandler findClient(Contact contact) {
        for (ChatClientHandler user: clientList) {
            if (user.user.equals(contact)) {
                return user;
            }
        }
        return null;
    }

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

    public void sendEventToAllContacts(Event event) {
        for (Contact contact: contactMap.values()) {
            if (contact.isConnected()) {
                sendEventToContact(contact, event);
            }
        }
    }

    public ContactMap getContactMap() {
        return contactMap;
    }

    /** Temporaire : connecte daisy pour test */
    public static void daisyConnect() throws IOException {
        ChatClient client = new ChatClient("localhost", 2024, null);
        client.sendAuthEvent(new Contact("daisy", null));
    }

    private class ChatClientHandler {
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

        private Contact user;

        /**
         * Initialise les attributs {@link #sock} (socket connecté au client),
         * {@link #out} (flux de caractères UTF-8 en sortie) et
         * {@link #in} (flux de caractères UTF-8 en entrée).
         *
         * @param sock socket connecté au client
         * @throws IOException
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
            } else if (event.getType().equals(Event.QUIT)) {
                LOGGER.info("Déconnexion");
                return false;
            } else {
                LOGGER.warning("Unhandled event type: " + event.getType());
                return false;
            }
        }

        private void doListRoom(JSONObject content) {
            System.out.println(contactMap.getContact(user.getLogin()).isConnected());
            if (contactMap.getContact(user.getLogin()).isConnected()) {
                for (Room room: roomMap.values()) {
                    System.out.println(room);
                    try {
                        System.out.println(new Event("ROOM", room.toJsonObject()).toJson());
                        send(new Event("ROOM", room.toJsonObject()).toJson());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        throw new IllegalStateException();
                    }
                }
            }
        }

        private void doListPost(JSONObject content) throws JSONException, IllegalStateException {

            if (contactMap.getContact(user.getLogin()).isConnected()) {
                if (!contactMap.containsKey(content.getString("select"))) {
                    throw new IllegalStateException();
                }
                for (Post post: postVector.getPostsSince(content.getLong("since"))) {
                    if (post.getTo().equals(content.getString("select")) ||
                        post.getFrom().equals(content.getString("select"))) {
                        sendEventToContact(contactMap.getContact(user.getLogin()), new Event(Event.POST, post.toJsonObject()));
                    }
                }
            }
        }

        private void doMessage(JSONObject content) throws JSONException, IllegalStateException {
            if (contactMap.getContact(user.getLogin()).isConnected()) {
                if (content.getString("to").equals(user.getLogin()) || !contactMap.containsKey(content.getString("to"))) {
                    throw new IllegalStateException();
                } else {
                    Post post = new Post(
                            user.getLogin(),
                            Message.fromJson(content)
                    );
                    Event postEvent = new Event("POST", post.toJsonObject());

                    sendEventToContact(contactMap.getContact(post.getFrom()), postEvent);
                    sendEventToContact(contactMap.getContact(post.getTo()), postEvent);

                    postVector.add(post);
                    LOGGER.info("Fin de doMessage");
                }
            }
        }

        private void doListContact(JSONObject content) throws JSONException, IllegalStateException {
            for (Contact contact: contactMap.values()) {
                if (contactMap.getContact(user.getLogin()).isConnected()) {
                    try {
                        send(new Event(Event.CONT, contact.toJsonObject()).toJson());
                    } catch (IOException e) {
                        throw new IllegalStateException();
                    }
                }
            }
        }

        private void doLogin(JSONObject content) {
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
                sendAllOtherClients(
                        findClient(contactMap.getContact(login)),
                        new Event("CONT", user.toJsonObject()).toJson()
                );
            }
        }

        public void send(String message) throws IOException {
            LOGGER.finest("send: %s".formatted(message));
            out.println(message);
            if (out.checkError()) {
                throw new IOException("Output stream error");
            }
        }

        public String getIpPort() {
            return ipPort;
        }

        public void sendAllOtherClients(ChatClientHandler fromClient, String message) {
            for (ChatClientHandler client : clientList) {
                if (!client.equals(this)) {
                    LOGGER.fine(clientList.toString());
                    LOGGER.fine("Envoi vers [%s] : %s".formatted(client.getIpPort(), message));
                    try {
                        client.send("[%s] %s".formatted(fromClient.getIpPort(), message));
                    } catch (Exception e) {
                        LOGGER.severe("[%s] %s".formatted(client.getIpPort(), e));
                        client.close();
                    }
                }
            }
        }

        public String receive() throws IOException {
            String message = in.readLine();
            LOGGER.info("receive: %s".formatted(message));
            if (message == null) {
                throw new IOException("End of the stream has been reached");
            }
            return message;
        }

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