package com.luv2code.springbootlibrary.repository;  // Changé de 'dao' à 'repository'


import com.luv2code.springbootlibrary.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Trouver une catégorie par son nom (ignorer la casse)
    Optional<Category> findByNameIgnoreCase(String name);

    // Vérifier si une catégorie existe par son nom
    boolean existsByNameIgnoreCase(String name);

    // Trouver toutes les catégories actives
    List<Category> findByIsActiveTrue();

    // Rechercher des catégories par nom (pour l'autocomplétion)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Obtenir toutes les catégories avec le nombre de livres associés
    @Query("SELECT c, COUNT(b) FROM Category c LEFT JOIN Book b ON LOWER(b.category) = LOWER(c.name) GROUP BY c")
    List<Object[]> findAllWithBookCount();

    // Obtenir les catégories les plus utilisées
    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category ORDER BY COUNT(b) DESC")
    List<Object[]> findMostUsedCategories(Pageable pageable);
}