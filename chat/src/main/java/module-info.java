module rtgre.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens rtgre.chat to javafx.fxml;
    exports rtgre.chat;
}