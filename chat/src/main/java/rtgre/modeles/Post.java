package rtgre.modeles;

import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * Classe modélisant un post échangé entre un émetteur et un destinataire privé ou public
 */
public class Post extends Message {

    /** Identifiant unique du post */
    protected UUID id;
    /** Horodatage du post exprimé en nombre de millisecondes depuis le 01/01/1970 */
    protected long timestamp;
    /** Login du contact qui a envoyé ce post */
    protected String from;


    /**
     * Constructeur par défaut
     * @param id L'identifiant du post
     * @param timestamp Le timestamp du post
     * @param from L'émetteur du post
     * @param to Le destinataire du post
     * @param body Le contenu du post
     */
    public Post(UUID id, long timestamp, String from, String to, String body) {
        super(to, body);
        this.id = id;
        this.timestamp = timestamp;
        this.from = from;
    }

    /**
     * Constructeur d'un post sur la base de son émetteur, son destinataire et son contenu. L'identifiant est choisi de manière unique aléatoirement ; le timestamp correspond à la date courante
     * @param from L'émetteur du post
     * @param to Le destinataire du post
     * @param body Le contenu du post
     */
    public Post(String from, String to, String body) {
        super(to, body);
        this.from = from;
        this.to = to;
        this.body = body;
        this.timestamp = new Date().getTime();
        this.id = UUID.randomUUID();
    }

    /**
     * Constructeur à partir d'un Message
     * @param from Login de l'émetteur
     * @param message Message (incluant le destinataire et le contenu)
     */
    public Post(String from, Message message) {
        super(message.to, message.body);
        this.from = from;
        this.to = message.to;
        this.body = message.body;
        this.timestamp = new Date().getTime();
        this.id = UUID.randomUUID();
    }

    /**
     * Egalité de deux posts, s'ils ont le même identifiant unique
     * @param o Le post auquel est comparé
     * @return true si sont égaux, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id.equals(post.id);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Getter de l'identifiant
     * @return L'identifiant
     */
    public UUID getId() {
        return id;
    }

    /**
     * Getter du timestamp
     * @return Le timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter de l'émeteur
     * @return L'émetteur
     */
    public String getFrom() {
        return from;
    }

    /**
     * Représentation textuelle du post
     * @return La représentation textuelle du post
     */
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    /**
     * Objet JSON représentant un post
     * @return La représentation JSON d'un post
     */
    @Override
    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("id", this.id)
                .put("timestamp", this.timestamp)
                .put("from", this.from)
                .put("to", this.to)
                .put("body", this.body);
    }

    /**
     * Sérialise dans une chaine de caractères la représentation JSON d'un objet.
     * @return La représentation textuelle associée à la représentation JSON d'un post
     */
    @Override
    public String toJson() {
        return toJsonObject().toString();
    }

    /**
     * Création d'un post à partir d'un objet JSON représentant un post
     * @param jsonObject L'objet JSON représentant un post
     * @return Le post créé
     */
    public static Post fromJson(JSONObject jsonObject) {
        return new Post(
                UUID.fromString(jsonObject.getString("id")),
                jsonObject.getLong("timestamp"),
                jsonObject.getString("from"),
                jsonObject.getString("to"),
                jsonObject.getString("body")
                );
    }
}
