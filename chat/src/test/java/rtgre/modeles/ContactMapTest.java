package rtgre.modeles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

class ContactMapTest {

    static Class<?> classe = ContactMap.class;

    @DisplayName("01-Structure")
    @Nested
    class StructureContactMapTest {

        @Test
        @DisplayName("Heritage")
        void testHeritage() {
            ContactMap contactMap = new ContactMap();
            Assertions.assertInstanceOf(TreeMap.class, contactMap, "doit hériter de TreeMap");
        }


    }

    @DisplayName("02-Ajout")
    @Nested
    class AddTest {

        @Test
        @DisplayName("Ajout d'un contact")
        void TestAdd() {
            Contact riri = new Contact("riri", null);
            ContactMap contactMap = new ContactMap();
            contactMap.add(riri);

            Assertions.assertTrue(contactMap.containsKey("riri"),
                    "Les clés sont les logins");
            Assertions.assertTrue(contactMap.containsValue(riri),
                    "Les valeurs sont les contacts");
        }

    }

}