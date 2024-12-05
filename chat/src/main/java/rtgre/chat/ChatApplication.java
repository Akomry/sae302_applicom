package rtgre.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class ChatApplication extends Application {
    public static final Logger LOGGER = Logger.getLogger(ChatApplication.class.getCanonicalName());
    public class EssaiLogger {
        /* . . . */
        static {
            try {
                InputStream is = EssaiLogger.class.getClassLoader()
                        .getResource("logging.properties").openStream();
                LogManager.getLogManager().readConfiguration(is);
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Cannot read configuration file", e);
            }
        }
        /* . . . */
    }
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Chat @BOUCLY_Emi (B2GA)");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}