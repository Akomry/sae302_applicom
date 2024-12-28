package rtgre.server;

import org.json.JSONException;
import org.json.JSONObject;
import rtgre.modeles.ContactMap;
import rtgre.modeles.Event;

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
    private ContactMap contactMap;

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
        contactMap.loadDefaultContacts();
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
        Thread clientLoop = new Thread(client::echoLoop);
        clientLoop.start();
        clientList.add(client);
        LOGGER.fine("Ajout du client [%s] dans la liste (%d clients connectés)"
                .formatted(client.getIpPort(), clientList.size()));
        //client.echoLoop();
    }

    public ContactMap getContactMap() {
        return contactMap;
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
                    LOGGER.info("[%s] Envoi de : %s".formatted(ipPort, message));
                    try {
                        if (handleEvent(message)) {
                            break;
                        }
                    } catch (Exception e) {
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
            switch (event.getType()) {
                case Event.AUTH:
                    doLogin(event.getContent());
                    return false;
                default:
                    return true;
            }
        }

        private void doLogin(JSONObject content) {
            String login = content.getString("login");
            if (login.equals("")) {
                throw new JSONException("Aucun login fourni");
            } else if (!contactMap.containsKey(login)) {
                throw new IllegalStateException("Login non-authorisé");
            } else {
                contactMap.getContact(login).setConnected(true);
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
            LOGGER.finest("receive: %s".formatted(message));
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}