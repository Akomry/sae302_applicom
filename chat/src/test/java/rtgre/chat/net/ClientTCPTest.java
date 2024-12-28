package rtgre.chat.net;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import rtgre.chat.net.ClientTCP;


import java.io.IOException;
import java.lang.reflect.*;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import static org.junit.jupiter.params.provider.Arguments.arguments;

class ClientTCPTest {

    static Class classe = ClientTCP.class;
    static String module = "rtgre.chat.net";


    @DisplayName("01-Structure de ClientTCP")
    @Nested
    class StructureTest {
        static Method[] methodes;
        static List<String> methodesSignatures;
        static List<String> constructeursSignatures;

        @BeforeAll
        static void init() {
            // Les méthodes
            methodes = classe.getDeclaredMethods();
            methodesSignatures = Arrays.asList(methodes).stream().map(e -> e.toString()).collect(Collectors.toList());
            Constructor<?>[] constructeurs = classe.getConstructors();
            constructeursSignatures = Arrays.asList(constructeurs).stream().map(e -> e.toString()).collect(Collectors.toList());
        }


        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("sock", "java.net.Socket", Modifier.PROTECTED),
                    arguments("out", "java.io.PrintStream", Modifier.PROTECTED),
                    arguments("in", "java.io.BufferedReader", Modifier.PROTECTED),
                    arguments("ipPort", "java.lang.String", Modifier.PROTECTED),
                    arguments("connected", "boolean", Modifier.PROTECTED)
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
                    arguments("public %s.ClientTCP(java.lang.String,int) throws java.io.IOException")
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
                    arguments("isConnected", "public boolean %s.ClientTCP.isConnected()"),
                    arguments("send", "public void %s.ClientTCP.send(java.lang.String) throws java.io.IOException"),
                    arguments("receive", "public java.lang.String %s.ClientTCP.receive() throws java.io.IOException"),
                    arguments("close", "public void %s.ClientTCP.close()")
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

    @DisplayName("02-Connexion/déconnexion (port=1800)")
    @Nested
    class ConnexionTest {
        static int port = 1800;
        static ServerSocket passiveSocket;

        @BeforeAll
        static void init() throws IOException {
            passiveSocket = new ServerSocket(1800);
        }

        @AfterAll
        static void close() throws IOException {
            passiveSocket.close();
        }

        @DisplayName("Connexion+deconnexion")
        @Test
        void testConnexion() throws IOException {
            ClientTCP client = new ClientTCP("localhost", port);
            Assertions.assertNotNull(client, "Connexion impossible");
            Assertions.assertTrue(client.isConnected(), "Etat de connexion erroné");
            client.close();
            Assertions.assertFalse(client.isConnected(), "Etat de connexion erroné");
        }

    }

}