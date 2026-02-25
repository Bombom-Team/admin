package me.bombom.api.v1.withdraw.fixture;

import static org.instancio.Select.field;

import java.time.LocalDate;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.withdraw.domain.WithdrawnMember;
import org.instancio.Instancio;

public class WithdrawnMemberFixture {

    public static WithdrawnMember createWithdrawnMember(Long memberId, String email, LocalDate deletedDate) {
        return Instancio.of(WithdrawnMember.class)
                .set(field(WithdrawnMember::getId), null)
                .set(field(WithdrawnMember::getMemberId), memberId)
                .set(field(WithdrawnMember::getEmail), email)
                .set(field(WithdrawnMember::getGender), Gender.MALE)
                .set(field(WithdrawnMember::getJoinedDate), LocalDate.now().minusMonths(1))
                .set(field(WithdrawnMember::getDeletedDate), deletedDate)
                .set(field(WithdrawnMember::getExpireDate), deletedDate.plusDays(30))
                .create();
    }
}
