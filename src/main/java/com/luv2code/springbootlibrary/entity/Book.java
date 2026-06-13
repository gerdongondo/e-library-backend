package com.luv2code.springbootlibrary.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "book")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "author")
    private String author;

    @Column(name = "description")
    private String description;

    @Column(name = "copies")
    private int copies;

    @Column(name = "copies_available")
    private int copiesAvailable;

    @Column(name = "category")
    private String category;

    @Column(name = "img")
    private String img;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "price")
    private Double price;

    @Column(name = "number_of_pages")
    private Integer numberOfPages;

    // AJOUTE @JsonIgnore POUR ÉVITER LA RÉCURSION INFINIE
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore  // ← AJOUTE CETTE ANNOTATION
    private List<Review> reviews = new ArrayList<>();


}