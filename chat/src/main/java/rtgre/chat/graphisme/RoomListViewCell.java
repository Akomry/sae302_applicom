package rtgre.chat.graphisme;

import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import rtgre.chat.ChatController;
import rtgre.modeles.Room;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant la fabrique de cellule de la vue des salons
 * {@link ChatController#roomsListView}.
 *
 * @see ListCell
 */
public class RoomListViewCell extends ListCell<Room> {

    /**
     * Callback déclenchée à chaque modification d'un objet d'une liste d'observable.
     *
     * @param room Le salon
     * @param empty La liste de cellule doit-elle être complètement remise à zéro ?
     */
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


    /**
     * Couleur d'un salon, choisie parmi une banque de couleurs, en fonction d'un nom de salon.
     * @param roomName Nom du salon
     * @return La couleur associée à la première lettre du nom du salon
     */
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

    /**
     * Mise à jour de la cellule d'un salon.
     *
     * @param room Le salon à mettre à jour
     */
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

        HBox temp = new HBox(stack);
        temp.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(roomText, Priority.ALWAYS);
        HBox hBox = new HBox(temp, roomText, view);
        hBox.setSpacing(5.0);
        hBox.setAlignment(Pos.CENTER_LEFT);

        setGraphic(hBox);
    }


}
