package me.bombom.api.v1.newsletterrequest.collector;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PublicUrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "http://localhost:8080/admin",
            "http://127.0.0.1/",
            "http://10.0.0.1/",
            "http://192.168.0.10/",
            "http://169.254.169.254/latest/meta-data",
            "ftp://example.com/",
            "not a url"
    })
    void 내부망이나_허용하지_않는_주소로는_요청할_수_없다(String url) {
        // when & then
        assertThatThrownBy(() -> PublicUrlValidator.validate(url))
                .isInstanceOf(CollectFailedException.class);
    }
}
