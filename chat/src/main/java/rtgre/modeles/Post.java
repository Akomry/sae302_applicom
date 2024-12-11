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
        this.to = to;
        this.body = body;
        this.timestamp = new Date().getTime();
        this.id = UUID.randomUUID();
    }

    public Post(String from, Message message) {
        super(message.to, message.body);
        this.from = from;
        this.to = message.to;
        this.body = message.body;
        this.timestamp = new Date().getTime();
        this.id = UUID.randomUUID();
    }

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
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("id", this.id)
                .put("timestamp", this.timestamp)
                .put("from", this.from)
                .put("to", this.to)
                .put("body", this.body);
    }

    @Override
    public String toJson() {
        return toJsonObject().toString();
    }

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
