package com.yusung.realestateapi.backend.area.application;

import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.dto.AgentSignupRequest;
import com.yusung.realestateapi.backend.area.infra.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentService {

    private final AgentRepository agentRepository;

    /**
     * ✅ 회원가입
     */
    public Long signup(AgentSignupRequest request) {
        if (agentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Agent agent = Agent.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .officeName(request.getOfficeName())
                .phone(request.getPhone())
                .kakaoOpenchatUrl(request.getKakaoOpenchatUrl())
                .build();

        return agentRepository.save(agent).getAgentId();
    }

    /**
     * ✅ 로그인
     */
    @Transactional(readOnly = true)
    public Agent login(String email, String password) {
        Agent agent = agentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));

        if (!agent.getPassword().equals(password)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return agent;
    }

    /**
     * ✅ 아이디(이메일) 찾기
     */
    @Transactional(readOnly = true)
    public String findEmail(String name, String phone) {
        return agentRepository.findByNameAndPhone(name, phone)
                .map(Agent::getEmail)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));
    }

    /**
     * ✅ 비밀번호 찾기
     */
    @Transactional(readOnly = true)
    public String findPassword(String name, String phone) {
        return agentRepository.findByNameAndPhone(name, phone)
                .map(Agent::getPassword)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원 정보가 없습니다."));
    }
}