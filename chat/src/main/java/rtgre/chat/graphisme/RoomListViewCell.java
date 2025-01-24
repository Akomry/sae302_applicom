package rtgre.chat.graphisme;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import rtgre.modeles.Contact;
import rtgre.modeles.Room;

import java.awt.image.BufferedImage;

import static rtgre.chat.ChatApplication.LOGGER;

public class RoomListViewCell extends ListCell<Room> {

    @Override
    protected void updateItem(Room room, boolean empty) {
        super.updateItem(room, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            // Cas d'un contact
            updateRoom(room);
        }
    }


    public Color colorFromName(String roomName) {
        switch (roomName) {
            case "#all":
                return Color.CADETBLUE;
            case "#juniors":
                return Color.FORESTGREEN;
            case "#ducks":
                return Color.GOLD;
            case "#mice":
                return Color.LIGHTPINK;
            default:
                return Color.GRAY;
        }
    }

    private void updateRoom(Room room) {
        LOGGER.finest("Mise à jour de " + room);

        String unreadCountNotif = (room.getUnreadCount().getUnreadCount() == 0) ? "" : " (%d)".formatted(room.getUnreadCount().getUnreadCount());
        LOGGER.finest("unread: %s %s".formatted(room.getRoomName(), unreadCountNotif));
        Text roomText = new Text(room.getRoomName() + unreadCountNotif);
        roomText.setFont(Font.font(null, 12)); // FontWeight.BOLD, 14));
        roomText.setFill(Color.BLACK);


        ImageView view = new ImageView();
        Rectangle rectangle = new Rectangle(15, 15, colorFromName(room.getRoomName()));
        rectangle.setArcHeight(8.0d);
        rectangle.setArcWidth(8.0d);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(rectangle, new Text(room.abbreviation()));
        /*
        if (contact.getAvatar() != null) {
            avatar = SwingFXUtils.toFXImage((BufferedImage) contact.getAvatar(), null);
            view = new ImageView(avatar);
        }*/
        HBox temp = new HBox(stack);
        temp.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(roomText, Priority.ALWAYS);
        HBox hBox = new HBox(temp, roomText, view);
        hBox.setSpacing(5.0);
        hBox.setAlignment(Pos.CENTER_LEFT);

        setGraphic(hBox);
    }


}
