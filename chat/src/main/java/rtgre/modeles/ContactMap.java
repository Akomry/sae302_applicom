package rtgre.modeles;
import java.util.TreeMap;

public class ContactMap extends TreeMap<String, Contact> {
    public void add(Contact contact) {
        this.put(contact.login, contact);
    }

    public Contact getContact(String login) {
        return this.get(login);
    }
}
