package com.strangegrotto.clijournal.model;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class EntryMetadata {
    private static final LocalDateTime MISSING_TIMESTAMP_DEFAULT_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final DateTimeFormatter FILENAME_DATE_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd[_HH:mm:ss]");

    private String nameSansExt;
    private String extension;
    private Optional<LocalDateTime> creationTimestamp;
    private Set<String> tags;

    EntryMetadata(
            String nameSansExt,
            String extension,
            Optional<LocalDateTime> creationTimestamp,
            Set<String> tags) {
        this.nameSansExt = nameSansExt;
        this.extension = extension;
        this.creationTimestamp = creationTimestamp;
        this.tags = tags;
    }

    public String getNameSansExt() {
        return nameSansExt;
    }

    public String getExtension() {
        return extension;
    }

    public Optional<LocalDateTime> getCreationTimestamp() {
        return creationTimestamp;
    }

    public Set<String> getTags() {
        return tags;
    }
}
