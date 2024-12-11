package rtgre.modeles;

import javafx.geometry.Pos;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PostVectorTest {

    static Class<?> classe = PostVector.class;
    static String module = "rtgre.modeles";

    @DisplayName("01-Structure")
    @Nested
    class Structure {

        static List<String> constructeursSignatures;
        static List<String> methodesSignatures;

        @BeforeAll
        static void init() {
            Constructor<?>[] constructeurs = classe.getConstructors();
            constructeursSignatures = Arrays.stream(constructeurs).map(Constructor::toString).collect(Collectors.toList());
            Method[] methodes = classe.getDeclaredMethods();
            methodesSignatures = Arrays.stream(methodes).map(Method::toString).collect(Collectors.toList());
        }

        @Test
        @DisplayName("Heritage")
        void testHeritage() {
            PostVector pv = new PostVector();
            assertInstanceOf(Vector.class, pv, "Doit hériter de Vector");
        }

        @Test
        @DisplayName("Nbre attributs")
        void testDeclarationNbreAttributs() {
            Field[] fields = classe.getDeclaredFields();
            assertEquals(0, fields.length, "Ne doit pas posséder d'attributs");
        }

        /** Méthodes */
        static Stream<Arguments> methodesProvider1() {
            return Stream.of(
                    arguments("getPostById", "public %s.Post rtgre.modeles.PostVector.getPostById(java.util.UUID)"),
                    arguments("getPostsSince",  "public java.util.Vector %s.PostVector.getPostsSince(long)")
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


    }

    @DisplayName("02-Méthodes")
    @Nested
    class MethodesPostVectorTest {
        @Test
        void getPostById() {
            PostVector pv = new PostVector();
            UUID uuid1 = UUID.fromString("a821f534-b63a-4006-bbe1-eab7c4ff834e");
            UUID uuid2 = UUID.fromString("b932f534-b63a-4006-bbe1-eab7c4ff834e");
            Post p1 = new Post(uuid1, 70000, "fifi", "riri", "message");
            Post p2 = new Post(uuid2, 70000, "donald", "mickey", "message");

            pv.add(new Post("fifi", "riri", "message"));
            pv.add(new Post("donald", "mickey", "message"));
            pv.add(p1);
            pv.add(new Post("fifi", "riri", "message"));
            pv.add(p2);

            Assertions.assertEquals(p1, pv.getPostById(uuid1), "Récupération post erronée");
            Assertions.assertEquals(p2, pv.getPostById(uuid2), "Récupération post erronée");
        }

        @Test
        void getPostsSince() {
            PostVector pv = new PostVector();
            pv.add(new Post(UUID.randomUUID(), 70000, "fifi", "riri", "message"));
            pv.add(new Post(UUID.randomUUID(), 70010, "donald", "mickey", "message"));
            pv.add(new Post(UUID.randomUUID(), 70020, "fifi", "riri", "message"));
            pv.add(new Post(UUID.randomUUID(), 80000, "fifi", "riri", "message"));
            pv.add(new Post(UUID.randomUUID(), 80020, "fifi", "riri", "message"));

            Vector<Post> expected = new Vector<>();
            expected.add(pv.get(2)); expected.add(pv.get(3)); expected.add(pv.get(4));

            Vector<Post> res = pv.getPostsSince(70015);
            assertEquals(expected, res, "Extraction erronée");
        }
    }
}