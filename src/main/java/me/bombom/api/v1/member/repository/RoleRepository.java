package me.bombom.api.v1.member.repository;

import java.util.Optional;
import me.bombom.api.v1.member.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByAuthority(String role);
}
