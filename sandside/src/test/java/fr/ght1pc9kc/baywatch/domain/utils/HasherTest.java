package fr.ght1pc9kc.baywatch.domain.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

class HasherTest {
    @ParameterizedTest
    @CsvSource({
            "http://blog.ght1pc9kc.fr/, 6643b401c6d42f24523e16b727e9ae90fd202240721b78b3df8cdc4b3acb8f8f",
            "https://blog.ght1pc9kc.fr:80/, 6643b401c6d42f24523e16b727e9ae90fd202240721b78b3df8cdc4b3acb8f8f",
            "https://blog.ght1pc9kc.fr:443/, 6643b401c6d42f24523e16b727e9ae90fd202240721b78b3df8cdc4b3acb8f8f",
            "https://blog.ght1pc9kc.fr:8080/, e04c5cab3b37b018c8d24f3e1aece71cbc0d278ab8b4f8f4e0eccd3bcbf37cc2",
            "https://blog.ght1pc9kc.fr:443/test/retest/article.html, e030afa6d492079ded386a4b2081e90f7b6ba0ec0b9e1815f53c1ee400ff4b30",
            "https://blog.ght1pc9kc.fr:443/test/retest/article2.html, 2dc89eac72030699a9f8f2cddc6a79362479f23320b76b2ce1c1a1df67dad381",
            "https://blog.ght1pc9kc.fr:443/test/retest/, 42e06c6a8292c4de4b8063f0ea5768a621f7c800e5a98ce199c8bb6d657c0ed3",
            "https://blog.ght1pc9kc.fr:443/test/retest, 5843581cb979b1fb10f6d807071296a317a78e5d0ff3c6f37edddeb24da307af",
            "http://blog.ght1pc9kc.fr/test/retest, 5843581cb979b1fb10f6d807071296a317a78e5d0ff3c6f37edddeb24da307af",
    })
    void should_hash_uri(URI toHash, String expected) {
        Assertions.assertThat(Hasher.identify(toHash)).isEqualTo(expected);
    }
}