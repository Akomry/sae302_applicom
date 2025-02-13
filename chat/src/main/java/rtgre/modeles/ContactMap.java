package rtgre.modeles;
import java.util.TreeMap;

/**
 * Modélise un annuaire des contacts sous la forme d’un tableau associatif clé=“login” ⇒ valeur=“objet Contact”. Cela permet de trouver rapidement un contact à partir de son login.
 */
public class ContactMap extends TreeMap<String, Contact> {
    /**
     * Ajoute un contact à l'objet `ContactMap` en utilisant pour clé le login du contact et pour valeur le contact lui-même.
     * @param contact Le contact à ajouter
     */
    public void add(Contact contact) {
        this.put(contact.login, contact);
    }

    /**
     * Récupère un contact en fonction de son login
     * @param login Le login duquel découle le contact recherché
     * @return Le contact associé au login
     */
    public Contact getContact(String login) {
        return this.get(login);
    }

    /**
     * Charge les contacts par défaut gérés par le serveur : "mickey", "minnie", "dingo", "riri", "fifi", "loulou", "donald", "daisy", "picsou", chaque contact ayant un avatar par défaut.
     */
    public void loadDefaultContacts() {
        String base64Img = "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";
        java.awt.Image image = Contact.base64ToImage(base64Img);

        this.put("mickey", new Contact("mickey", image));
        this.put("minnie", new Contact("minnie", image));
        this.put("dingo", new Contact("dingo", image));
        this.put("riri", new Contact("riri", image));
        this.put("fifi", new Contact("fifi", image));
        this.put("loulou", new Contact("loulou", image));
        this.put("donald", new Contact("donald", image));
        this.put("daisy", new Contact("daisy", image));
        this.put("picsou", new Contact("picsou", image));
    }
}
