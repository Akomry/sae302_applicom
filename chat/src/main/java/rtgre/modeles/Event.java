package rtgre.modeles;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe modélisant un évènement permettant un échange client/serveur pour une application de chat.
 */
public class Event {
    /** Type de l'évènement client -> serveur : authentification */
    public static final String AUTH = "AUTH";
    /** Type de l'évènement client -> serveur : déconnexion */
    public static final String QUIT = "QUIT";
    /** Type de l'évènement client -> serveur : envoi d'un message */
    public static final String MESG = "MESG";
    /** Type de l'évènement client -> serveur : rejoindre un salon */
    public static final String JOIN = "JOIN";
    /** Type de l'évènement serveur -> client : envoi d'un post */
    public static final String POST = "POST";
    /** Type de l'évènement serveur -> client : informations sur un contact */
    public static final String CONT = "CONT";
    /** Type de l'évènement client -> serveur : demander la liste des contacts */
    public static final String LIST_CONTACTS = "LSTC";
    /** Type de l'évènement client -> serveur : demander la liste des posts */
    public static final String LIST_POSTS = "LSTP";
    /** Type de l'évènement système interne au client */
    public static final String SYSTEM = "SYST";
    /** Type de l'évènement client -> serveur : demander la liste des salons */
    public static final String LIST_ROOMS = "LSTR";
    /** Type de l'évènement serveur -> client : informations sur un salon de discussion */
    public static final String ROOM = "ROOM";
    /** Le type d'évènement de l'Event courant */
    private final String type;
    /** Le contenu de l'Event courant */
    private final JSONObject content;


    /**
     * Getter du type
     * @return Le type de l'évènement
     */
    public String getType() {
        return type;
    }

    /**
     * Getter du contenu
     * @return Le contenu de l'évènement
     */
    public JSONObject getContent() {
        return content;
    }

    /**
     * Constructeur par défaut à partir d'un type et d'un objet JSON servant de contenu
     * @param type Le type de l'évènement
     * @param content Le contenu de l'évènement
     */
    public Event(String type, JSONObject content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Représentation textuelle de l'objet
     * @return Chaine de caractères représentant l'évènement
     */
    @Override
    public String toString() {
        return "Event{type=" + type + ", content=" + content.toString() + "}";
    }

    /**
     * Renvoie l'objet JSON représentant l'évènement
     * @return L'objet JSON représentant l'évènement
     */
    public JSONObject toJsonObject() {
        return new JSONObject().put("type", type).put("content", content);
    }

    /**
     * Sérialisation de la représentation JSON d'un évènement
     * @return La chaine de caractères représentant l'évènement
     */
    public String toJson() {
        return toJsonObject().toString();
    }

    /**
     * Méthode de classe instanciant et renvoyant un évènement à partir d'une chaine de caractères représentant un évènement JSON
     * @param json La représentation JSON d'un évènement
     * @return L'évènement associé
     * @throws JSONException s'il y a une erreur dans la sérialisation
     */
    public static Event fromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        String type = jsonObject.getString("type");
        JSONObject content = jsonObject.getJSONObject("content");
        return new Event(type, content);
    }
}

