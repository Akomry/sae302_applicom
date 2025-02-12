package rtgre.chat.graphisme;

import rtgre.chat.ChatController;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import rtgre.modeles.Contact;

import java.awt.image.BufferedImage;

import static rtgre.chat.ChatApplication.LOGGER;

/**
 * Classe modélisant la fabrique de cellule de la vue des contacts
 * visibles/connectés {@link ChatController#contactsListView}.
 *
 * @see ListCell
 */
public class ContactListViewCell extends ListCell<Contact> {

    /**
     * Callback déclenchée à chaque modification d'un objet d'une liste d'observable.
     *
     * @param contact Le contact à mettre à jour
     * @param empty La liste de cellule doit-elle être complètement remise à zéro ?
     */
    @Override
    protected void updateItem(Contact contact, boolean empty) {
        super.updateItem(contact, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            // Cas d'un contact
            updateContact(contact);
        }
    }

    /**
     * Mise à jour de la cellule d'un contact.
     *
     * @param contact  Le contact à mettre à jour
     */

    private void updateContact(Contact contact) {
        LOGGER.finest("Mise à jour de " + contact);

        String unreadCountNotif = (contact.getUnreadCount().getUnreadCount() == 0) ? "" : " (%d)".formatted(contact.getUnreadCount().getUnreadCount());
        LOGGER.finest("unread: %s %s".formatted(contact.getLogin(), unreadCountNotif));
        Text loginText = new Text(contact.getLogin() + unreadCountNotif);
        loginText.setFont(Font.font(null, 12)); // FontWeight.BOLD, 14));
        loginText.setFill(contact.isConnected() ? Color.BLACK : Color.GRAY);

        Circle circle = new Circle(5, 5, 5);
        circle.setFill(contact.isConnected() ? Color.CADETBLUE : Color.FIREBRICK);
        // circle.setOpacity(contact.is_connected() ? 1 : 0.5);

        Image avatar;
        ImageView view = new ImageView();

        if (contact.getAvatar() != null) {
            avatar = SwingFXUtils.toFXImage((BufferedImage) contact.getAvatar(), null);
            view = new ImageView(avatar);
        }
        view.setOpacity(contact.isConnected() ? 1 : 0.5);
        view.setFitWidth(15);
        view.setFitHeight(15);

        HBox temp = new HBox(circle);
        temp.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(temp, Priority.ALWAYS);
        HBox hBox = new HBox(view, loginText, temp);
        hBox.setSpacing(5.0);
        hBox.setAlignment(Pos.CENTER_LEFT);

        setGraphic(hBox);
    }


}