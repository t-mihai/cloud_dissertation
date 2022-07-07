package com.ism.dissertation.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.sql.Blob;

@Data
@Component
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;
    private String password;
    private String email;
    private Blob picture;

    public User() {
        super();
    }

    public User(int id, String username, String password, String email, Blob picture) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.picture = picture;
    }
}
