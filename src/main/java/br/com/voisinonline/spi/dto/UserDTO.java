package br.com.voisinonline.spi.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class UserDTO implements Serializable {

    private static final long serialVersionUID = 8904116189236118280L;

    private String id;
    private String name;
    private String mail;
    private String phone;
    private List<String> relation;
    private boolean active;

    public UserDTO(String id, String name, String mail, String phone, List<String> relation, boolean active) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.relation = relation;
        this.active = active;
    }

    public UserDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getRelation() {
        return relation;
    }

    public void setRelation(List<String> relation) {
        this.relation = relation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDTO)) return false;
        UserDTO userDTO = (UserDTO) o;
        return active == userDTO.active &&
                Objects.equals(id, userDTO.id) &&
                Objects.equals(name, userDTO.name) &&
                Objects.equals(mail, userDTO.mail) &&
                Objects.equals(phone, userDTO.phone) &&
                Objects.equals(relation, userDTO.relation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, mail, phone, relation, active);
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mail='" + mail + '\'' +
                ", phone='" + phone + '\'' +
                ", relation='" + relation + '\'' +
                ", active=" + active +
                '}';
    }
}