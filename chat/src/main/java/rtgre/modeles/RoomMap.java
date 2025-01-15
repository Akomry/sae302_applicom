package rtgre.modeles;

import java.util.TreeMap;

public class RoomMap extends TreeMap<String, Room> {
    public void add(Room room) {
        this.put(room.getRoomName(), room);
    }

    public void loadDefaultRooms() {
        this.add(new Room("#all"));
        this.add(new Room("#juniors"));
        this.add(new Room("#ducks"));
        this.add(new Room("#mice"));
    }
}
