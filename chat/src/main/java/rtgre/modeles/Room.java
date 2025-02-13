package rtgre.modeles;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Objects;

/**
 * Classe modélisant un salon avec son nom et sa liste d'utilisateurs autorisés.
 */
public class Room {
    /** Le nom du salon */
    protected String roomName;
    /** La liste des utilisateurs autorisés à rejoindre le salon. Si `null`, tout le monde peut poster */
    protected HashSet<String> loginSet;
    /** Le compteur de messages non-lus */
    protected UnreadCount unreadCount = new UnreadCount();


    /**
     * Le getter associé à roomName
     * @return Le nom du salon
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Le getter associé au loginSet
     * @return La liste des utilisateurs autorisés à rejoindre le salon
     */
    public HashSet<String> getLoginSet() {
        return loginSet;
    }

    /**
     * Constructeur par défaut
     * @param roomName Le nom du salon
     */
    public Room(String roomName) {
        this.roomName = roomName;
        this.loginSet = null;
    }

    /**
     * Abréviation du nom du salon avec la 1re lettre de son nom
     * @return La première lettre du nom du salon (# non compris)
     */
    public String abbreviation() {
        return this.roomName.split("#")[1].substring(0, 1);
    }

    /**
     * Représentation textuelle d'un salon
     * @return Le nom du salon
     */
    @Override
    public String toString() {
        return this.roomName;
    }

    /**
     * Setter de LoginSet
     * @param loginSet La liste des utilisateurs autorisés à se connecter au salon
     */
    public void setLoginSet (HashSet<String> loginSet) {
        this.loginSet = loginSet;
    }

    /**
     * Egalité de deux salons, s'ils ont le même nom
     * @param o Le salon auquel est comparé
     * @return true si sont égaux, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(roomName, room.roomName);
    }

    /**
     * Ajoute un login au contact du salon (en créant le loginSet au besoin)
     * @param login Le login du contact à ajouter au salon
     */
    public void add(String login) {
        if (loginSet == null) {
            loginSet = new HashSet<>();
        }
        loginSet.add(login);
    }

    /**
     * Représentation JSON d'un salon, incluant son nom et la liste des utilisateurs autorisés à y poster
     * @return La représentation JSON
     */
    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("room", this.roomName)
                .put("loginSet", this.loginSet);
    }

    /**
     * Chaine de caractères associée à la représentation JSON d'un contact
     * @return La chaine de caractères correspondant à la représentation JSON
     */
    public String toJson() {
        return this.toJsonObject().toString();
    }

    /**
     * Getter de l'unreadCount
     * @return Le compteur de messages non-lus
     */
    public UnreadCount getUnreadCount() {
        return this.unreadCount;
    }
}
