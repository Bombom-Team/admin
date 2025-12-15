package news.bombomadmin.notice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum NoticeCategory {

    NOTICE("공지"),
    UPDATE("업데이트"),
    EVENT("이벤트"),
    CHECK("점검"),
    ;

    private final String value;
}
