package com.strangegrotto.clijournal.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Representation of a journal entry on the filesystem, returned by {@link EntryStore}
 */
public class Entry {
    private final Path filepath;
    private final EntryMetadata metadata;

    public Entry(Path filepath, EntryMetadata metadata) {
        this.filepath = filepath;
        this.metadata = metadata;
    }

    public Path getFilepath() {
        return filepath;
    }

    public String getId() {
        return this.filepath.getFileName().toString();
    }

    public EntryMetadata getMetadata() {
        return metadata;
    }
}

