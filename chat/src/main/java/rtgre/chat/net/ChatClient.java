package rtgre.chat.net;

import javafx.application.Platform;
import org.json.JSONObject;
import rtgre.chat.ChatController;
import rtgre.modeles.Contact;
import rtgre.modeles.Event;
import rtgre.modeles.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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



    public void sendEvent(Event event) {
        connected = true;
        try {
            String message = event.toJson();
            if (message == null) { // fin du flux stdIn
                message = END_MESSAGE;
            }
            System.out.println(BLUE + "Envoi: " + message + RST);
            this.send(message);
            if (END_MESSAGE.equals(message)) {
                connected = false;
            }
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            connected = false;
        }
    }

    public void sendAuthEvent(Contact contact) {
        Event authEvent = new Event(Event.AUTH, new JSONObject().put("login", contact.getLogin()));
        sendEvent(authEvent);
    }

    public void sendListPostEvent(long since, String select) {
        Event listPostEvent = new Event(
                Event.LIST_POSTS,
                new JSONObject()
                        .put("since", since)
                        .put("select", select)
        );
        sendEvent(listPostEvent);
    }

    public void sendQuitEvent() {
        Event quitEvent = new Event(Event.QUIT, new JSONObject());
        sendEvent(quitEvent);
    }


    @Override
    public void receiveLoop() {
        LOGGER.info(RED + "Boucle de réception de messages..." + RST);
        try {
            while (connected) {
                String message = this.receive();
                LOGGER.info(RED + "Réception: " + message + RST);
                LOGGER.info(RED + message + RST);
                if (listener != null) {
                    Platform.runLater(() -> listener.handleEvent(Event.fromJson(message)));
                }
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

    public void sendMessageEvent(Message msg) {
        sendEvent(new Event("MESG", msg.toJsonObject()));
    }
}
