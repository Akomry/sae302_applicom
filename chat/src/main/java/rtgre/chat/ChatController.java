package rtgre.chat;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Date;
import java.util.Objects;
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
    public TextField messageTextField;
    public Button sendButton;
    public Label statusLabel;
    public Label dateTimeLabel;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialisation de l'interface graphique");

        Image image = new Image(Objects.requireNonNull(ChatController.class.getResourceAsStream("anonymous.png")));
        this.avatarImageView.setImage(image);

        Thread dateTimeLoop = new Thread(this::dateTimeLoop);
        dateTimeLoop.setDaemon(true);
        dateTimeLoop.start();

        hostComboBox.getItems().addAll("localhost:2024");
        hostComboBox.getItems().addAll("localhost:2025");
        hostComboBox.setValue("localhost:2024");
        hostComboBox.setOnAction(this::statusNameUpdate);

        statusLabel.setText("not connected to " + hostComboBox.getValue());



    }

    private void statusNameUpdate(Event event) {
        statusLabel.setText("not connected to " + hostComboBox.getValue());

    }


    private void dateTimeLoop() {
        while (true) {
            try {
                String datetime = "%1$ta %1$te %1$tb %1$tY - %1$tH:%1$tM".formatted(new Date());
                Platform.runLater(() -> dateTimeLabel.setText(datetime));
                Thread.sleep(60000);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    }
}