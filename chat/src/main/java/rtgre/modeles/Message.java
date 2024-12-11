package rtgre.modeles;
import org.json.JSONObject;


public class Message {
    /**
     * Un message décrit sous la forme :
     * @serialField : String to: Le destinataire
     * @serialField : String body: le corps du message
     */
    protected String to;
    protected String body;

    public Message(String to, String body) {

        this.to = to;
        this.body = body;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "to=" + to +
                ", body=" + body +
                '}';
    }

    public JSONObject toJsonObject() {
        /**
         * Transforme le message courant en objet JSON
         */
        return new JSONObject("{to:%s,body:%s}".formatted( this.to, this.body));
    }

    public String toJson() {
        /**
         * Transforme l'objet courant en String JSON
         */
        return toJsonObject().toString();
    }

    public static Message fromJson(JSONObject json) {
        /**
         * Crée un objet message à partir d'un objet JSON
         * @param: JSONObject json: l'objet JSON à transformer
         */
        return new Message(json.getString("to"), json.getString("body"));
    }
}
