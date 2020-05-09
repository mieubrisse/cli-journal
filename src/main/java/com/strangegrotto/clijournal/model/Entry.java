package com.strangegrotto.clijournal.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representation of a journal entry on the filesystem, returned by {@link EntryStore}
 */
public class Entry {
    private final Path filepath;
    private final LocalDateTime creationTimestamp;
    private final String name;
    private final List<String> tags;

    public Entry(Path filepath, LocalDateTime creationTimestamp, String name, List<String> tags) {
        this.filepath = filepath;
        this.creationTimestamp = creationTimestamp;
        this.name = name;
        this.tags = tags;
    }

    public Path getFilepath() {
        return filepath;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return tags;
    }
}
