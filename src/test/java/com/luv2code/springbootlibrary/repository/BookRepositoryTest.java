package com.luv2code.springbootlibrary.repository;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByTitleContaining_ShouldReturnMatches() {
        Book book = new Book();
        book.setTitle("Microservices with Spring Boot");
        book.setAuthor("John Doe");
        bookRepository.save(book);

        var result = bookRepository.findByTitleContaining("Microservices", PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Microservices");
    }
}