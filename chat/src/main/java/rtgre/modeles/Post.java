package rtgre.modeles;

import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;
public class Post extends Message {

    protected UUID id;
    protected long timestamp;
    protected String from;


    public Post(UUID id, long timestamp, String from, String to, String body) {
        /**
         * Crée un objet Post
         * @param: UUID id
         * @param: long timestamp
         * @param: String from
         * @param: String to,
         * @param: String body
         */
        super(to, body);
        this.id = id;
        this.timestamp = timestamp;
        this.from = from;
    }

    public Post(String from, String to, String body) {
        super(to, body);
        this.from = from;
        this.id = UUID.randomUUID();
        this.timestamp = new Date().getTime();
    }

    public UUID getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", from=" + from +
                ", to=" + to +
                ", body=" + body +
                '}';
    }

    @Override
    public String toJson() {
        JSONObject json = toJsonObject()
                .put("id", this.id)
                .put("timestamp", this.timestamp)
                .put("from", this.from);
        return json.toString();
    }

    public static Post fromJson(JSONObject jsonObject) {
        return new Post(
                (UUID) jsonObject.get("id"),
                jsonObject.getLong("timestamp"),
                jsonObject.getString("from"),
                jsonObject.getString("to"),
                jsonObject.getString("body")
                );
    }
}
