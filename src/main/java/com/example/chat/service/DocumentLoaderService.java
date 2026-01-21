package com.example.chat.service;

import com.example.chat.model.LoadedDocument;
import com.example.chat.repository.LoadedDocumentRepository;
import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class DocumentLoaderService implements CommandLineRunner {

    @Autowired
    private LoadedDocumentRepository loadedDocumentRepository;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private VectorStore vectorStore;

    @SneakyThrows
    public void loadDocuments() {
        List<Resource> resources = Arrays.stream(resourcePatternResolver.getResources("classpath:/knowledgebase/**/*.txt")).toList();

        resources.stream()
                .map(resource -> Pair.of(resource, calculateHash(resource)))
                .filter(resourcePair -> !loadedDocumentRepository.existsByFilenameAndContentHash(resourcePair.getFirst().getFilename(), resourcePair.getSecond()))
                .forEach(resourcePair -> {
                    List<Document> documents = new TextReader(resourcePair.getFirst()).get();
                    TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder().withChunkSize(300).build();
                    List<Document> chunks = tokenTextSplitter.apply(documents);
                    vectorStore.accept(chunks);

                    LoadedDocument loadedDocument = LoadedDocument.builder()
                            .filename(resourcePair.getFirst().getFilename())
                            .contentHash(resourcePair.getSecond())
                            .documentType(Objects.requireNonNull(resourcePair.getFirst().getFilename()).split("\\.")[0])
                            .chunkCount(chunks.size())
                            .build();

                    loadedDocumentRepository.save(loadedDocument);
                });
    }

    @SneakyThrows
    private String calculateHash(Resource resource) {
        return DigestUtils.md5DigestAsHex(resource.getInputStream());
    }


    @Override
    public void run(String... args) throws Exception {
        loadDocuments();
    }
}
