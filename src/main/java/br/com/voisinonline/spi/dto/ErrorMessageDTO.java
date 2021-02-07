package br.com.voisinonline.spi.dto;

import java.util.Arrays;

public class ErrorMessageDTO {

    private int code;
    private String message;
    private String[] details;

    public ErrorMessageDTO(int code, String message, String[] details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public ErrorMessageDTO() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getDetails() {
        return details;
    }

    public void setDetails(String[] details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "ErrorMessageDTO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", details=" + Arrays.toString(details) +
                '}';
    }
}