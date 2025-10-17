package com.example.logging.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 3, max = 50, message = "사용자명은 3-50자 사이여야 합니다")
    private String username;

    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "유효한 이메일 주소를 입력하세요")
    private String email;

    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
    private String password;

    private String fullName;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 회원가입 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "사용자명은 필수입니다")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "유효한 이메일 주소를 입력하세요")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String password;

        private String fullName;
    }

    /**
     * 로그인 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "이메일은 필수입니다")
        @Email
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }

    /**
     * 로그인 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {

        private String accessToken;
        private String refreshToken;
        private String tokenType = "Bearer";
        private UserDTO user;
    }

    /**
     * 사용자 업데이트 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @Email(message = "유효한 이메일 주소를 입력하세요")
        private String email;

        private String fullName;

        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;

        private String currentPassword;
    }
}
