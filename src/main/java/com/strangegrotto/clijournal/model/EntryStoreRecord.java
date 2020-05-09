package com.strangegrotto.clijournal.model;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class EntryStoreRecord {
    private static final String METADATA_SEPARATOR = "~";
    private static final String TAG_SEPARATOR = ",";
    private static final LocalDateTime MISSING_TIMESTAMP_DEFAULT_DATE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final DateTimeFormatter FILENAME_DATE_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd[_HH:mm:ss]");

    private String filename;
    private String nameSansExt;
    private String extension;
    private LocalDateTime creationTimestamp;
    private Set<String> tags;

    private EntryStoreRecord(
            String filename,
            String nameSansExt,
            String extension,
            LocalDateTime creationTimestamp,
            Set<String> tags) {
        this.filename = filename;
        this.nameSansExt = nameSansExt;
        this.creationTimestamp = creationTimestamp;
        this.tags = tags;
    }

    public static EntryStoreRecord fromFilename(String filename) {
        String nameMinusExt = Files.getNameWithoutExtension(filename);
        String extension = Files.getFileExtension(filename);

        String[] nameFragments = nameMinusExt.split(METADATA_SEPARATOR);
        String nameSansExt = nameFragments[0];

        String creationTimestampStr = nameFragments.length >= 2 ? nameFragments[1] : "";
        LocalDateTime creationTimestamp;
        try {
            creationTimestamp = LocalDateTime.parse(creationTimestampStr, FILENAME_DATE_PARSER);
        } catch (DateTimeParseException e) {
            creationTimestamp = MISSING_TIMESTAMP_DEFAULT_DATE;
        }

        String tagsStr = nameFragments.length >= 3 ? nameFragments[2] : "";
        Set<String> tags = tagsStr.length() > 0 ? Sets.newHashSet(tagsStr.split(TAG_SEPARATOR)) : Sets.newHashSet();

        return new EntryStoreRecord(
                filename,
                nameSansExt,
                extension,
                creationTimestamp,
                tags
        );
    }

    public String getFilename() {
        return filename;
    }

    public String getNameSansExt() {
        return nameSansExt;
    }

    public String getExtension() {
        return extension;
    }

    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    public Set<String> getTags() {
        return tags;
    }
}
