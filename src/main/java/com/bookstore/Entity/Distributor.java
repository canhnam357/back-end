package com.bookstore.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "distributor")
public class Distributor  {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String distributorId;

    private String distributorName;

    private String nameNormalized;

    @OneToMany(mappedBy = "distributor")
    @JsonIgnore
    private List<Book> books;
}
