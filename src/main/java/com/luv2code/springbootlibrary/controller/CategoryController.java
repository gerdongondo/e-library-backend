package com.luv2code.springbootlibrary.controller;

import com.luv2code.springbootlibrary.entity.Category;
import com.luv2code.springbootlibrary.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ================ ENDPOINTS PUBLICS ================

    /**
     * Récupère toutes les catégories actives
     * Accessible à tous (sans authentification)
     * GET /api/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllActiveCategories() {
        List<Category> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Récupère une catégorie spécifique par son ID
     * Accessible à tous
     * GET /api/categories/{id}
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Récupère les statistiques des catégories
     * Accessible à tous
     * GET /api/categories/stats/most-used?limit=5
     */
    @GetMapping("/categories/stats/most-used")
    public ResponseEntity<?> getMostUsedCategories(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(categoryService.getMostUsedCategories(limit));
    }

    /**
     * Récupère toutes les catégories avec le nombre de livres
     * Accessible à tous
     * GET /api/categories/with-book-count
     */
    @GetMapping("/categories/with-book-count")
    public ResponseEntity<?> getCategoriesWithBookCount() {
        return ResponseEntity.ok(categoryService.getCategoriesWithBookCount());
    }

    // ================ ENDPOINTS ADMIN ================

    /**
     * Crée une nouvelle catégorie
     * Réservé aux administrateurs
     * POST /api/admin/categories
     */
    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    /**
     * Met à jour une catégorie existante
     * Réservé aux administrateurs
     * PUT /api/admin/categories/{id}
     */
    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category categoryDetails) {
        Category updatedCategory = categoryService.updateCategory(id, categoryDetails);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * Supprime une catégorie
     * Réservé aux administrateurs
     * DELETE /api/admin/categories/{id}
     */
    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Désactive une catégorie (soft delete)
     * Réservé aux administrateurs
     * PATCH /api/admin/categories/{id}/deactivate
     */
    @PatchMapping("/admin/categories/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> deactivateCategory(@PathVariable Long id) {
        Category category = categoryService.deactivateCategory(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Active une catégorie
     * Réservé aux administrateurs
     * PATCH /api/admin/categories/{id}/activate
     */
    @PatchMapping("/admin/categories/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> activateCategory(@PathVariable Long id) {
        Category category = categoryService.activateCategory(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Récupère toutes les catégories (y compris inactives)
     * Réservé aux administrateurs
     * GET /api/admin/categories
     */
    @GetMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Category>> getAllCategoriesAdmin() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}