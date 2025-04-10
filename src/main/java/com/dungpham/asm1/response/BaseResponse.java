package com.dungpham.asm1.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class BaseResponse<T> {
    private boolean isStatus;
    private T metadata;

    public static <T> BaseResponse<T> build(T data, boolean isStatus) {
        return (BaseResponse<T>) BaseResponse.builder().isStatus(isStatus).metadata(data).build();
    }

    public static <T> BaseResponse<T> ok() {
        return (BaseResponse<T>) BaseResponse.builder().isStatus(true).metadata("Success").build();
    }
}
