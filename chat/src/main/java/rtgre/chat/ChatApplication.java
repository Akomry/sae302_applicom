package rtgre.chat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Application graphique de chat
 */
public class ChatApplication extends Application {
    /** Logger de l'application graphique */
    public static final Logger LOGGER = Logger.getLogger(ChatApplication.class.getCanonicalName());
    /** Controller de l'application de chat */
    private ChatController controller;
    /** Stage principal */
    private Stage stage;
    static {
        try {
            InputStream is = ChatApplication.class
                    .getResource("logging.properties").openStream();
            LogManager.getLogManager().readConfiguration(is);
            if (!Files.exists(new File("target/").toPath())) {
                Files.createDirectory(new File("target").toPath());
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Cannot read configuration file", e);
        }
    }

    /**
     * Lancement de l'application
     * @param stage La scène graphique
     * @throws IOException En cas de problème d'accès aux ressources
     */
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
        this.controller = fxmlLoader.getController();
        this.stage = stage;

        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("config.properties"));
            if (!properties.getProperty("width").isEmpty() && !properties.getProperty("height").isEmpty()) {
                stage.setWidth(Double.parseDouble(properties.getProperty("width")));
                stage.setHeight(Double.parseDouble(properties.getProperty("height")));
            }
            if (properties.getProperty("posx").isEmpty() || properties.getProperty("height").isEmpty()) {
                stage.centerOnScreen();
            } else {
                stage.setX(Double.parseDouble(properties.getProperty("posx")));
                stage.setY(Double.parseDouble(properties.getProperty("posy")));
            }
        } catch (IOException e) {
            LOGGER.warning("Cannot load stage config!");
        }
    }

    /**
     * Fermeture de l'application
     */
    @Override
    public void stop() {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream("config.properties"));
            properties.setProperty("width", String.valueOf(stage.getWidth()));
            properties.setProperty("height", String.valueOf(stage.getHeight()));
            properties.setProperty("posx", String.valueOf(stage.getX()));
            properties.setProperty("posy", String.valueOf(stage.getY()));

            properties.setProperty("split1", String.valueOf(controller.senderSplitPane.getDividerPositions()[0]));
            properties.setProperty("split2", String.valueOf(controller.exchangeSplitPane.getDividerPositions()[0]));
            LOGGER.finest(properties.toString());
            properties.store(new FileOutputStream(getClass().getResource("config.properties").getPath()), null);
        } catch (IOException e) {
            LOGGER.warning("Cannot store stage info in config!");
        }
    }

    /**
     * Programme principal
     * @param args Les arguments du programme principal
     */
    public static void main(String[] args) {
        launch();
    }
}