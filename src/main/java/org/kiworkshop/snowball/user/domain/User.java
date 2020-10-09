package org.kiworkshop.snowball.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiworkshop.snowball.note.domain.Note;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String name;
    private int age;
    private String gender;
    private String pictureUrl;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Note> notes;

    @Builder
    public User(String email, String name, int age, String gender, String pictureUrl) {
        this.email = email;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.pictureUrl = pictureUrl;
        this.notes = new ArrayList<>();
    }
}