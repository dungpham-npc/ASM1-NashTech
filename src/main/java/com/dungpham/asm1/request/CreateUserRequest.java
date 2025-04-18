package com.dungpham.asm1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CreateUserRequest {
    @NotNull(message = "Email is required")
    @NotBlank(message = "Email cannot be blank")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "The email format is incorrect.")
    @Schema(description = "email", example = "email@example.com")
    private String email;

    @NotNull(message = "Password is required")
    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message =
                    "The password must be at least 8 characters, including letters, numbers, and special characters.")
    @Schema(description = "password", example = "Password123!")
    private String password;

    @NotNull(message = "Role ID is required")
    @NotBlank(message = "Role ID cannot be blank")
    @Schema(description = "roleId 2 (CUSTOMER)", example = "2")
    private Long roleId;
}
