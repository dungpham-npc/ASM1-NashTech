package com.dungpham.asm1.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UpdateRecipientInfoRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private Boolean isDefault;
}
