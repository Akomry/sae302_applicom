package rtgre.modeles;


import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant un contact avec son `login`, son `avatar` et son état (connecté ou non)
 */
public class Contact {
    /** Le login du contact */
    protected String login;
    /** L'avatar du contact */
    protected java.awt.Image avatar;
    /** L'utilisateur est connecté ? */
    protected boolean connected;
    /** Le salon courant */
    protected String currentRoom;
    /** Le compteur de messages non-lus */
    protected UnreadCount unreadCount = new UnreadCount();

    /**
     * Crée un objet Contact
     * @param login Login du contact
     * @param avatar Avatar du contact au format java.awt.Image
     */
    public Contact(String login, java.awt.Image avatar) {
        this.login = login;
        this.avatar = avatar;
        this.connected = false;
        this.currentRoom = null;
    }

    /**
     * Crée un objet Contact
     * @param login Login du contact
     * @param connected Utilisateur connecté ?
     * @param avatar au format java.awt.Image
     */
    public Contact(String login, boolean connected, java.awt.Image avatar) {
        this.login = login;
        this.avatar = avatar;
        this.connected = connected;
        this.currentRoom = null;
    }

    /**
     * Getter de `currentRoom`
     * @return Le salon actuel
     */
    public String getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Crée un objet Contact
     * @param login Login du contact
     * @param connected Utilisateur connecté ?
     * @param banques_avatars Image contenant les avatars par défaut relatifs aux logins
     */
    public Contact(String login, boolean connected, File banques_avatars) {
        this.login = login;
        try {
            this.avatar = avatarFromLogin(banques_avatars, login);
        } catch (IOException e) {
            LOGGER.severe("Impossible de créer l'utilisateur " + login);
            LOGGER.severe(e.getMessage());
            LOGGER.severe(banques_avatars.getAbsolutePath());
        }
        this.connected = connected;
        this.currentRoom = null;
    }

    /**
     * Getter de `login`
     * @return Le login du contact
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Getter de `avatar`
     * @return L'avatar du contact au format java.awt.Image
     */
    public java.awt.Image getAvatar() {
        return this.avatar;
    }

    /**
     * Représentation textuelle de l'objet Contact
     * @return Le contact au format `@login`
     */
    @Override
    public String toString() {
        return "@" + this.login;
    }

    /**
     * Getter du booléen `connected`
     * @return Le statut de connexion du contact
     */
    public boolean isConnected() {
        return this.connected;
    }

    /**
     * Setter du booléen `connected`
     * @param connected Le statut de connexion du contact
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Egalité de deux contacts, s'ils ont le même login
     * @param o Le salon auquel est comparé
     * @return true si sont égaux, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(login, contact.login);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(login);
    }

    /**
     * Getter du compteur de messages non-lus
     * @return L'object `UnreadCount` du contact
     */
    public UnreadCount getUnreadCount() {
        return unreadCount;
    }

    /**
     * Sérialise le contact courant en objet JSON
     * @return L'objet JSONObject sérialisé
     */
    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("login", this.login)
                .put("connected", this.connected)
                .put("avatar", Contact.imageToBase64((BufferedImage) avatar));
    }

    /**
     * Sérialise le contact courant en chaîne de caractères au format JSON
     * @return un String au format JSON
     */
    public String toJson() {
        return toJsonObject().toString();
    }

    /**
     * Construit un objet Contact à partir d'un objet JSON et de la banque d'avatars
     * @param jsonObject L'objet JSON source
     * @param banque_avatars La banque d'avatars
     * @return Un objet Contact
     */
    public static Contact fromJSON(JSONObject jsonObject, File banque_avatars) {
        return new Contact(jsonObject.getString("login"), jsonObject.getBoolean("connected"), banque_avatars);
    }

    /**
     * Renvoie une sous-image en fonction d'une banque d'image et d'un login.
     * @param fichier La banque d'avatars
     * @param login Le login dont on cherche l'avatar
     * @return Un objet BufferedImage contenant l'image recherchée
     * @throws IOException si le fichier est introucable
     */
    public static BufferedImage avatarFromLogin(File fichier, String login) throws IOException {

        BufferedImage img = ImageIO.read(fichier);
        int width = img.getWidth() / 9;
        int height = img.getHeight();
        int n = Integer.remainderUnsigned(login.hashCode(), 9);
        return img.getSubimage(n*width, 0, width, height);
    }

    /**
     * Initialise l'avatar du contact courant en fonction de son login actuel et d'un fichier de banque d'avatars
     * @param file La banque d'avatars
     */
    public void setAvatarFromFile(File file) {
        try {
            this.avatar = avatarFromLogin(file, this.login);
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    /**
     * Setter de `avatar`
     * @param avatar L'avatar au format java.awt.Image
     */
    public void setAvatar(java.awt.Image avatar) {
        this.avatar = avatar;
    }

    /**
     * Setter de currentRoom
     * @param currentRoom Le salon courant
     */
    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    /**
     * Transforme une image au format java.awt.Image ou BufferedImage en une image encodée en base64.
     * @param img L'image en java.awt.Image ou en BufferedImage
     * @return L'image encodée en base64 si l'image est chargée correctement, un String vide sinon
     */
    public static String imageToBase64(BufferedImage img) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", bos);
            System.out.println(Base64.getEncoder().encodeToString(bos.toByteArray()));
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            LOGGER.severe("Impossible de convertir l'image en base64");
        }
        return "";
    }

    /**
     * Transforme une image encodée en base64 en une image au format java.awt.Image.
     * @param avatar64 L'image encodée en base64
     * @return L'image en java.awt.Image
     */
    public static java.awt.Image base64ToImage(String avatar64) {
        byte[] bytes64 = Base64.getDecoder().decode(avatar64);
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes64));
            return image;
        } catch (IOException e) {
            LOGGER.severe("Impossible de convertir le base64 en image");
        }
        return null;
    }

    /**
     * Transforme une image encodée en base64 en une image au format BufferedImage.
     * @param avatar64 L'image encodée en base64
     * @return L'image en BufferedImage
     */
    public static BufferedImage base64ToBufferedImage(String avatar64) {
        byte[] bytes64 = Base64.getDecoder().decode(avatar64);
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes64));
            return image;
        } catch (IOException e) {
            LOGGER.severe("Impossible de convertir le base64 en image");
        }
        return null;
    }


}
