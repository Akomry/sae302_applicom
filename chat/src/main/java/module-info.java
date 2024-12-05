module rtgre.chat {
    requires javafx.controls;
    requires javafx.fxml;


    opens rtgre.chat to javafx.fxml;
    exports rtgre.chat;
}