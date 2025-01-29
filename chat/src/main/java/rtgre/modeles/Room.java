package rtgre.modeles;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Objects;

public class Room {
    protected String roomName;
    protected HashSet<String> loginSet;
    protected UnreadCount unreadCount = new UnreadCount();


    public String getRoomName() {
        return roomName;
    }

    public HashSet<String> getLoginSet() {
        return loginSet;
    }

    public Room(String roomName) {
        this.roomName = roomName;
        this.loginSet = null;
    }

    public String abbreviation() {
        return this.roomName.split("#")[1].substring(0, 1);
    }

    @Override
    public String toString() {
        return this.roomName;
    }

    public void setLoginSet (HashSet<String> loginSet) {
        this.loginSet = loginSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomName, room.roomName);
    }

    public void add(String login) {
        if (loginSet == null) {
            loginSet = new HashSet<>();
        }
        loginSet.add(login);
    }

    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("room", this.roomName)
                .put("loginSet", this.loginSet);
    }

    public String toJson() {
        return this.toJsonObject().toString();
    }

    public UnreadCount getUnreadCount() {
        return this.unreadCount;
    }
}
