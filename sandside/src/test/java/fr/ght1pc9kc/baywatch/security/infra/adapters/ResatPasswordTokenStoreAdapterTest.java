
package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.KeyValueStore;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests pour ResatPasswordTokenStoreAdapter")
class ResatPasswordTokenStoreAdapterTest {

    private static final String TOKEN_KEY_PREFIX = "security:reset-password:";
    private static final String LUKE_TOKEN = "luke-skywalker-hash-token";
    private static final String LEIA_TOKEN = "princess-leia-hash-token";
    private static final String VADER_TOKEN = "darth-vader-hash-token";
    private static final String INVALID_TOKEN = "invalid-token-hash";

    @Mock
    private KeyValueStore kvStore;

    private ResatPasswordTokenStoreAdapter adapter;

    // Données de test avec le thème Star Wars
    private Entity<User> lukeEntity;
    private Entity<User> leiaEntity;
    private Entity<User> vaderEntity;

    @BeforeEach
    void setUp() {
        adapter = new ResatPasswordTokenStoreAdapter(kvStore);

        // Création des utilisateurs Star Wars
        User luke = User.builder()
                .login("luke.skywalker")
                .name("Luke Skywalker")
                .mail("luke@rebelalliance.galaxy")
                .build();

        User leia = User.builder()
                .login("princess.leia")
                .name("Princess Leia Organa")
                .mail("leia@rebelalliance.galaxy")
                .build();

        User vader = User.builder()
                .login("darth.vader")
                .name("Anakin Skywalker")
                .mail("vader@empire.galaxy")
                .build();

        lukeEntity = Entity.identify(luke).withId("luke-entity-001");
        leiaEntity = Entity.identify(leia).withId("leia-entity-002");
        vaderEntity = Entity.identify(vader).withId("vader-entity-003");
    }

    @Nested
    @DisplayName("Tests de la méthode get")
    class GetTests {

