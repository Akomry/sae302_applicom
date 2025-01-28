package rtgre;

import rtgre.chat.ChatApplication;
import rtgre.server.ChatServer;

import java.io.IOException;

public class ChatLauncher {
    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
        try {
            if ("server".equals(args[0])) {
                System.out.println("test1");
                ChatServer.main(args);
            } else {
                ChatApplication.main(args);
            }
        } catch (Exception e) {
            System.out.println("test2");
            ChatApplication.main(args);
        }

    }
}
