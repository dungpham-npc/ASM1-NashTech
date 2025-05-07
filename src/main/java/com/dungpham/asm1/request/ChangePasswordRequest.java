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
public class ChangePasswordRequest {
    @NotNull(message = "Old password is required")
    @NotBlank(message = "Old password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message =
                    "The password must be at least 8 characters, including letters, numbers, and special characters.")
    @Schema(description = "old password", example = "Password123!")
    private String oldPassword;


    @NotNull(message = "New password is required")
    @NotBlank(message = "New password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message =
                    "The password must be at least 8 characters, including letters, numbers, and special characters.")
    @Schema(description = "new password", example = "Password456!")
    private String newPassword;
}
