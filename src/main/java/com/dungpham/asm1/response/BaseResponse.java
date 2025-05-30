package com.dungpham.asm1.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponse<T> {
    @JsonIgnore
    private boolean status;

    private String code;
    private String message;
    private T data;

    public static <T> BaseResponse<T> build(T data, boolean status) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setData(data);
        response.setStatus(status);
        response.setCode(status ? "200" : null);
        response.setMessage(status ? "Success" : null);
        return response;
    }
}
