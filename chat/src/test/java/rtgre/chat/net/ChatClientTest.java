package rtgre.chat.net;


import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class ChatClientTest {

    static Class classe = ChatClient.class;
    static String module = "rtgre.chat.net";

    @DisplayName("01-Structure de ChatClient")
    @Nested
    class StructureTest {

        static List<String> methodesSignatures;
        static List<String> constructeursSignatures;

        @BeforeAll
        static void init() {
            // Les méthodes
            Method[] methodes = classe.getDeclaredMethods();
            methodesSignatures = Arrays.asList(methodes).stream().map(e -> e.toString()).collect(Collectors.toList());
            Constructor<?>[] constructeurs = classe.getConstructors();
            constructeursSignatures = Arrays.asList(constructeurs).stream().map(e -> e.toString()).collect(Collectors.toList());
        }


        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("listener", "rtgre.chat.ChatController", Modifier.PRIVATE | Modifier.FINAL)
            );
        }
        @DisplayName("Déclaration des attributs")
        @ParameterizedTest
        @MethodSource("attributsProvider")
        void testDeclarationAttributs(String nom, String type, int modifier) throws NoSuchFieldException {
            Field field = classe.getDeclaredField(nom);
            Assertions.assertEquals(type, field.getType().getName(),
                    "Type " + nom + " erroné : doit être " + type);
            Assertions.assertEquals(modifier, field.getModifiers(),
                    "Visibilité " + nom + " erronée : doit être " + modifier);

        }

        static Stream<Arguments> constructeursProvider() {
            return Stream.of(
                    arguments("public %s.ChatClient(java.lang.String,int,rtgre.chat.ChatController) throws java.io.IOException")
            );
        }
        @DisplayName("Déclaration des constructeurs")
        @ParameterizedTest
        @MethodSource("constructeursProvider")
        void testConstructeurs1(String signature) {
            Assertions.assertTrue(constructeursSignatures.contains(String.format(signature, module)),
                    String.format("Constructeur non déclaré : doit être %s\nalors que sont déclarés %s",
                            signature, constructeursSignatures));

        }

        static Stream<Arguments> methodesProvider() {
            return Stream.of(
                    arguments("receiveLoop", "public void %s.ChatClient.receiveLoop()"),
                    arguments("getLogger", "public java.util.logging.Logger %s.ChatClient.getLogger()")
            );
        }
        @DisplayName("Déclaration des méthodes")
        @ParameterizedTest
        @MethodSource("methodesProvider")
        void testDeclarationMethodes1(String nom, String signature) {
            Assertions.assertTrue(methodesSignatures.contains(String.format(signature, module, module)),
                    String.format("Méthode non déclarée : doit être %s\nalors que sont déclarés %s",
                            signature, methodesSignatures));
        }

    }


    @DisplayName("02-Connexion/déconnexion (port=1810)")
    @Nested
    class ConnexionTest {
        static int port = 1810;
        static LocalServer server;
        static ExecutorService executorService;

        @BeforeAll
        static void init() throws IOException {
            server = new LocalServer(port);
            executorService = new ThreadPoolExecutor(1, 1, 2,
                    TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
            executorService.submit(server::acceptClients);
        }

        @AfterAll
        static void close() throws IOException {
            server.passiveSocket.close();
            executorService.shutdown();
        }

        @DisplayName("Boucle de réception")
        @Test
        void testReceiveLoop() throws IOException, InterruptedException {

            ChatClient client = new ChatClient("localhost", port, null);
            Logger logger = client.getLogger();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Handler handler = new StreamHandler(out, new SimpleFormatter());
            logger.addHandler(handler);


            server.clientOut.println("bonjour");
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            handler.flush();
            String logMsg = out.toString();
            client.close();
            Assertions.assertTrue(logMsg.contains("bonjour"),
                    "Le message reçu doit être loggué");
        }

    }

}

class LocalServer {
    public ServerSocket passiveSocket;
    public Socket clientSocket;
    public PrintStream clientOut;
    public BufferedReader clientIn;
    public boolean stop = false;

    public LocalServer(int port) throws IOException {
        passiveSocket = new ServerSocket(port);
    }

    public void acceptClients() {
        while (!stop) {
            try {
                clientSocket = passiveSocket.accept();
                clientOut = new PrintStream(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8);
                clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8), 2048);
            } catch (IOException e) {
                stop = true;
            }
        }
    }
}