
package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.KeyValueStore;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResetPasswordTokenStoreAdapterTest {

    private static final String TOKEN_KEY_PREFIX = "security:reset-password:";
    private static final String LUKE_TOKEN = "luke-skywalker-hash-token";
    private static final String LEIA_TOKEN = "princess-leia-hash-token";
    private static final String VADER_TOKEN = "darth-vader-hash-token";
    private static final String INVALID_TOKEN = "invalid-token-hash";

    private KeyValueStore kvStore;

    private KeyValuePersistenceStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        kvStore = mock(KeyValueStore.class);
        adapter = new KeyValuePersistenceStoreAdapter(kvStore);
    }

    @Nested
    class GetTests {

        @Test
        void should_return_user_when_token_exists_and_isvalid() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + LUKE_TOKEN)).thenReturn(Optional.of(UserSamples.LUKE));

            // When
            Optional<Entity<User>> result = adapter.get(TOKEN_KEY_PREFIX + LUKE_TOKEN);

            // Then
            assertThat(result).isPresent()
                    .hasValueSatisfying(entity -> SoftAssertions.assertSoftly(softly -> {
                        softly.assertThat(entity.id()).isEqualTo(UserSamples.LUKE.id());
                        softly.assertThat(entity.self().login()).isEqualTo(UserSamples.LUKE.self().login());
                        softly.assertThat(entity.self().name()).isEqualTo(UserSamples.LUKE.self().name());
                        softly.assertThat(entity.self().mail()).isEqualTo(UserSamples.LUKE.self().mail());
                    }));

            verify(kvStore).get(TOKEN_KEY_PREFIX + LUKE_TOKEN);
            verify(kvStore, never()).remove(any());
        }

        @Test
        void should_return_empty_when_token_does_not_exist() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + INVALID_TOKEN)).thenReturn(Optional.empty());

            // When
            Optional<Entity<User>> result = adapter.get(TOKEN_KEY_PREFIX + INVALID_TOKEN);

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
            Optional<Entity<User>> result = adapter.get(TOKEN_KEY_PREFIX + VADER_TOKEN);

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
            Optional<Entity<User>> result = adapter.get(TOKEN_KEY_PREFIX + VADER_TOKEN);

            // Then
            assertThat(result).isEmpty();

            verify(kvStore).get(TOKEN_KEY_PREFIX + VADER_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + VADER_TOKEN);
        }

        @Test
        void should_handle_different_users_correctly() {
            // Given
            when(kvStore.get(TOKEN_KEY_PREFIX + LEIA_TOKEN)).thenReturn(Optional.of(UserSamples.OBIWAN));

            // When
            Optional<Entity<User>> result = adapter.get(TOKEN_KEY_PREFIX + LEIA_TOKEN);

            // Then
            assertThat(result).isPresent()
                    .hasValueSatisfying(entity -> SoftAssertions.assertSoftly(softly -> {
                        softly.assertThat(entity.id()).isEqualTo(UserSamples.OBIWAN.id());
                        softly.assertThat(entity.self().login()).isEqualTo(UserSamples.OBIWAN.self().login());
                        softly.assertThat(entity.self().name()).isEqualTo(UserSamples.OBIWAN.self().name());
                        softly.assertThat(entity.self().mail()).isEqualTo(UserSamples.OBIWAN.self().mail());
                    }));
        }
    }

    @Nested
    class StoreTests {

        @Test
        void should_store_user_with_token_and_ttl() {
            // Given
            Duration ttl = Duration.ofMinutes(30);

            // When
            adapter.store(TOKEN_KEY_PREFIX + LUKE_TOKEN, UserSamples.LUKE, ttl);

            // Then
            verify(kvStore).put(
                    assertArg(key -> assertThat(key).isEqualTo(TOKEN_KEY_PREFIX + LUKE_TOKEN)),
                    assertArg(user -> assertThat(user).isEqualTo(UserSamples.LUKE)),
                    assertArg(ttlCaptor -> assertThat(ttlCaptor).isEqualTo(ttl))
            );
        }

        @Test
        void should_store_different_users_with_different_tokens() {
            // Given
            Duration shortTtl = Duration.ofMinutes(15);
            Duration longTtl = Duration.ofHours(2);

            // When
            adapter.store(TOKEN_KEY_PREFIX + LEIA_TOKEN, UserSamples.OBIWAN, shortTtl);
            adapter.store(TOKEN_KEY_PREFIX + VADER_TOKEN, UserSamples.DSIDIOUS, longTtl);

            // Then
            verify(kvStore).put(TOKEN_KEY_PREFIX + LEIA_TOKEN, UserSamples.OBIWAN, shortTtl);
            verify(kvStore).put(TOKEN_KEY_PREFIX + VADER_TOKEN, UserSamples.DSIDIOUS, longTtl);
            verify(kvStore, times(2)).put(any(String.class), any(Entity.class), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_hashed_token_isnull() {
            // When/Then
            Duration ttl = Duration.ofMinutes(30);
            assertThatThrownBy(() -> adapter.store(null, UserSamples.LUKE, ttl))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_user_isnull() {
            // When/Then
            Duration ttl = Duration.ofMinutes(30);
            assertThatThrownBy(() -> adapter.store(TOKEN_KEY_PREFIX + LUKE_TOKEN, null, ttl))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void should_throw_null_pointer_exception_when_ttl_isnull() {
            // When/Then
            assertThatThrownBy(() -> adapter.store(TOKEN_KEY_PREFIX + LUKE_TOKEN, UserSamples.LUKE, null))
                    .isInstanceOf(NullPointerException.class);

            verify(kvStore, never()).put(any(), any(), any(Duration.class));
        }
    }

    @Nested
    class RemoveTests {

        @Test
        void should_remove_token_from_store() {
            adapter.remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);

            verify(kvStore).remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);
        }

        @Test
        void should_remove_different_tokens() {
            // When
            adapter.remove(TOKEN_KEY_PREFIX + LEIA_TOKEN);
            adapter.remove(TOKEN_KEY_PREFIX + VADER_TOKEN);

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX + LEIA_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + VADER_TOKEN);
            verify(kvStore, times(2)).remove(any(String.class));
        }

        @Test
        void should_call_remove_even_with_non_existent_token() {
            // When
            adapter.remove(TOKEN_KEY_PREFIX + "death-star-plans-token");

            // Then
            verify(kvStore).remove(TOKEN_KEY_PREFIX + "death-star-plans-token");
        }

        @Test
        void should_handle_empty_tokens() {
            Assertions.assertThatThrownBy(() -> adapter.remove(""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class IntegrationTests {

        @Test
        void should_handle_complete_lifecycle() {
            // Given
            Duration ttl = Duration.ofHours(1);
            when(kvStore.get(TOKEN_KEY_PREFIX + LUKE_TOKEN)).thenReturn(Optional.of(UserSamples.LUKE));

            // When - Store
            adapter.store(TOKEN_KEY_PREFIX + LUKE_TOKEN, UserSamples.LUKE, ttl);

            // When - Get
            Optional<Entity<User>> actual = adapter.get(TOKEN_KEY_PREFIX + LUKE_TOKEN);

            // When - Remove
            adapter.remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);

            // Then
            assertThat(actual).isPresent().contains(UserSamples.LUKE);

            verify(kvStore).put(TOKEN_KEY_PREFIX + LUKE_TOKEN, UserSamples.LUKE, ttl);
            verify(kvStore).get(TOKEN_KEY_PREFIX + LUKE_TOKEN);
            verify(kvStore).remove(TOKEN_KEY_PREFIX + LUKE_TOKEN);
        }
    }
}