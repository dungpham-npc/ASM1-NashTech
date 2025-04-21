package com.dungpham.asm1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateUserProfileRequest {
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "The email format is incorrect.")
    @Schema(description = "email", example = "email@example.com")
    private String email;

    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
            message = "The new password must be at least 8 characters, including letters, numbers, and special characters.")
    @Schema(description = "new password", example = "Password123!")
    private String newPassword;

    @Schema(description = "confirm new password", example = "Password123!")
    private String confirmNewPassword;

    @Valid
    @Schema(description = "recipient information list")
    private List<RecipientInfoRequest> recipientInfo;
}