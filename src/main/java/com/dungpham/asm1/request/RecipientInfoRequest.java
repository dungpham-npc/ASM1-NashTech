package com.dungpham.asm1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RecipientInfoRequest {
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 40, message = "First name cannot exceed 40 characters")
    @Schema(description = "recipient's first name", example = "John")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 40, message = "Last name cannot exceed 40 characters")
    @Schema(description = "recipient's last name", example = "Smith")
    private String lastName;

    @NotNull(message = "Phone number is required")
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10-15 digits")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Schema(description = "recipient's phone number", example = "1234567890")
    private String phone;

    @NotNull(message = "Address is required")
    @NotBlank(message = "Address cannot be blank")
    @Size(max = 100, message = "Address cannot exceed 100 characters")
    @Schema(description = "recipient's address", example = "123 Main St, City, Country")
    private String address;

    @Schema(description = "default delivery address flag", example = "true")
    private Boolean isDefault;
}