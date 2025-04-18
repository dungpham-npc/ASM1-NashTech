package com.dungpham.asm1.request;

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
    private String email;
    private String newPassword;
    private String confirmNewPassword;
    private List<UpdateRecipientInfoRequest> recipientInfo;
}
