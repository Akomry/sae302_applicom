package rtgre;

import rtgre.chat.ChatApplication;
import rtgre.server.ChatServer;


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
            System.out.println(arg);
        }
        try {
            if ("server".equals(args[0])) {
                System.out.println("test1");
                ChatServer.main(args);
            } else {
                ChatApplication.main(args);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            System.out.println("test2");
            ChatApplication.main(args);
        }

    }
}
