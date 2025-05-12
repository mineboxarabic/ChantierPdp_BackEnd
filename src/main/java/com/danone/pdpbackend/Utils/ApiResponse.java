package com.danone.pdpbackend.Utils;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse<T> {

    // Getters and Setters
    private T data;
    private String message;

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public ApiResponse() {
        this.message = "";
        this.data = null;
    }

}