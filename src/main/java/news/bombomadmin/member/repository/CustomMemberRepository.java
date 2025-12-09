package news.bombomadmin.member.repository;

import news.bombomadmin.member.dto.GetMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMemberRepository {

    Page<GetMemberResponse> findMemberInfo(Pageable pageable, String name, String role);
}
