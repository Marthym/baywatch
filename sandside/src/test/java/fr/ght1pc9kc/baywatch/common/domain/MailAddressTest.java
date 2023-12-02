package fr.ght1pc9kc.baywatch.common.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MailAddressTest {
    @ParameterizedTest
    @CsvSource({
            "obiwan.kenobi@jedi.com, obiwan.kenobi@jedi.com",
            "obiwan+kenobi@jedi.com, obiwan@jedi.com",
            "okenobi@jedi.com <Obiwan Kenobi>, okenobi@jedi.com",
    })
    void should_sanitize_mail_address(String mail, String expected) {
        Assertions.assertThat(MailAddress.sanitize(mail))
                .isEqualTo(expected);
    }
}