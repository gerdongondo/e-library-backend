package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.entity.Category;

import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.luv2code.springbootlibrary.exception.ResourceNotFoundException;
import com.luv2code.springbootlibrary.exception.DuplicateResourceException;
import com.luv2code.springbootlibrary.exception.ResourceInUseException;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookRepository bookRepository;

    /**
     * Récupère toutes les catégories actives (accessible au public)
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    /**
     * Récupère toutes les catégories (y compris inactives) - Admin uniquement
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Récupère une catégorie par son ID
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée avec l'id: " + id));
    }

    /**
     * Crée une nouvelle catégorie
     */
    public Category createCategory(Category category) {
        // Vérifier si une catégorie avec le même nom existe déjà
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Une catégorie avec le nom '" + category.getName() + "' existe déjà");
        }

        // S'assurer que la catégorie est active par défaut
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }

        return categoryRepository.save(category);
    }

    /**
     * Met à jour une catégorie existante
     */
    public Category updateCategory(Long id, Category categoryDetails) {
        Category existingCategory = getCategoryById(id);

        // Vérifier si le nouveau nom n'est pas déjà utilisé par une autre catégorie
        if (!existingCategory.getName().equalsIgnoreCase(categoryDetails.getName()) &&
                categoryRepository.existsByNameIgnoreCase(categoryDetails.getName())) {
            throw new DuplicateResourceException("Une catégorie avec le nom '" + categoryDetails.getName() + "' existe déjà");
        }

        // Mettre à jour les champs
        existingCategory.setName(categoryDetails.getName());
        existingCategory.setDescription(categoryDetails.getDescription());

        // Ne pas modifier isActive ici (utiliser activate/deactivate à la place)

        return categoryRepository.save(existingCategory);
    }

    /**
     * Supprime une catégorie (vérifie d'abord si elle est utilisée)
     */
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);

        // Vérifier si des livres utilisent cette catégorie
        long booksInCategory = bookRepository.countByCategory(category.getName());

        if (booksInCategory > 0) {
            throw new ResourceInUseException(
                    "Impossible de supprimer la catégorie '" + category.getName() +
                            "' car elle est utilisée par " + booksInCategory + " livre(s). " +
                            "Veuillez d'abord réaffecter ces livres."
            );
        }

        categoryRepository.delete(category);
    }

    /**
     * Désactive une catégorie (soft delete)
     */
    public Category deactivateCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(false);
        return categoryRepository.save(category);
    }

    /**
     * Active une catégorie
     */
    public Category activateCategory(Long id) {
        Category category = getCategoryById(id);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    /**
     * Récupère toutes les catégories avec le nombre de livres associés
     */
    public List<CategoryWithBookCount> getCategoriesWithBookCount() {
        List<Object[]> results = categoryRepository.findAllWithBookCount();
        return results.stream()
                .map(result -> new CategoryWithBookCount(
                        (Category) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Récupère les catégories les plus utilisées
     */
    public List<CategoryUsage> getMostUsedCategories(int limit) {
        List<Object[]> results = categoryRepository.findMostUsedCategories(PageRequest.of(0, limit));
        return results.stream()
                .map(result -> new CategoryUsage(
                        (String) result[0],
                        ((Number) result[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Classe interne pour les catégories avec compteur
     */
    public static class CategoryWithBookCount {
        private Category category;
        private Long bookCount;

        public CategoryWithBookCount(Category category, Long bookCount) {
            this.category = category;
            this.bookCount = bookCount;
        }

        // Getters et setters
        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }
        public Long getBookCount() { return bookCount; }
        public void setBookCount(Long bookCount) { this.bookCount = bookCount; }
    }

    /**
     * Classe interne pour l'utilisation des catégories
     */
    public static class CategoryUsage {
        private String categoryName;
        private Long usageCount;

        public CategoryUsage(String categoryName, Long usageCount) {
            this.categoryName = categoryName;
            this.usageCount = usageCount;
        }

        // Getters et setters
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Long getUsageCount() { return usageCount; }
        public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    }
}