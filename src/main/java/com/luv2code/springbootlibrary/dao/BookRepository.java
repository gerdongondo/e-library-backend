package com.luv2code.springbootlibrary.dao;

import com.luv2code.springbootlibrary.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

public interface BookRepository  extends JpaRepository<Book,Long> {


    Page<Book> findByTitleContaining(@RequestParam("title") String title, Pageable pageable);

    Page<Book> findByCategory(@RequestParam("category") String category,Pageable pageable );

    // Nouvelle méthode à ajouter pour CategoryService
    @Query("SELECT COUNT(b) FROM Book b WHERE LOWER(b.category) = LOWER(:category)")
    long countByCategory(@Param("category") String category);

    /**
     * Recherche avancée avec filtres dynamiques pour la bibliothèque universitaire
     * Tous les paramètres sont optionnels (peuvent être null)
     * La recherche est insensible à la casse
     *
     * @param title     Titre du livre (recherche partielle)
     * @param author    Auteur du livre (recherche partielle)
     * @param category  Catégorie du livre (recherche exacte insensible à la casse)
     * @param keyword   Mot-clé recherché dans le titre, l'auteur ou la description
     * @param pageable  Informations de pagination
     * @return Page de livres correspondant aux critères
     */
    @Query("SELECT DISTINCT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR LOWER(b.category) = LOWER(:category)) AND " +
            "(:keyword IS NULL OR " +
            "   LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "   LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> advancedSearch(@Param("title") String title,
                              @Param("author") String author,
                              @Param("category") String category,
                              @Param("keyword") String keyword,
                              Pageable pageable);

    /**
     * Version simplifiée pour la recherche combinée titre + auteur + catégorie
     */
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:category IS NULL OR LOWER(b.category) = LOWER(:category))")
    Page<Book> searchByTitleAuthorCategory(@Param("title") String title,
                                           @Param("author") String author,
                                           @Param("category") String category,
                                           Pageable pageable);
}
