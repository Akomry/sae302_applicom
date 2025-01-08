package rtgre.modeles;

import org.json.JSONException;
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

class EventTest {

    static Class<?> classe = Event.class;
    static String module = "rtgre.modeles";

    // @Tag("01-mise_en_place_contact")
    @DisplayName("01-Structure de Event")
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

        static Stream<Arguments> constantesProvider() {
            return Stream.of(
                    arguments("AUTH", "AUTH"),
                    arguments("QUIT", "QUIT"),
                    arguments("MESG", "MESG"),
                    arguments("JOIN", "JOIN"),
                    arguments("POST", "POST"),
                    arguments("CONT", "CONT"),
                    arguments("LIST_POSTS", "LSTP"),
                    arguments("LIST_CONTACTS", "LSTC"),
                    arguments("SYSTEM", "SYST")
            );
        }

        @DisplayName("Déclaration des constantes")
        @ParameterizedTest
        @MethodSource("constantesProvider")
        void testDeclarationConstantes(String constante, String value) throws NoSuchFieldException, IllegalAccessException {
            Field field = classe.getField(constante);
            Assertions.assertEquals(Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC, field.getModifiers(),
                    "Visibilité " + constante + " erronée");
            Assertions.assertEquals("java.lang.String", field.getType().getName(),
                    "Type " + constante + " erroné");
            Assertions.assertEquals(value, field.get(null), "Valeur " + constante + " erronée");
        }


        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("type", "java.lang.String", Modifier.PRIVATE | Modifier.FINAL),
                    arguments("content", "org.json.JSONObject", Modifier.PRIVATE | Modifier.FINAL)
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
                    arguments("public %s.Event(java.lang.String,org.json.JSONObject)")
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
                    arguments("getType", "public java.lang.String %s.Event.getType()"),
                    arguments("getContent", "public org.json.JSONObject %s.Event.getContent()"),
                    arguments("toString", "public java.lang.String %s.Event.toString()"),
                    arguments("toJsonObject", "public org.json.JSONObject %s.Event.toJsonObject()"),
                    arguments("toJson", "public java.lang.String %s.Event.toJson()"),
                    arguments("fromJson", "public static %s.Event %s.Event.fromJson(java.lang.String) throws org.json.JSONException")
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

    @DisplayName("02-Instanciation d'un évènement")
    @Nested
    class InstanciationTest {

        @Test
        @DisplayName("Constructeur par défaut")
        void TestContructeurParDefaut() {
            JSONObject json = new JSONObject("{\"login\":\"riri\"}");
            Event event = new Event("AUTH", json);
            Assertions.assertEquals("AUTH", event.getType(), "Type erroné");
            Assertions.assertEquals(json, event.getContent(), "Content erroné");
        }



    }

    @DisplayName("03-Instanciation fromJSON")
    @Nested
    class fromJSONTest {

        static Stream<Arguments> contentProvides() {
            return Stream.of(
                    arguments("AUTH", "{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}"),
                    arguments("QUIT", "{\"type\": \"QUIT\",\"content\": {}}"),
                    arguments("MESG", "{\"type\": \"MESG\",\"content\": {\"to\":\"DEST\",\"body\":\"BODY\"}}")
            );
        }
        @DisplayName("Récupération du type")
        @ParameterizedTest
        @MethodSource("contentProvides")
        void TestContructeurType(String type, String json) {
            Event event = Event.fromJson(json);
            Assertions.assertEquals(type, event.getType(), "Type erroné");
        }

        static Stream<Arguments> keyProvides() {
            return Stream.of(
                    arguments("login", "riri", "{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}"),
                    arguments("to", "DEST", "{\"type\": \"MESG\",\"content\": {\"to\":\"DEST\",\"body\":\"BODY\"}}"),
                    arguments("body", "BODY", "{\"type\": \"MESG\",\"content\": {\"to\":\"DEST\",\"body\":\"BODY\"}}")
            );
        }

        @DisplayName("Récupération du contenu")
        @ParameterizedTest
        @MethodSource("keyProvides")
        void TestContructeurContenu(String key, String value, String json) {
            Event event = Event.fromJson(json);
            Assertions.assertTrue(event.getContent().has(key), "Contenu erroné");
            Assertions.assertEquals(value, event.getContent().get(key), "Contenu erroné");
        }

        @DisplayName("Levée d'exception")
        @Test
        void TestFromJsonException() {
            assertThrows(JSONException.class, () -> Event.fromJson("bonjour"));
        }
    }

    @Nested
    @DisplayName("04-Méthodes")

    class TestMethodes {
        @Test
        @DisplayName("Méthode toString")
        void TestToString() {
            Event event = Event.fromJson("{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}");
            String repr = event.toString();
            Assertions.assertEquals("Event{type=AUTH, content={\"login\":\"riri\"}}", repr, "Représentation textuelle erronée");
        }

        @Test
        @DisplayName("Méthode toJsonObject")
        void TestToJsonObject() {
            Event event = Event.fromJson("{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}");
            JSONObject json = event.toJsonObject();
            Assertions.assertTrue(json.has("type"), "Représentation JSON erronée");
            Assertions.assertEquals("AUTH", json.get("type"), "Représentation JSON erronée");
            Assertions.assertTrue(json.has("content"), "Représentation JSON erronée");
            Assertions.assertTrue(json.getJSONObject("content").has("login"), "Représentation JSON erronée");
            Assertions.assertEquals("riri", json.getJSONObject("content").get("login"), "Représentation JSON erronée");
        }

        @Test
        @DisplayName("Méthode toJson")
        void TestToJson() {
            Event event = Event.fromJson("{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}");
            String json = event.toJson();
            Assertions.assertEquals("{\"type\":\"AUTH\",\"content\":{\"login\":\"riri\"}}",
                    json, "Sérialisation JSON erronée");
        }


    }


}