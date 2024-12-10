package rtgre.modeles;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/** Tests unitaires du modèle de base de Contact (étape 1) */

class ContactWithAvatarTest2 {

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



        static Stream<Arguments> constructeursProvider2() {
            return Stream.of(
                    arguments("public %s.Contact(java.lang.String,boolean,java.io.File)")
            );
        }
        // @Disabled("Jusqu'à ce que soit codé les avatars à partir d'un fichier")
        @DisplayName("Déclaration des constructeurs (avec avatars à partir d'un fichier)")
        @ParameterizedTest
        @MethodSource("constructeursProvider2")
        void testConstructeurs2(String signature) {
            Assertions.assertTrue(constructeursSignatures.contains(String.format(signature, module)),
                    String.format("Constructeur non déclaré : doit être %s\nalors que sont déclarés %s",
                            signature, constructeursSignatures));

        }


        static Stream<Arguments> methodesProvider2() {
            return Stream.of(
                    arguments("avatarFromLogin", "public static java.awt.image.BufferedImage %s.Contact.avatarFromLogin(java.io.File,java.lang.String) throws java.io.IOException"),
                    arguments("setAvatarFromFile", "public void %s.Contact.setAvatarFromFile(java.io.File)")
            );
        }
        // @Disabled("Jusqu'à ce que soit codé les avatars à partir d'un fichier")
        @DisplayName("Déclaration des méthodes (avec avatars à partir d'un fichier)")
        @ParameterizedTest
        @MethodSource("methodesProvider2")
        void testDeclarationMethodes2(String nom, String signature) {
            Assertions.assertTrue(methodesSignatures.contains(String.format(signature, module, module)),
                    String.format("Méthode non déclarée : doit être %s\nalors que sont déclarés %s",
                            signature, methodesSignatures));
        }

    }

    @DisplayName("02-Instanciation et getters")
    @Nested
    class InstanciationContactTest {

        // @Disabled("Jusqu'à ce que soit codé les avatars à partir d'un fichier")
        @Test
        @DisplayName("avec avatar : getters de avatar")
        void TestConstructeurParDefautAvecAvatar() throws IOException {
            String work_dir = System.getProperty("user.dir");
            Assertions.assertTrue(work_dir.endsWith("chat"),
                    "Le working dir doit être <projet>/chat/ et non : " + work_dir);
            File f = new File("src/main/resources/rtgre/chat/anonymous.png");
            Assertions.assertTrue(f.canRead(), "Fichier manquant " + f.getAbsolutePath());
            Image avatar = ImageIO.read(f);
            Contact fifi = new Contact("fifi", avatar);
            Assertions.assertEquals(avatar, fifi.getAvatar(), "Avatar erroné");
        }

    }


    // @Disabled("Jusqu'à ce que soit codé les avatars à partir d'un fichier")
    @Nested
    @DisplayName("06-Avatar à partir d'un fichier")
    class AvatarFromFilesTest {

        @Test
        @DisplayName("A partir d'un fichier")
        void TestSetFromFile() throws IOException {
            String work_dir = System.getProperty("user.dir");
            Assertions.assertTrue(work_dir.endsWith("chat"),
                    "Le working dir doit être <projet>/chat/ et non : " + work_dir);
            File f = new File("src/main/resources/rtgre/chat/avatar1.png");
            Assertions.assertTrue(f.canRead(), "Fichier manquant " + f.getAbsolutePath());
            Contact fifi = new Contact("fifi",  null);
            fifi.setAvatarFromFile(f);
            Assertions.assertNotNull(fifi.getAvatar(), "Avatar non chargé");
        }
    }

}