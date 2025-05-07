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
public class OtpRequest {
    @NotNull(message = "OTP is required")
    @NotBlank(message = "OTP cannot be blank")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP must be exactly 6 digits")
    @Schema(description = "6-digit OTP code", example = "123456")
    private String otp;
}
