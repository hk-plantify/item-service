package com.plantify.forest.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private HttpStatus status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(HttpStatus.OK, "성공", null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(HttpStatus.OK, "성공", data);
    }

    public static <T> ApiResponse<T> fail(HttpStatus status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}