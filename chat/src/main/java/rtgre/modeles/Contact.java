package rtgre.modeles;


public class Contact {
    protected String login;
    protected java.awt.Image avatar;
    protected boolean connected;
    protected String currentRoom;

    Contact(String login, java.awt.Image avatar) {
        this.login = login;
        this.avatar = avatar;
        this.connected = false;
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
        return "";
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }


    public boolean equals(Object o) {return true;}
        /*if (this.login == o.login) {
            return true;
        } else {
            return false;
        }
    }*/

}
