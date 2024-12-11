package rtgre.modeles;

import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MessageTest {

    static Class<?> classe = Message.class;
    static String module = "rtgre.modeles";


    @DisplayName("01-Structure")
    @Nested
    class StructureTest {
        static List<String> constructeursSignatures;
        static List<String> methodesSignatures;

        @BeforeAll
        static void init() {
            Constructor<?>[] constructeurs = classe.getConstructors();
            constructeursSignatures = Arrays.stream(constructeurs).map(Constructor::toString).collect(Collectors.toList());
            Method[] methodes = classe.getDeclaredMethods();
            methodesSignatures = Arrays.stream(methodes).map(Method::toString).collect(Collectors.toList());
        }

        /**
         * Attributs
         */
        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("to", "java.lang.String", Modifier.PROTECTED),
                    arguments("body", "java.lang.String", Modifier.PROTECTED)
            );
        }

        @DisplayName("Déclaration des attributs : nom, type et visibilité")
        @ParameterizedTest
        @MethodSource("attributsProvider")
        void testDeclarationAttributs(String nom, String type, int modifier) throws NoSuchFieldException {
            Field field = classe.getDeclaredField(nom);
            Assertions.assertEquals(type, field.getType().getName(),
                    "Type " + nom + " erroné : doit être " + type);
            Assertions.assertEquals(modifier, field.getModifiers(),
                    "Visibilité " + nom + " erronée : doit être " + modifier);

        }

        /**
         * Constructeurs
         */
        static Stream<Arguments> constructeursProvider() {
            return Stream.of(
                    arguments("public %s.Message(java.lang.String,java.lang.String)")
            );
        }

        @DisplayName("Déclaration des constructeurs (base)")
        @ParameterizedTest
        @MethodSource("constructeursProvider")
        void testConstructeurs(String signature) {
            Assertions.assertTrue(constructeursSignatures.contains(String.format(signature, module)),
                    String.format("Constructeur non déclaré : doit être %s\nalors que sont déclarés %s",
                            signature, constructeursSignatures));

        }

        /**
         * Méthodes
         */
        static Stream<Arguments> methodesProvider() {
            return Stream.of(
                    arguments("getTo", "public java.lang.String %s.Message.getTo()"),
                    arguments("getBody", "public java.lang.String %s.Message.getBody()"),
                    arguments("toString", "public java.lang.String %s.Message.toString()"),
                    arguments("toJsonObject", "public org.json.JSONObject %s.Message.toJsonObject()"),
                    arguments("toJSON", "public java.lang.String %s.Message.toJson()"),
                    arguments("fromJson", "public static %s.Message %s.Message.fromJson(org.json.JSONObject)")
            );
        }

        @DisplayName("Déclaration des méthodes (base)")
        @ParameterizedTest
        @MethodSource("methodesProvider")
        void testDeclarationMethodes(String nom, String signature) {
            Assertions.assertTrue(methodesSignatures.contains(String.format(signature, module, module)),
                    String.format("Méthode non déclarée : doit être %s\nalors que sont déclarés %s",
                            signature, methodesSignatures));
        }

    }

    @DisplayName("02-Instanciation et getters")
    @Test
    void testGetToBody() {
        String erreur = "Getter erroné";
        Message m = new Message("riri", "bonjour");
        assertEquals("riri", m.getTo(), erreur);
        assertEquals("bonjour", m.getBody(), erreur);
    }


    @Test
    @DisplayName("03-Représentation textuelle")
    void testToString() {
        Message m = new Message("riri", "bonjour");
        assertEquals("Message{to=riri, body=bonjour}", m.toString(), "Représentation textuelle erronée");
    }

    @Nested
    @DisplayName("04-Représentation JSON")
    class JSONTest {


        @Test
        @DisplayName("Objet JSON")
        void testClesToJSONObject() {
            Message m = new Message("riri", "bonjour");
            JSONObject obj = m.toJsonObject();
            Assertions.assertTrue(obj.has("to"), "Clé manquante");
            Assertions.assertTrue(obj.has("body"), "Clé manquante");

        }


        @Test
        @DisplayName("Sérialisation de la représentation JSON")
        void testToJson() {
            Message m = new Message("riri", "bonjour");
            String chaine = m.toJson();
            Assertions.assertTrue(chaine.contains("\"to\":\"riri\""), "to erroné dans " + m);
            Assertions.assertTrue(chaine.contains("\"body\":\"bonjour\""), "body erroné dans " + m);
        }

        @Test
        @DisplayName("Instanciation à partir d'un objet JSON")
        void testMessageFromJSON() {
            JSONObject json = new JSONObject("{\"to\":\"riri\",\"body\":\"bonjour\"}");
            Message m = Message.fromJson(json);
            Assertions.assertEquals("riri", m.getTo(), "Constructeur erroné");
            Assertions.assertEquals("bonjour", m.getBody(), "Constructeur erroné");
        }
    }
}