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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class PostTest {

    static Class<?> classe = Post.class;
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
        static Stream<Arguments> attributsHeritesProvider() {
            return Stream.of(
                    arguments("to", "java.lang.String", Modifier.PROTECTED),
                    arguments("body", "java.lang.String", Modifier.PROTECTED)
            );
        }
        @DisplayName("Attributs hérités : nom, type et visibilité")
        @ParameterizedTest
        @MethodSource("attributsHeritesProvider")
        void testDeclarationAttributsHerites(String nom, String type, int modifier) throws NoSuchFieldException {
            Field[] fields = Post.class.getDeclaredFields();
            List<String> noms = Arrays.stream(fields).map(Field::getName).toList();
            Assertions.assertFalse(noms.contains(nom), nom + " doit être hérité");
        }


        /** Attributs */
        static Stream<Arguments> attributsProvider() {
            return Stream.of(
                    arguments("id", "java.util.UUID", Modifier.PROTECTED),
                    arguments("timestamp", "long", Modifier.PROTECTED),
                    arguments("from", "java.lang.String", Modifier.PROTECTED)
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

        @DisplayName("Héritage")
        @Test
        void testHeritage() {
            Post p = new Post("riri", "fifi", "bonjour");
            Assertions.assertInstanceOf(Message.class, p, "Post doit hériter de Message");
        }

        /** Constructeurs */
        static Stream<Arguments> constructeursProvider() {
            return Stream.of(
                    arguments("public %s.Post(java.util.UUID,long,java.lang.String,java.lang.String,java.lang.String)"),
                    arguments("public %s.Post(java.lang.String,java.lang.String,java.lang.String)")
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


        /** Méthodes */
        static Stream<Arguments> methodesProvider1() {
            return Stream.of(
                    arguments("getId", "public java.util.UUID %s.Post.getId()"),
                    arguments("getTimestamp", "public long %s.Post.getTimestamp()"),
                    arguments("getFrom", "public java.lang.String %s.Post.getFrom()"),
                    arguments("toString", "public java.lang.String %s.Post.toString()"),
                    arguments("toJsonObject", "public org.json.JSONObject %s.Post.toJsonObject()"),
                    arguments("toJSON", "public java.lang.String %s.Post.toJson()"),
                    arguments("fromJson", "public static %s.Post %s.Post.fromJson(org.json.JSONObject)"),
                    arguments("equals", "public boolean %s.Post.equals(java.lang.Object)")
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

    @DisplayName("02-Instanciation et getter")
    @Nested
    class InstanciationPostTest {

        @DisplayName("Constructeur par défaut")
        @Test
        void testConstructeurDefaut() {
            UUID id = UUID.fromString("a821f534-b63a-4006-bbe1-eab7c4ff834e");
            Post p = new Post(id, 700000, "riri", "fifi", "bonjour");
            Assertions.assertEquals(id, p.getId(), "id erroné");
            Assertions.assertEquals(700000, p.getTimestamp(), "timestamp erroné");
            Assertions.assertEquals("riri", p.getFrom(), "from erroné");
            Assertions.assertEquals("fifi", p.getTo(), "to erroné");
            Assertions.assertEquals("bonjour", p.getBody(), "body erroné");
        }

        @DisplayName("Constructeur avec choix id, timestamp")
        @Test
        void testConstructeur() {
            Post p = new Post("fifi", "riri", "salut");
            Assertions.assertEquals("fifi", p.getFrom(), "from erroné");
            Assertions.assertEquals("riri", p.getTo(), "to erroné");
            Assertions.assertEquals("salut", p.getBody(), "body erroné");
        }

    }

    @Test
    @DisplayName("03-Représentation textuelle d'un post")
    void testToString() {
        UUID uuid = UUID.fromString("a821f534-b63a-4006-bbe1-eab7c4ff834e");
        Post p = new Post(uuid, 17297794, "riri", "fifi", "bonjour");
        String chaine = p.toString();
        Assertions.assertEquals("Post{id=a821f534-b63a-4006-bbe1-eab7c4ff834e, timestamp=17297794, " +
                "from='riri', to='fifi', body='bonjour'}", chaine, "Représentation textuelle erronée");
    }


    @DisplayName("04-Représentation JSON")
    @Nested
    class TestJSON {

        @Test
        @DisplayName("Objet JSON représentant un post")
        void toJsonObject () {
            UUID uuid = UUID.randomUUID();
            Post p = new Post(uuid, 800000, "riri", "fifi", "bonjour");
            JSONObject obj = p.toJsonObject();
            Assertions.assertTrue(obj.has("id"), "Clé manquante");
            Assertions.assertTrue(obj.has("timestamp"), "Clé manquante");
            Assertions.assertTrue(obj.has("from"), "Clé manquante");
            Assertions.assertTrue(obj.has("to"), "Clé manquante");
            Assertions.assertTrue(obj.has("body"), "Clé manquante");
            /*JSONAssert.assertEquals("{id:1}", obj, true);*/
        }

        @Test
        @DisplayName("Sérialisation de la représentation JSON")
        void testToJson () {
            UUID uuid = UUID.fromString("a821f534-b63a-4006-bbe1-eab7c4ff834e");
            Post p = new Post(uuid, 17297794, "bob", "alice", "bonjour");
            String chaine = p.toJson();
            Assertions.assertTrue(chaine.contains("\"from\":\"bob\""), "from erroné dans " + p);
            Assertions.assertTrue(chaine.contains("\"to\":\"alice\""), "to erroné dans " + p);
            Assertions.assertTrue(chaine.contains("\"body\":\"bonjour\""), "body erroné dans " + p);
            Assertions.assertTrue(chaine.contains("\"timestamp\":17297794"), "timestamp erroné dans " + p);
            Assertions.assertTrue(chaine.contains("\"id\":\"a821f534-b63a-4006-bbe1-eab7c4ff834e\""), "id erroné dans " + p.toJson());

        }

        @Test
        @DisplayName("Construction à partir d'un JSON")
        void testInstanciationJSON() {
            JSONObject json = new JSONObject("{\"id\": \"a821f534-b63a-4006-bbe1-eab7c4ff834e\",\"timestamp\":17297794,\"to\":\"riri\",\"from\":\"fifi\",\"body\":\"ouf\"}");
            Post p = Post.fromJson(json);
            UUID uuid = UUID.fromString("a821f534-b63a-4006-bbe1-eab7c4ff834e");
            Assertions.assertEquals(uuid, p.getId(), "id erroné");
            Assertions.assertEquals(17297794, p.getTimestamp(), "timestamp erroné");
            Assertions.assertEquals("riri", p.getTo(), "to erroné");
            Assertions.assertEquals("fifi", p.getFrom(), "from erroné");
            Assertions.assertEquals("ouf", p.getBody(), "body erroné");
        }

    }

    @DisplayName("05-Traitement des posts par le serveur")
    @Nested
    class TestTraitementPost {

        @Test
        @DisplayName("Instanciation à partir d'un message")
        void testInstanciationFromMessage() {
            Message m = new Message("bob", "hello");
            Post p = new Post("alice", m);
            Assertions.assertEquals("alice", p.getFrom(), "From erroné");
            Assertions.assertEquals("bob", p.getTo(), "To erroné");
            Assertions.assertEquals("hello", p.getBody(), "Body erroné");
        }


        @Test
        @DisplayName("Egalité de posts")
        void testEquals() {
            Post p1 = new Post("riri", "fifi", "salut");
            Post p2 = new Post(p1.id, 17297794, "riri", "fifi", "bonjour");
            Post p3 = new Post("riri", "fifi", "salut");
            Assertions.assertEquals(p1, p2, "Erreur de comparaison");
            Assertions.assertNotEquals(p1, p3, "Erreur de comparaison");
            Assertions.assertNotEquals(p2, p3, "Erreur de comparaison");

        }
    }
}