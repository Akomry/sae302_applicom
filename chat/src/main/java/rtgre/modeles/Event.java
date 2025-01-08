package rtgre.modeles;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {
    public static final String AUTH = "AUTH";
    public static final String QUIT = "QUIT";
    public static final String MESG = "MESG";
    public static final String JOIN = "JOIN";
    public static final String POST = "POST";
    public static final String CONT = "CONT";
    public static final String LIST_CONTACTS = "LSTC";
    public static final String LIST_POSTS = "LSTP";
    public static final String SYSTEM = "SYST";
    public static final String LIST_ROOMS = "LSTR";
    public static final String ROOM = "ROOM";
    private final String type;
    private final JSONObject content;

    public String getType() {
        return type;
    }

    public JSONObject getContent() {
        return content;
    }

    public Event(String type, JSONObject content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Event{type=" + type + ", content=" + content.toString() + "}";
    }

    public JSONObject toJsonObject() {
        return new JSONObject().put("type", type).put("content", content);
    }

    public String toJson() {
        return toJsonObject().toString();
    }

    public static Event fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String type = jsonObject.getString("type");
        JSONObject content = jsonObject.getJSONObject("content");
        return new Event(type, content);
    }
}

