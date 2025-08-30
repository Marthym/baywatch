
package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class KeyValueStoreAdapterTest {

    private KeyValueStore tested;

    @BeforeEach
    void setUp() {
        tested = new KeyValueStoreAdapter();
    }

    @Nested
    class GetOperationsTest {

        @Test
        void should_return_empty_when_key_not_found() {
            // Given
            String jediKey = "luke.skywalker";

            // When
            Optional<Object> result = tested.get(jediKey);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void should_return_empty_when_key_not_found_with_type() {
            // Given
            String sithKey = "darth.vader";

            // When
            Optional<String> result = tested.get(sithKey, String.class);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void should_get_stored_value_after_put() {
            // Given
            String jediKey = "obi.wan.kenobi";
            String jediValue = "Hello there! General Grievous.";
            tested.put(jediKey, jediValue);

            // When
            Optional<Object> result = tested.get(jediKey);

            // Then
            assertThat(result).isPresent().contains(jediValue);
        }

        @Test
        void should_get_stored_value_with_correct_type() {
            // Given
            String forceKey = "force.power.level";
            Integer forcePower = 9001;
            tested.put(forceKey, forcePower);

            // When
            Optional<Integer> result = tested.get(forceKey, Integer.class);

            // Then
            assertThat(result).isPresent().contains(forcePower);
        }

        @Test
        void should_return_empty_when_type_does_not_match() {
            // Given
            String planetKey = "tatooine.suns";
            Integer numberOfSuns = 2;
            tested.put(planetKey, numberOfSuns);

            // When
            Optional<String> result = tested.get(planetKey, String.class);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class PutOperationsTest {

        @Test
        void should_store_single_value() {
            // Given
            String rebelKey = "princess.leia";
            String rebelValue = "Help me, Obi-Wan Kenobi. You're my only hope.";

            // When
            tested.put(rebelKey, rebelValue);

            // Then
            Optional<Object> result = tested.get(rebelKey);
            assertThat(result).isPresent().contains(rebelValue);
        }

        @Test
        void should_store_single_value_with_ttl() {
            // Given
            String droidsKey = "c3po.protocol";
            String droidsValue = "I am C-3PO, human-cyborg relations";
            Duration shortTtl = Duration.ofMillis(100);

            // When
            tested.put(droidsKey, droidsValue, shortTtl);

            // Then
            Optional<Object> result = tested.get(droidsKey);
            assertThat(result).isPresent().contains(droidsValue);

            // Wait for expiration
            await().atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> assertThat(tested.get(droidsKey)).isEmpty());
        }

        @Test
        void should_store_multiple_values() {
            // Given
            Map<String, Object> starWarsCharacters = Map.of(
                    "han.solo", "I've got a bad feeling about this",
                    "chewbacca", "Rrwwwgg",
                    "yoda", "Do or do not, there is no try",
                    "vader.breathing", "Khhhh-purrrr"
            );

            // When
            tested.putAll(starWarsCharacters);

            // Then
            starWarsCharacters.forEach((key, value) -> {
                Optional<Object> result = tested.get(key);
                assertThat(result).isPresent().contains(value);
            });
        }

        @Test
        void should_store_multiple_values_with_ttl() {
            // Given
            Map<String, Object> imperialData = Map.of(
                    "death.star.plans", "Weakness in thermal exhaust port",
                    "storm.trooper.accuracy", 0.01,
                    "emperor.location", "Death Star II"
            );
            Duration imperialTtl = Duration.ofMillis(150);

            // When
            tested.putAll(imperialData, imperialTtl);

            // Then - Vérifier que les valeurs sont stockées
            imperialData.forEach((key, value) -> {
                Optional<Object> result = tested.get(key);
                assertThat(result).isPresent().contains(value);
            });

            // Wait for expiration
            await().atMost(Duration.ofSeconds(1))
                    .untilAsserted(() -> {
                        imperialData.keySet().forEach(key ->
                                assertThat(tested.get(key)).isEmpty());
                    });
        }

        @Test
        void should_overwrite_existing_value() {
            // Given
            String jediKey = "anakin.skywalker";
            String youngAnakin = "I'll try spinning, that's a good trick!";
            String darthVader = "I find your lack of faith disturbing";

            // When
            tested.put(jediKey, youngAnakin);
            tested.put(jediKey, darthVader);

            // Then
            Optional<Object> result = tested.get(jediKey);
            assertThat(result).isPresent().contains(darthVader);
        }
    }

    @Nested
    class RemoveOperationsTest {

        @Test
        void should_return_empty_when_removing_non_existent_key() {
            // Given
            String unknownKey = "jar.jar.binks";

            // When
            Optional<Object> result = tested.remove(unknownKey);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void should_return_empty_when_removing_non_existent_key_with_type() {
            // Given
            String unknownKey = "midi.chlorians";

            // When
            Optional<String> result = tested.remove(unknownKey, String.class);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        void should_remove_and_return_existing_value() {
            // Given
            String rebelKey = "x.wing.fighter";
            String rebelValue = "The Force is strong with this one";
            tested.put(rebelKey, rebelValue);

            // When
            Optional<Object> result = tested.remove(rebelKey);

            // Then
            assertThat(result).isPresent().contains(rebelValue);
            assertThat(tested.get(rebelKey)).isEmpty();
        }

        @Test
        void should_remove_and_return_existing_value_with_correct_type() {
            // Given
            String shipKey = "millennium.falcon.speed";
            Double parcsecSpeed = 0.5; // It made the Kessel Run in less than twelve parsecs
            tested.put(shipKey, parcsecSpeed);

            // When
            Optional<Double> result = tested.remove(shipKey, Double.class);

            // Then
            assertThat(result).isPresent().contains(parcsecSpeed);
            assertThat(tested.get(shipKey)).isEmpty();
        }

        @Test
        void should_return_empty_when_removing_with_wrong_type() {
            // Given
            String planetKey = "alderaan.population";
            Long population = 2_000_000_000L;
            tested.put(planetKey, population);

            // When
            Optional<String> result = tested.remove(planetKey, String.class);

            // Then
            assertThat(result).isEmpty();
            // La valeur devrait toujours être là car le type ne correspond pas
            assertThat(tested.get(planetKey)).isPresent().contains(population);
        }
    }

    @Nested
    class ComplexScenariosTest {

        @Test
        void should_handle_different_value_types() {
            // Given
            Map<String, Object> galaxyData = Map.of(
                    "jedi.count", 10_000,
                    "is.empire.evil", true,
                    "lightsaber.colors", new String[]{"blue", "green", "red", "purple"},
                    "death.star.diameter", 120.0,
                    "padawan.name", "Ahsoka Tano"
            );

            // When
            tested.putAll(galaxyData);

            // Then
            assertThat(tested.get("jedi.count", Integer.class)).isPresent().contains(10_000);
            assertThat(tested.get("is.empire.evil", Boolean.class)).isPresent().contains(true);
            assertThat(tested.get("lightsaber.colors", String[].class)).isPresent()
                    .hasValueSatisfying(colors -> assertThat(colors).contains("blue", "green", "red", "purple"));
            assertThat(tested.get("death.star.diameter", Double.class)).isPresent().contains(120.0);
            assertThat(tested.get("padawan.name", String.class)).isPresent().contains("Ahsoka Tano");
        }

        @Test
        void should_handle_mixed_operations() {
            // Given
            String masterKey = "yoda.wisdom";
            String masterWisdom = "Fear leads to anger, anger leads to hate, hate leads to suffering";

            // When & Then - Store and retrieve
            tested.put(masterKey, masterWisdom);
            assertThat(tested.get(masterKey)).isPresent().contains(masterWisdom);

            // Update the value
            String newWisdom = "Size matters not. Look at me. Judge me by my size, do you?";
            tested.put(masterKey, newWisdom);
            assertThat(tested.get(masterKey)).isPresent().contains(newWisdom);

            // Remove and check
            Optional<String> removed = tested.remove(masterKey, String.class);
            assertThat(removed).isPresent().contains(newWisdom);
            assertThat(tested.get(masterKey)).isEmpty();
        }

        @Test
        void should_handle_ttl_expiration_scenarios() {
            // Given
            String quickKey = "r2d2.beep";
            String quickValue = "Beep boop beep!";
            Duration quickTtl = Duration.ofMillis(50);

            String slowKey = "c3po.translation";
            String slowValue = "The odds of successfully navigating an asteroid field are 3,720 to 1";
            Duration slowTtl = Duration.ofSeconds(5);

            // When
            tested.put(quickKey, quickValue, quickTtl);
            tested.put(slowKey, slowValue, slowTtl);

            // Then - Quick value should expire first
            await().atMost(Duration.ofMillis(200))
                    .untilAsserted(() -> assertThat(tested.get(quickKey)).isEmpty());

            // Slow value should still be there
            assertThat(tested.get(slowKey)).isPresent().contains(slowValue);
        }

        @Test
        void should_maintain_data_integrity_across_multiple_operations() {
            // Given
            Map<String, Object> rebelBase = Map.of(
                    "location", "Yavin 4",
                    "leader", "Mon Mothma",
                    "ships.count", 30,
                    "operational", true
            );

            // When - Batch store
            tested.putAll(rebelBase);

            // Add individual items
            tested.put("security.level", "Maximum");
            tested.put("last.attack", "Battle of Endor");

            // Remove one item
            Optional<Object> removedLeader = tested.remove("leader");

            // Update one item
            tested.put("ships.count", 25);

            // Then
            assertThat(tested.get("location")).isPresent().contains("Yavin 4");
            assertThat(tested.get("leader")).isEmpty();
            assertThat(removedLeader).isPresent().contains("Mon Mothma");
            assertThat(tested.get("ships.count", Integer.class)).isPresent().contains(25);
            assertThat(tested.get("operational", Boolean.class)).isPresent().contains(true);
            assertThat(tested.get("security.level")).isPresent().contains("Maximum");
            assertThat(tested.get("last.attack")).isPresent().contains("Battle of Endor");
        }
    }
}