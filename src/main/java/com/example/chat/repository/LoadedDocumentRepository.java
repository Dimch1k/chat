package com.example.chat.repository;

import com.example.chat.model.LoadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadedDocumentRepository extends JpaRepository<LoadedDocument, Long> {
    boolean existsByFilenameAndContentHash(String filename, String contentHash);
}
