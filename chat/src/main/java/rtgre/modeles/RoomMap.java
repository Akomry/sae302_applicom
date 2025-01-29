package rtgre.modeles;

import java.util.HashSet;
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

    public void setLoginSets() {

        HashSet<String> juniors = new HashSet<>();
        juniors.add("riri");
        juniors.add("fifi");
        juniors.add("loulou");
        this.get("#juniors").setLoginSet(juniors);

        HashSet<String> ducks = new HashSet<>();
        ducks.add("riri");
        ducks.add("fifi");
        ducks.add("loulou");
        ducks.add("donald");
        ducks.add("daisy");
        ducks.add("picsou");
        this.get("#ducks").setLoginSet(ducks);

        HashSet<String> mice = new HashSet<>();
        mice.add("mickey");
        mice.add("minnie");
        this.get("#mice").setLoginSet(mice);

        HashSet<String> all = new HashSet<>();
        all.add("riri");
        all.add("fifi");
        all.add("loulou");
        all.add("donald");
        all.add("daisy");
        all.add("picsou");
        all.add("mickey");
        all.add("minnie");
        all.add("dingo");
        this.get("#all").setLoginSet(all);
    }
}
