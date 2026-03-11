package com.yusung.realestateapi.backend.area.infra;

import com.yusung.realestateapi.backend.area.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    // ✅ 로그인할 때: 입력한 이메일로 등록된 중개사가 있는지 찾습니다.
    Optional<Agent> findByEmail(String email);

    // ✅ 회원가입할 때: 이미 사용 중인 이메일인지 확인합니다.
    boolean existsByEmail(String email);

    Optional<Agent> findByNameAndPhone(String name, String phone);
}