package com.luv2code.springbootlibrary.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.pdf-dir:uploads/books/pdfs}")
    private String pdfStorageDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(pdfStorageDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier de stockage des PDF", e);
        }
    }

    public String storePdf(MultipartFile file) {
        try {
            if (file.isEmpty()) throw new RuntimeException("Fichier vide");
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String newFileName = UUID.randomUUID() + extension;
            Path destination = this.rootLocation.resolve(newFileName).normalize().toAbsolutePath();
            if (!destination.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Chemin de fichier invalide");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Échec du stockage du PDF", e);
        }
    }

    // ✅ Méthode utilitaire pour trouver le fichier PDF (normalisation des chemins)
    private Path getPdfPath(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        // Extraire le nom du fichier (après le dernier slash)
        String normalizedFileName = fileName;
        int lastSlash = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            normalizedFileName = fileName.substring(lastSlash + 1);
        }

        // 1. Chercher dans le dossier principal des PDF (uploads/books/pdfs)
        Path candidate = rootLocation.resolve(normalizedFileName).normalize().toAbsolutePath();
        if (Files.exists(candidate)) {
            return candidate;
        }

        // 2. Fallback : dossier uploads/books (sans /pdfs) – pour les anciens enregistrements
        Path fallback = Paths.get("uploads/books").resolve(normalizedFileName).normalize().toAbsolutePath();
        if (Files.exists(fallback)) {
            return fallback;
        }

        // 3. Fallback : essayer le chemin exact tel qu’il est stocké (ex: "uploads/books/uuid.pdf")
        Path exactPath = Paths.get(fileName).normalize().toAbsolutePath();
        if (Files.exists(exactPath)) {
            return exactPath;
        }

        // 4. Retourner le chemin par défaut (même s’il n’existe pas)
        return candidate;
    }

    public void deletePdf(String fileName) {
        Path path = getPdfPath(fileName);
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Impossible de supprimer le PDF", e);
            }
        }
    }

    public Path loadPdf(String fileName) {
        Path path = getPdfPath(fileName);
        if (path == null) throw new RuntimeException("Nom de fichier invalide");
        return path;
    }
}