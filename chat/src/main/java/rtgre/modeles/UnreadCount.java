package rtgre.modeles;

/**
 * Classe modélisant le compteur de messages non-lus
 */
public class UnreadCount {
    /** Le compteur */
    private int unreadCount = 0;

    /**
     * Incrémente le compteur de posts reçus, mais non lus unreadCount et renvoie sa valeur.
     * @return Le compteur après incrémentation
     */
    public int incrementUnreadCount() {
        unreadCount += 1;
        return unreadCount;
    }

    /**
     * Setter de unreadCount
     * @param unreadCount La valeur à donner à unreadCount
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Getter de unreadCount
     * @return Le compteur
     */
    public int getUnreadCount() {
        return unreadCount;
    }
}
