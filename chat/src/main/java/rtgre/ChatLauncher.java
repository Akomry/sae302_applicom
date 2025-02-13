package rtgre;

import rtgre.chat.ChatApplication;
import rtgre.server.ChatServer;

import java.util.logging.Level;

import static rtgre.chat.ChatApplication.LOGGER;


/**
 * Application pour lancer soit ChatServer, soit ChatApplication
 */
public class ChatLauncher {
    /**
     * Avec le paramètre "server" sur la ligne de commande, ChatServer, sinon lance ChatApplication
     * @param args Paramètres de la ligne de commande
     */
    public static void main(String[] args) {
        for (String arg : args) {
            LOGGER.log(Level.FINEST, arg);
        }
        try {
            if ("server".equals(args[0])) {
                LOGGER.log(Level.INFO, "Lancement du serveur...");
                ChatServer.main(args);
            } else {
                LOGGER.log(Level.INFO, "Lancement du client...");
                ChatApplication.main(args);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, e.getMessage(), e);
            LOGGER.log(Level.INFO, "Lancement du client...");
            ChatApplication.main(args);
        }

    }
}
