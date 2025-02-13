package rtgre.chat.net;

import javafx.application.Platform;
import org.json.JSONObject;
import rtgre.chat.ChatController;
import rtgre.modeles.Contact;
import rtgre.modeles.Event;
import rtgre.modeles.Message;
import rtgre.modeles.Post;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant
 */
public class ChatClient extends ClientTCP {

    /** Le Controller du chat associé à la connexion */
    private final ChatController listener;

    /**
     * Le constructeur ouvre la connexion TCP au serveur <code>host:port</code>
     * et récupère les flux de caractères en entrée {@link #in} et sortie {@link #out}
     * import static rtgre.chat.ChatApplication.LOGGER;
     *
     * @param host IP ou nom de domaine du serveur
     * @param port port d'écoute du serveur
     * @param listener instance de ChatController liée au client
     * @throws IOException si la connexion échoue
     */
    public ChatClient(String host, int port, ChatController listener) throws IOException {
        super(host, port);
        this.listener = listener;
    }


    /**
     * Envoi d'un évènement, sérialisé dans sa représentation JSON, au serveur.
     * @param event L'évènement à envoyer
     */
    public void sendEvent(Event event) {
        connected = true;
        try {
            String message = event.toJson();
            if (message == null) { // fin du flux stdIn
                message = END_MESSAGE;
            }
            LOGGER.log(Level.FINE, BLUE + "Envoi: " + message + RST);
            this.send(message);
            if (END_MESSAGE.equals(message)) {
                connected = false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            connected = false;
        }
    }

    /**
     * Envoi de l'évènement d'authentification
     * @param contact Le contact associé à l'utilisateur
     */
    public void sendAuthEvent(Contact contact) {
        Event authEvent = new Event(Event.AUTH, new JSONObject().put("login", contact.getLogin()));
        sendEvent(authEvent);
    }

    /**
     * Demande la liste des posts (évènement de type "LSTP")
     * @param since L'horodatage à partir duquel est demandée la liste des posts
     * @param select Le login du contact ou le salon de discussion avec lequel les posts ont été échangés
     */
    public void sendListPostEvent(long since, String select) {
        Event listPostEvent = new Event(
                Event.LIST_POSTS,
                new JSONObject()
                        .put("since", since)
                        .put("select", select)
        );
        sendEvent(listPostEvent);
    }

    /**
     * Demande la liste des salons (évènement de type "LSTR")
     */
    public void sendListRoomEvent() {
        Event listRoomEvent = new Event(Event.LIST_ROOMS, new JSONObject());
        sendEvent(listRoomEvent);
    }

    /**
     * Envoie un évènement de fermeture de connexion (de type "QUIT")
     */
    public void sendQuitEvent() {
        Event quitEvent = new Event(Event.QUIT, new JSONObject());
        sendEvent(quitEvent);
    }


    /**
     * Boucle de réception des messages : chaque message est un évènement sérialisé en JSON, qui est transféré à ChatController.handleEvent(rtgre.modeles.Event) pour traitement.
     * Si le message n'est pas conforme (format JSON), la connection est stoppée.
     */
    @Override
    public void receiveLoop() {
        LOGGER.info(RED + "Boucle de réception de messages..." + RST);
        try {
            while (connected) {
                String message = this.receive();
                LOGGER.finest(RED + "Réception: " + message + RST);
                LOGGER.info(RED + message + RST);
                if (listener != null) {
                    LOGGER.log(Level.FINE, message);
                    Platform.runLater(() -> listener.handleEvent(Event.fromJson(message)));
                }
            }
        } catch (IOException e) {
            LOGGER.severe("[%s] %s".formatted(ipPort, e));
            connected = false;
            Platform.runLater(() -> listener.connectionButton.setSelected(false));
        } finally {
            close();
        }
    }

    /**
     * Getter du logger
     * @return Le logger utilisé par ChatClient
     */
    public Logger getLogger() {
        return LOGGER;
    }

    /**
     * Getter du listener
     * @return Le controller associé à la connexion
     */
    public ChatController getListener() {
        return listener;
    }

    /**
     * Envoi d'un message au travers d'un évènement "MSG", contenant l'objet `Message` adressé à un destinataire `to` avec un contenu `body`
     * @param msg Le message à envoyer
     */
    public void sendMessageEvent(Message msg) {
        sendEvent(new Event("MESG", msg.toJsonObject()));
    }

    /**
     * Envoi d'un post au travers d'un évènement "POST".
     * @param selectedItem Post à envoyer
     */
    public void sendPostEvent(Post selectedItem) {
        Event postEvent = new Event(Event.POST, selectedItem.toJsonObject());
        sendEvent(postEvent);
    }
}
