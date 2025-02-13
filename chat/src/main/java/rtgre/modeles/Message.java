package rtgre.modeles;
import org.json.JSONObject;

/**
 * Classe modélisant un message adressé à un destinataire
 */
public class Message {
    /** Login du destinataire du message */
    protected String to;
    /** Contenu textuel du message */
    protected String body;

    /**
     * Constructeur par défaut
     * @param to Le destinataire du message
     * @param body Le contenu du message
     */
    public Message(String to, String body) {
        this.to = to;
        this.body = body;
    }

    /**
     * Getter de `to`
     * @return Le destinataire du message
     */
    public String getTo() {
        return to;
    }

    /**
     * Getter de `body`
     * @return Le message sous la forme d'une chaine de caractères
     */
    public String getBody() {
        return body;
    }

    /**
     * Représentation textuelle d'un message
     * @return La chaine de caractère représentant un message
     */
    @Override
    public String toString() {
        return "Message{" +
                "to=" + to +
                ", body=" + body +
                '}';
    }

    /**
     * Renvoie l'objet JSON représentant un message
     * @return L'objet JSON
     */
    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("to", this.to)
                .put("body", this.body);
    }

    /**
     * Sérialise dans une chaine de caractères la représentation JSON d'un message
     * @return La représentation textuelle associée à la représentation JSON d'un message
     */
    public String toJson() {
        return toJsonObject().toString();
    }

    /**
     * Message sur la base d'une représentation JSON
     * @param json La représentation JSON
     * @return Un message
     */
    public static Message fromJson(JSONObject json) {
        return new Message(json.getString("to"), json.getString("body"));
    }
}
