module rtgre.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;


    opens rtgre.chat to javafx.fxml;
    exports rtgre.chat;
}