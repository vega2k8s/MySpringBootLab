package com.rookies3.myspringbootlab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publishers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Publisher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "publisher_id")
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "established_date")
    private LocalDate establishedDate;
    
    private String address;
    
    @JsonIgnore
    @OneToMany(mappedBy = "publisher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Book> books = new ArrayList<>();
    
    // Helper methods
    public void addBook(Book book) {
        books.add(book);
        book.setPublisher(this);
    }
    
    public void removeBook(Book book) {
        books.remove(book);
        book.setPublisher(null);
    }
}