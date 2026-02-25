package me.bombom.api.v1.subscribe.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import org.instancio.Instancio;

public class UnsubscribePatternFixture {

    public static UnsubscribePattern createUnsubscribePattern(String key, String value) {
        return Instancio.of(UnsubscribePattern.class)
                .set(field(UnsubscribePattern::getId), null)
                .set(field(UnsubscribePattern::getPatternKey), key)
                .set(field(UnsubscribePattern::getPatternValue), value)
                .create();
    }
}
