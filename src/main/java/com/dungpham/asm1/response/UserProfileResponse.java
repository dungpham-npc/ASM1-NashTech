package com.dungpham.asm1.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class UserProfileResponse {
    private String email;
    private List<RecipientInfoResponse> recipientInfo;
}
