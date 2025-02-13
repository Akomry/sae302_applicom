package rtgre.chat.graphisme;

import rtgre.chat.ChatController;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import rtgre.modeles.Contact;
import rtgre.modeles.Post;

import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * Classe modélisant la fabrique de cellule de la vue des posts
 * {@link ChatController#postListView}.
 *
 * @see ListCell
 */
public class PostListViewCell extends ListCell<Post> {

    /** Controller de l'application */
    ChatController controller;

    /**
     * Constructeur par défaut
     * @param controller Le controller de l'application grapihque
     */
    public PostListViewCell(ChatController controller) {
        this.controller = controller;
    }

    /**
     * Callback déclenchée à chaque modification d'un objet d'une liste d'observable.
     *
     * @param post Le post
     * @param empty La liste de cellule doit-elle être complètement remise à zéro ?
     */
    @Override
    protected void updateItem(Post post, boolean empty) {
        super.updateItem(post, empty);
        if (empty) {
            setGraphic(null);
        }
        else {
            updatePost(post);
        }
    }

    /**
     * Mise à jour de la cellule d'un post.
     *
     * @param post Le post à mettre à jour
     */
    void updatePost(Post post) {

        Text datetimeText = new Text("\n%1$td/%1$tm/%1$tY %1$tH:%1$tM:%1$tS\n".formatted(new Date(post.getTimestamp())));
        datetimeText.setFont(Font.font(null, FontPosture.ITALIC, 8));

        Text nicknameText = new Text(post.getFrom() + ": ");
        nicknameText.setFill(Color.DARKBLUE);
        nicknameText.setFont(Font.font(null, FontWeight.BOLD, 14));
        nicknameText.setFill(Color.BLUEVIOLET);

        Text msgText = new Text(post.getBody());

        // L'émetteur du message
        Contact c = this.controller.getContactsMap().get(post.getFrom());
        ImageView avatar = new ImageView();
        if (c != null) {
            avatar = new ImageView(SwingFXUtils.toFXImage((BufferedImage) c.getAvatar(), null));
            avatar.setFitWidth(20);
            avatar.setFitHeight(20);
        }

        TextFlow tf = new TextFlow((Node) datetimeText, avatar, nicknameText, msgText);
        tf.maxWidthProperty().bind(getListView().widthProperty().multiply(0.8));
        HBox hBox = new HBox(tf);
        hBox.maxWidthProperty().bind(getListView().widthProperty());
        String login;

        try {
            login = this.controller.getContact().getLogin();
        }
        catch (Exception e) {
            login = "???";
        }

        if (post.getFrom().equals(login)) {
            tf.setBackground(Background.fill(Color.web("#EEF")));
            hBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            tf.setBackground(Background.fill(Color.web("#FEE")));
            hBox.setAlignment(Pos.CENTER_LEFT);
        }
        if (!post.isEditable()) {
            tf.setBackground(Background.fill(Color.web("#808080")));
        }
        setGraphic(hBox);
        getListView().scrollTo(getListView().getItems().size() - 1);
    }
}