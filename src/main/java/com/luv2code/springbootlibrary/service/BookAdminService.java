package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookAdminService {


    // create a new book with pdf file upload
    Book createBookWithPdfUpload(String title, String author, String description,
                                 String category, Double price, Integer numberOfPages,
                                 MultipartFile pdfFile,MultipartFile imgFile);

    // update book details
    Book updateBookDetails(Long bookId,
                           String title,
                           String author,
                           String description,
                           String category,
                           Double price,
                           Integer numberOfPages,
                           MultipartFile pdfFile,
                           MultipartFile imgFile);

    // delete book by id
    void deleteBookById(Long bookId);

    // delete book pdf file
    void deleteBookPdfFile(Long bookId);

    //ajout image a un livre
    void attachImageOnly(Long bookId, MultipartFile imgFile);

    void attachPdfOnly(Long bookId, MultipartFile pdfFile);

    // lire livre sur page admin
    Page<Book> getAllBooks(Pageable pageable);

}
