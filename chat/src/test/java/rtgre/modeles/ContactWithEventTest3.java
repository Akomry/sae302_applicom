package rtgre.modeles;

import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/** Tests unitaires du modèle de base de Contact (étape 1) */

class ContactWithEventTest3 {

    static Class<?> classe = Contact.class;
    static String module = "rtgre.modeles";

    @DisplayName("01-Structure de la classe Contact")
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

        static Stream<Arguments> methodesProvider3() {
            return Stream.of(
                    arguments("toJsonObject", "public org.json.JSONObject %s.Contact.toJsonObject()"),
                    arguments("toJson", "public java.lang.String %s.Contact.toJson()"),
                    arguments("fromJSON", "public static %s.Contact %s.Contact.fromJSON(org.json.JSONObject,java.io.File)")
            );
        }
        // @Disabled("Jusqu'à ce que soit codé les events")
        @DisplayName("Déclaration des méthodes (avec event et JSON)")
        @ParameterizedTest
        @MethodSource("methodesProvider3")
        void testDeclarationMethodes3(String nom, String signature) {
            Assertions.assertTrue(methodesSignatures.contains(String.format(signature, module, module)),
                    String.format("Méthode non déclarée : doit être %s\nalors que sont déclarés %s",
                            signature, methodesSignatures));
        }



    }


    @DisplayName("07-Représentation JSON")
    @Nested
    class JSONTest {

        @Test
        @DisplayName("JSONObject")
        void TestToJSONObject() {
            String erreur = "Représentation JSON erronée";
            Contact fifi = new Contact("fifi", true, (Image) null);
            JSONObject json = fifi.toJsonObject();
            Assertions.assertTrue(json.has("login"), erreur);
            Assertions.assertEquals("fifi", json.get("login"), erreur);
            Assertions.assertTrue(json.has("connected"), erreur);
            Assertions.assertEquals(true, json.get("connected"), erreur);
        }

        @Test
        @DisplayName("Sérialisation JSON")
        void TestToJSON() {
            String erreur = "Sérialisation de la représentation JSON erronée";
            Contact riri = new Contact("riri", true, (Image) null);
            String json = riri.toJson();
            Assertions.assertTrue(json.contains("\"login\":\"riri\""), erreur);
            Assertions.assertTrue(json.contains("\"connected\":true"), erreur);
        }

        @Test
        @DisplayName("Contact à partir d'un JSON")
        void TestConstructeur() throws IOException {
            String work_dir = System.getProperty("user.dir");
            Assertions.assertTrue(work_dir.endsWith("chat"),
                    "Le working dir doit être <projet>/chat/ et non : " + work_dir);
            File f = new File("src/main/resources/rtgre/chat/banque_avatars.png");
            String json = "{\"login\":\"riri\",\"connected\":true}";
            Contact toto = Contact.fromJSON(new JSONObject(json), f);
            Assertions.assertEquals("riri", toto.getLogin(), "Login erroné");
            Assertions.assertEquals(true, toto.isConnected(), "Connected erroné");
            Assertions.assertNotNull(toto.getAvatar(), "Avatar non chargé");
        }

    }

}