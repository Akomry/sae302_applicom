package rtgre.modeles;

public class UnreadCount {
    private int unreadCount = 0;

    public int incrementUnreadCount() {
        unreadCount += 1;
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }
}
