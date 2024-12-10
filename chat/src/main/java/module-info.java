module rtgre.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires java.desktop;
    requires javafx.swing;
    requires net.synedra.validatorfx;


    opens rtgre.chat to javafx.fxml;
    exports rtgre.chat;
    exports rtgre.chat.graphisme;
    opens rtgre.chat.graphisme to javafx.fxml;
}