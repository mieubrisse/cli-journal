package com.strangegrotto.clijournal.entrystore;

import java.nio.file.Path;

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

