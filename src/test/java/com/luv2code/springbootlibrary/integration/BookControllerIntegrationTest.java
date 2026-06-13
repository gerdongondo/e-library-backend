package com.luv2code.springbootlibrary.integration;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.entity.Book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        Book book = new Book();
        book.setTitle("Spring Security");
        book.setAuthor("Laurentiu Spilca");
        book.setCategory("IT");
        book.setCopies(3);
        book.setCopiesAvailable(3);
        bookRepository.save(book);
    }

    @Test
    void searchByTitle_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search/title")
                        .param("title", "Security")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", containsString("Security")));
    }
}