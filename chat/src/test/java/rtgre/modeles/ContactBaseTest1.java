package rtgre.modeles;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/** Tests unitaires du modèle de base de Contact (étape 1) */

class ContactBaseTest1 {

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

        /**
         * Attributs
         */
        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("login", "java.lang.String", Modifier.PROTECTED),
                    arguments("avatar", "java.awt.Image", Modifier.PROTECTED),
                    arguments("connected", "boolean", Modifier.PROTECTED),
                    arguments("currentRoom", "java.lang.String", Modifier.PROTECTED)
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
                    arguments("public %s.Contact(java.lang.String,java.awt.Image)")
            );
        }

        @DisplayName("Déclaration des constructeurs (base)")
        @ParameterizedTest
        @MethodSource("constructeursProvider")
        void testConstructeurs1(String signature) {
            Assertions.assertTrue(constructeursSignatures.contains(String.format(signature, module)),
                    String.format("Constructeur non déclaré : doit être %s\nalors que sont déclarés %s",
                            signature, constructeursSignatures));

        }


        /**
         * Méthodes
         */
        static Stream<Arguments> methodesProvider1() {
            return Stream.of(
                    arguments("getLogin", "public java.lang.String %s.Contact.getLogin()"),
                    arguments("getAvatar", "public java.awt.Image %s.Contact.getAvatar()"),
                    arguments("isConnected", "public boolean %s.Contact.isConnected()"),
                    arguments("toString", "public boolean %s.Contact.isConnected()"),
                    arguments("setConnected", "public java.lang.String %s.Contact.toString()"),
                    arguments("equals", "public boolean %s.Contact.equals(java.lang.Object)")
            );
        }

        @DisplayName("Déclaration des méthodes (base)")
        @ParameterizedTest
        @MethodSource("methodesProvider1")
        void testDeclarationMethodes1(String nom, String signature) {
            Assertions.assertTrue(methodesSignatures.contains(String.format(signature, module, module)),
                    String.format("Méthode non déclarée : doit être %s\nalors que sont déclarés %s",
                            signature, methodesSignatures));
        }


        @DisplayName("02-Instanciation et getters")
        @Nested
        class InstanciationContactTest {

            @Test
            @DisplayName("sans avatar : getters de login, connected")
            void TestConstructeurParDefautSansAvatar() {
                Contact riri = new Contact("riri", null);
                Assertions.assertEquals("riri", riri.getLogin(), "Login erroné");
                Assertions.assertFalse(riri.isConnected(), "Etat par défaut erroné");
            }

        }

        @DisplayName("03-Modification")
        @Nested
        class ModificationContactTest {

            @Test
            @DisplayName("Setter de connexion")
            void TestEtatConnexion() {
                Contact riri = new Contact("riri", null);
                riri.setConnected(true);
                Assertions.assertTrue(riri.isConnected(), "Changement d'état erroné");
                riri.setConnected(false);
                Assertions.assertFalse(riri.isConnected(), "Changement d'état erroné");
            }

        }

        @Test
        @DisplayName("04-Représentation textuelle")
        void TestToString() {
            Contact riri = new Contact("riri", null);
            Assertions.assertEquals("@riri", riri.toString(),
                    "Représentation textuelle erronée");
        }

        @Test
        @DisplayName("05-Egalité")
        void TestEquals() {
            Contact riri = new Contact("riri", null);
            Contact riri2 = new Contact("riri", null);
            Contact fifi = new Contact("fifi", null);
            Assertions.assertEquals(riri, riri2, "Comparaison erronée");
            Assertions.assertNotEquals(riri, fifi, "Comparaison erronée");
        }

    }
}