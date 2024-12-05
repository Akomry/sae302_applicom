package rtgre.chat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static rtgre.chat.ChatApplication.LOGGER;
public class ChatController implements Initializable {

    public MenuItem hostAddMenuItem;
    public MenuItem avatarMenuItem;
    public MenuItem aboutMenuItem;
    public ComboBox hostComboBox;
    public TextField loginTextField;
    public ToggleButton connectionButton;
    public ImageView avatarImageView;
    public SplitPane exchangeSplitPane;
    public ListView postListView;
    public ListView roomsListView;
    public ListView contactListView;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation de l'interface graphique");
    }
}