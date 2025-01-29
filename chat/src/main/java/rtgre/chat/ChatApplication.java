package rtgre.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ChatApplication extends Application {
    public static final Logger LOGGER = Logger.getLogger(ChatApplication.class.getCanonicalName());

    static {
        try {
            InputStream is = ChatApplication.class
                    .getResource("logging.properties").openStream();
            LogManager.getLogManager().readConfiguration(is);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot read configuration file", e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle i18nBundle = ResourceBundle.getBundle("rtgre.chat.i18nBundle",
                Locale.getDefault());
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"), i18nBundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Chat @BOUCLY_Emi (B2GA)");

        stage.getIcons().add(new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("rt.png"))));
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}