        @Test
        void should_return_user_when_token_exists_and_isvalid() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + LUKE_TOKEN)).thenReturn(Optional.of(lukeEntity));

            // When
            Optional<Entity<User>> result = adapter.get(LUKE_TOKEN);

            // Then
            assertThat(result)
                    .isPresent()
                    .hasValueSatisfying(entity -> {
                        assertThat(entity.id()).isEqualTo("luke-entity-001");
                        assertThat(entity.self().login()).isEqualTo("luke.skywalker");
                        assertThat(entity.self().name()).isEqualTo("Luke Skywalker");
                        assertThat(entity.self().mail()).isEqualTo("luke@rebelalliance.galaxy");
                    });

            verify(kvStore).get(TOKEN_KEY_PREFIX + LUKE_TOKEN);
            verify(kvStore, never()).remove(any());
        }

        @Test
        void should_return_empty_when_token_does_not_exist() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + INVALID_TOKEN)).thenReturn(Optional.empty());

            // When
            Optional<Entity<User>> result = adapter.get(INVALID_TOKEN);

            // Then
            assertThat(result).isEmpty();

            verify(kvStore).get(TOKEN_KEY_PREFIX + INVALID_TOKEN);
            verify(kvStore, never()).remove(any());
        }

        @Test
        void should_remove_and_return_empty_when_stored_object_is_not_user_entity() {
            // Given
            String invalidObject = "This is not an Entity<User>, this is the death star";
            when(kvStore.get(TOKEN_KEY_PREFIX + VADER_TOKEN)).thenReturn(Optional.of(invalidObject));

            // When
            Optional<Entity<User>> result = adapter.get(VADER_TOKEN);

            // Then
            assertThat(result).isEmpty();

            verify(kvStore).get(TOKEN_KEY_PREFIX + VADER_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + VADER_TOKEN);
        }

        @Test
        void should_remove_and_return_empty_when_entity_does_not_contain_user() {
            // Given
            Entity<String> nonUserEntity = Entity.identify("Je suis ton père").withId("vader-quote");
            when(kvStore.get(TOKEN_KEY_PREFIX + VADER_TOKEN)).thenReturn(Optional.of(nonUserEntity));

            // When
            Optional<Entity<User>> result = adapter.get(VADER_TOKEN);

            // Then
            assertThat(result).isEmpty();

            verify(kvStore).get(TOKEN_KEY_PREFIX + VADER_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + VADER_TOKEN);
        }

        @Test
        void should_handle_different_users_correctly() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + LEIA_TOKEN)).thenReturn(Optional.of(leiaEntity));

            // When
            Optional<Entity<User>> result = adapter.get(LEIA_TOKEN);

            // Then
            assertThat(result)
                    .isPresent()
                    .hasValueSatisfying(entity -> {
                        assertThat(entity.id()).isEqualTo("leia-entity-002");
                        assertThat(entity.self().login()).isEqualTo("princess.leia");
                        assertThat(entity.self().name()).isEqualTo("Princess Leia Organa");
                        assertThat(entity.self().mail()).isEqualTo("leia@rebelalliance.galaxy");
                    });
        }
    }

    @Nested
    class StoreTests {

        @Test
        void should_store_user_with_token_and_ttl() {
            // Given
            Duration ttl = Duration.ofMinutes(30);

            // When
            adapter.store(LUKE_TOKEN, lukeEntity, ttl);

            // Then
            verify(kvStore).put(
                    assertArg(key -> assertThat(key).isEqualTo(TOKEN_KEY_PREFIX + LUKE_TOKEN)),
                    assertArg(user -> assertThat(user).isEqualTo(lukeEntity)),
                    assertArg(ttlCaptor -> assertThat(ttlCaptor).isEqualTo(ttl))
            );
        }

        @Test
        void should_store_different_users_with_different_tokens() {
            // Given
            Duration shortTtl = Duration.ofMinutes(15);
            Duration longTtl = Duration.ofHours(2);

            // When
            adapter.store(LEIA_TOKEN, leiaEntity, shortTtl);
            adapter.store(VADER_TOKEN, vaderEntity, longTtl);

            // Then
            verify(kvStore).put(TOKEN_KEY_PREFIX + LEIA_TOKEN, leiaEntity, shortTtl);
            verify(kvStore).put(TOKEN_KEY_PREFIX + VADER_TOKEN, vaderEntity, longTtl);
            verify(kvStore, times(2)).put(any(String.class), any(Entity.class), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_hashed_token_isnull() {
            // When/Then
            Duration ttl = Duration.ofMinutes(30);
            assertThatThrownBy(() -> adapter.store(null, lukeEntity, ttl))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_user_isnull() {
            // When/Then
            Duration ttl = Duration.ofMinutes(30);
            assertThatThrownBy(() -> adapter.store(LUKE_TOKEN, null, ttl))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_ttl_isnull() {
            // When/Then
            assertThatThrownBy(() -> adapter.store(LUKE_TOKEN, lukeEntity, null))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }
    }

    @Nested
    class RemoveTests {

        @Test
        void should_remove_token_from_store() {
            // When
            adapter.remove(LUKE_TOKEN);

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);
        }

        @Test
        void should_remove_different_tokens() {
            // When
            adapter.remove(LEIA_TOKEN);
            adapter.remove(VADER_TOKEN);

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX + LEIA_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + VADER_TOKEN);
            verify(kvStore, times(2)).remove(any(String.class));
        }

        @Test
        void should_call_remove_even_with_non_existent_token() {
            // When
            adapter.remove("death-star-plans-token");

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX + "death-star-plans-token");
        }

        @Test
        void should_handle_empty_tokens() {
            // When
            adapter.remove("");

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX);
        }
    }

    @Nested
    class IntegrationTests {

        @Test
        void should_handle_complete_lifecycle() {
            // Given
            Duration ttl = Duration.ofHours(1);
            when(kvStore.get(TOKEN_KEY_PREFIX + LUKE_TOKEN)).thenReturn(Optional.of(lukeEntity));

            // When - Store
            adapter.store(LUKE_TOKEN, lukeEntity, ttl);

            // When - Get
            Optional<Entity<User>> retrieved = adapter.get(LUKE_TOKEN);

            // When - Remove
            adapter.remove(LUKE_TOKEN);

            // Then
            assertThat(retrieved).isPresent().contains(lukeEntity);

            verify(kvStore).put(TOKEN_KEY_PREFIX + LUKE_TOKEN, lukeEntity, ttl);
            verify(kvStore).get(TOKEN_KEY_PREFIX + LUKE_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);
        }

        @Test
        void should_prefix_all_keys_correctly() {
            // Given
            String[] tokens = {"jedi-council", "sith-lord", "rebel-scum"};
            Duration ttl = Duration.ofMinutes(45);

            // When
            for (String token : tokens) {
                adapter.store(token, lukeEntity, ttl);
                adapter.get(token);
                adapter.remove(token);
            }

            // Then
            for (String token : tokens) {
                String expectedKey = TOKEN_KEY_PREFIX + token;
                verify(kvStore).put(expectedKey, lukeEntity, ttl);
                verify(kvStore).get(expectedKey);
                verify(kvStore).remove(expectedKey);
            }
        }
    }
}