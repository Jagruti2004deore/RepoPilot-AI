package com.repolens.backend.ai.rag;

import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RepositoryChunker {
    private static final int MIN_CHUNK_SIZE = 300;

    private final int chunkSize;
    private final int overlapSize;
    private final int maxChunksPerFile;

    public RepositoryChunker(
            @Value("${app.ai.rag.chunk-size:1800}") int chunkSize,
            @Value("${app.ai.rag.chunk-overlap:200}") int overlapSize,
            @Value("${app.ai.rag.max-chunks-per-file:20}") int maxChunksPerFile
    ) {
        this.chunkSize = Math.max(chunkSize, MIN_CHUNK_SIZE);
        this.overlapSize = Math.max(0, Math.min(overlapSize, this.chunkSize / 2));
        this.maxChunksPerFile = Math.max(1, maxChunksPerFile);
    }

    public List<RepositoryChunk> chunk(RepositoryProject repository, List<RepositoryFile> files) {
        return files.stream()
                .filter(this::isIndexable)
                .flatMap(file -> chunkFile(repository, file).stream())
                .toList();
    }

    private List<RepositoryChunk> chunkFile(RepositoryProject repository, RepositoryFile file) {
        String content = normalize(file.getContent());
        List<RepositoryChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        int start = 0;

        while (start < content.length() && chunkIndex < maxChunksPerFile) {
            int end = Math.min(content.length(), start + chunkSize);
            if (end < content.length()) {
                end = nearestLineBreak(content, start, end);
            }

            String chunkContent = content.substring(start, end).trim();
            if (!chunkContent.isBlank()) {
                chunks.add(new RepositoryChunk(
                        repository.getId(),
                        file.getId(),
                        file.getPath(),
                        file.getLanguage(),
                        chunkIndex,
                        chunkContent
                ));
                chunkIndex++;
            }

            if (end >= content.length()) {
                break;
            }
            start = Math.max(end - overlapSize, start + 1);
        }

        return chunks;
    }

    private int nearestLineBreak(String content, int start, int proposedEnd) {
        int lineBreak = content.lastIndexOf('\n', proposedEnd);
        if (lineBreak <= start + MIN_CHUNK_SIZE) {
            return proposedEnd;
        }
        return lineBreak;
    }

    private boolean isIndexable(RepositoryFile file) {
        return file.getId() != null
                && file.getContent() != null
                && !file.getContent().isBlank()
                && file.getContent().length() <= 200_000;
    }

    private String normalize(String content) {
        return content == null ? "" : content.replace("\r\n", "\n").replace('\r', '\n').trim();
    }
}