package rtgre.chat.net;

import rtgre.chat.ChatController;

import java.io.IOException;
import java.util.logging.Logger;

import static rtgre.chat.ChatApplication.LOGGER;

public class ChatClient extends ClientTCP {

    private final ChatController listener;

    /**
     * Le constructeur ouvre la connexion TCP au serveur <code>host:port</code>
     * et récupère les flux de caractères en entrée {@link #in} et sortie {@link #out}
     * import static rtgre.chat.ChatApplication.LOGGER;
     *
     * @param host IP ou nom de domaine du serveur
     * @param port port d'écoute du serveur
     * @param listener instance de ChatController liée au client
     * @throws IOException
     */
    public ChatClient(String host, int port, ChatController listener) throws IOException {
        super(host, port);
        this.listener = listener;
    }

    @Override
    public void receiveLoop() {
        LOGGER.info(RED + "Boucle de réception de messages..." + RST);
        try {
            while (connected) {
                String message = this.receive();
                LOGGER.info(RED + "Réception: " + message + RST);
                LOGGER.info(RED + message + RST);
            }
        } catch (IOException e) {
            LOGGER.severe("[%s] %s".formatted(ipPort, e));
            connected = false;
        } finally {
            close();
        }
    }

    public Logger getLogger() {
        return LOGGER;
    }

    public ChatController getListener() {
        return listener;
    }
}
