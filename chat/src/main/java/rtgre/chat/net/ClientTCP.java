package rtgre.chat.net;

import rtgre.chat.ChatController;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.LogManager;
import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Client TCP : envoie des chaines de caractères à un serveur et lit les chaines en retour.
 */
public class ClientTCP {
    /** Couleur rouge */
    public static final String RED = "\u001b[31m";
    /** Couleur bleue */
    public static final String BLUE = "\u001b[34m";
    /** Couleur standard */
    public static final String RST = "\u001b[0m";
    /** Fin de message */
    public static final String END_MESSAGE = "fin";

    /**
     * Socket connecté au serveur
     */
    protected Socket sock;

    /**
     * Flux de caractères UTF-8 en sortie
     */
    protected PrintStream out;

    /**
     * Flux de caractères UTF-8 en entrée
     */
    protected BufferedReader in;

    /**
     * Chaine de caractères "ip:port" du client
     */
    protected String ipPort;

    /**
     * Le client est-il connecté ?
     */
    protected boolean connected;

    /*
    static {
        try {
            InputStream is = ClientTCP.class.getClassLoader()
                    .getResource("rtgre/chat/logging.properties").openStream();
            LogManager.getLogManager().readConfiguration(is);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot read configuration file", e);
        }
    }*/

    /**
     * Programme principal [Déprécié]
     * @param args Arguments
     * @throws Exception Si la connexion échoue
     */
    public static void main(String[] args) throws Exception {
        /*
        ClientTCP client = new ClientTCP("localhost", 2024);

        // Essai simple d'émission / réception d'une chaine de caractères
        String message = "Hello World!";
        System.out.println(BLUE + "Envoi :" + message + RST);
        client.send(message);
        message = client.receive();
        System.out.println(RED + "Réception: " + message + RST);

        client.close();
         */

        ClientTCP client = new ClientTCP("localhost", 2024);
        Thread envoi = new Thread(client::sendLoop);
        Thread reception = new Thread(client::receiveLoop);
        envoi.start();
        reception.start();
        envoi.join();
        client.close();
    }

    /**
     * Le constructeur ouvre la connexion TCP au serveur <code>host:port</code>
     * et récupère les flux de caractères en entrée {@link #in} et sortie {@link #out}
     *import static rtgre.chat.ChatApplication.LOGGER;
     * @param host IP ou nom de domaine du serveur
     * @param port port d'écoute du serveur
     * @throws IOException si la connexion échoue ou si les flux ne sont pas récupérables
     */
    public ClientTCP(String host, int port) throws IOException {
        System.out.printf("Connexion à [%s:%d]%n", host, port);
        sock = new Socket(host, port);
        ipPort = "%s:%d".formatted(sock.getLocalAddress().getHostAddress(), sock.getLocalPort());
        LOGGER.info("[%s] Connexion établie vers [%s:%d]".formatted(ipPort, host, port));
        this.connected = true;
        LOGGER.fine("[%s] Recuperation des flux d'octets en entree et sortie".formatted(ipPort));
        OutputStream os = sock.getOutputStream();
        InputStream is = sock.getInputStream();
        LOGGER.fine("[%s] Conversion flux d'octets en flux de caractères UTF-8".formatted(ipPort));
        out = new PrintStream(os, true, StandardCharsets.UTF_8);
        in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8), 2048);
        Thread rcLoop = new Thread(this::receiveLoop);
        rcLoop.setDaemon(true);
        rcLoop.start();
    }

    /**
     * Envoie une chaine de caractères
     *
     * @param message chaine de caractères à transmettre
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
     * Getter de connected
     * @return L'état de connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Setter de connected
     * @param connected L'utilisateur est-il connecté ?
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Attente d'une chaine de caractères en entrée.
     *
     * @return chaine de caractères reçue
     * @throws IOException lorsque la fin du flux est atteinte
     */
    public String receive() throws IOException {
        String message = in.readLine();
        LOGGER.finest("receive: %s".formatted(message));
        if (message == null) {
            throw new IOException("End of the stream has been reached");
        }
        return message;
    }

    /**
     * Fermeture de la connexion TCP
     */
    public void close() {
        LOGGER.info("[%s] Fermeture de la connexion".formatted(ipPort));
        try {
            sock.close();
            this.connected = false;
        } catch (IOException e) {
            LOGGER.finest("[%s] %s".formatted(ipPort, e));
        }
    }


    /**
     * Boucle d'envoi de messages
     */
    public void sendLoop() {
        System.out.println(BLUE + "Boucle d'envoi de messages..." + RST);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        connected = true;
        try {
            while (connected) {
                System.out.println(BLUE + "Votre message (\"fin\" pour terminer) : " + RST);
                String message = stdIn.readLine();
                if (message == null) { // fin du flux stdIn
                    message = END_MESSAGE;
                }
                System.out.println(BLUE + "Envoi: " + message + RST);
                this.send(message);
                if (END_MESSAGE.equals(message)) {
                    connected = false;
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            connected = false;
        }
    }

    /**
     * Boucle de réception de messages
     */
    public void receiveLoop() {
        System.out.println(RED + "Boucle de réception de messages..." + RST);
        connected = true;
        try {
            while (connected) {
                String message = this.receive();
                System.out.println(RED + "Réception: " + message + RST);
            }
        } catch (IOException e) {
            LOGGER.severe("[%s] %s".formatted(ipPort, e));
            connected = false;
        }
    }
}