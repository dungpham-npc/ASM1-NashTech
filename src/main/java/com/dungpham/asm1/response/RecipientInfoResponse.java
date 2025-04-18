package com.dungpham.asm1.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class RecipientInfoResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private Boolean isDefault;
}
