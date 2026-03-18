package com.yusung.realestateapi.backend.area.api;

import com.yusung.realestateapi.backend.area.application.AgentService;
import com.yusung.realestateapi.backend.area.domain.Agent;
import com.yusung.realestateapi.backend.area.dto.AgentSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AgentSignupRequest request) {
        try {
            Long agentId = agentService.signup(request);
            return ResponseEntity.ok("회원가입 성공! ID: " + agentId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 로그인 API
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");

            Agent agent = agentService.login(email, password);

            return ResponseEntity.ok(Map.of(
                    "message", "로그인 성공",
                    "agentId", agent.getAgentId(),
                    "name", agent.getName(),
                    "email", agent.getEmail(),
                    "officeName", agent.getOfficeName(),
                    "phone", agent.getPhone()
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "서버 내부 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 아이디/비밀번호 찾기 API
     * POST /api/agents/find-account
     */
    @PostMapping("/find-account")
    public ResponseEntity<?> findAccount(@RequestBody Map<String, String> findData) {
        try {
            String name = findData.get("name");
            String phone = findData.get("phone");
            String type = findData.get("type");

            String result;
            if ("email".equals(type)) {
                result = agentService.findEmail(name, phone);
            } else {
                result = agentService.findPassword(name, phone);
            }

            return ResponseEntity.ok(Map.of("result", result));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "오류가 발생했습니다."
            ));
        }
    }
}