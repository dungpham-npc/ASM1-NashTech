package com.dungpham.asm1.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class ExceptionResponse {
    private String code;
    private String message;
}
