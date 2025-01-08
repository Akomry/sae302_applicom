package rtgre.modeles;
import java.util.TreeMap;

public class ContactMap extends TreeMap<String, Contact> {
    public void add(Contact contact) {
        this.put(contact.login, contact);
    }

    public Contact getContact(String login) {
        return this.get(login);
    }

    public void loadDefaultContacts() {
        this.put("mickey", new Contact("mickey", null));
        this.put("minnie", new Contact("minnie", null));
        this.put("dingo", new Contact("dingo", null));
        this.put("riri", new Contact("riri", null));
        this.put("fifi", new Contact("fifi", null));
        this.put("loulou", new Contact("loulou", null));
        this.put("donald", new Contact("donald", null));
        this.put("daisy", new Contact("daisy", null));
        this.put("picsou", new Contact("picsou", null));
    }
}
