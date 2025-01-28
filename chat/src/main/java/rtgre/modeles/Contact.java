package rtgre.modeles;


import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

import static rtgre.chat.ChatApplication.LOGGER;

public class Contact {
    protected String login;
    protected java.awt.Image avatar;
    protected boolean connected;
    protected String currentRoom;
    protected UnreadCount unreadCount = new UnreadCount();

    /**
     * Crée un objet Contact
     * @param: String login
     * @param: java.awt.Image avatar
     */
    public Contact(String login, java.awt.Image avatar) {
        this.login = login;
        this.avatar = avatar;
        this.connected = false;
        this.currentRoom = null;
    }

    /**
     * Crée un objet Contact
     * @param: String login
     * @param: boolean connected
     * @param: java.awt.Image avatar
     */
    public Contact(String login, boolean connected, java.awt.Image avatar) {
        this.login = login;
        this.avatar = avatar;
        this.connected = connected;
        this.currentRoom = null;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Crée un objet Contact
     * @param: String login
     * @param: boolean connected
     * @param: File banques_avatars
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


    public String getLogin() {
        return this.login;
    }

    public java.awt.Image getAvatar() {
        return this.avatar;
    }

    @Override
    public String toString() {
        return "@" + this.login;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

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

    public UnreadCount getUnreadCount() {
        return unreadCount;
    }

    public JSONObject toJsonObject() {
        return new JSONObject()
                .put("login", this.login)
                .put("connected", this.connected)
                .put("avatar", Contact.imageToBase64((BufferedImage) avatar));
    }

    public String toJson() {
        return toJsonObject().toString();
    }

    public static Contact fromJSON(JSONObject jsonObject, File banque_avatars) {
        return new Contact(jsonObject.getString("login"), jsonObject.getBoolean("connected"), banque_avatars);
    }

    public static BufferedImage avatarFromLogin(File fichier, String login) throws IOException {
        /**
         * Renvoie une sous-image en fonction d'une banque d'image et d'un login.
         * @param: File fichier
         * @param: String login
         */
        BufferedImage img = ImageIO.read(fichier);
        int width = img.getWidth() / 9;
        int height = img.getHeight();
        int n = Integer.remainderUnsigned(login.hashCode(), 9);
        return img.getSubimage(n*width, 0, width, height);
    }

    public void setAvatarFromFile(File f) {
        try {
            this.avatar = avatarFromLogin(f, this.login);
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    public void setAvatar(java.awt.Image avatar) {
        this.avatar = avatar;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

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

}
