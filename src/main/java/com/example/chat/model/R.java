package com.example.chat.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Setter
@Getter
public class R<T> {

    private Integer code;

    private T data;

    private String message;

    private static final Integer SUCCESS = 0;

    private static final Integer FAILED = -1;

    public static <T> R<T> ok() {
        return new R<T>()
                .setCode(SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return new R<T>()
                .setCode(SUCCESS)
                .setData(data);
    }

    public static <T> R<T> failed(String message) {
        return new R<T>()
                .setCode(FAILED)
                .setMessage(message);
    }
}
