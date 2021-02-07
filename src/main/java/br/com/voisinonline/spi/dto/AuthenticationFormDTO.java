package br.com.voisinonline.spi.dto;

import java.io.Serializable;
import java.util.Objects;

public class AuthenticationFormDTO implements Serializable {
    private static final long serialVersionUID = -1601730219925155076L;

    private String mail;
    private String password;

    public AuthenticationFormDTO(String mail, String password) {
        this.mail = mail;
        this.password = password;
    }

    public AuthenticationFormDTO() {
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticationFormDTO that = (AuthenticationFormDTO) o;
        return Objects.equals(mail, that.mail) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mail, password);
    }
}