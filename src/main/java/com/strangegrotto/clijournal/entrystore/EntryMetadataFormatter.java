package com.strangegrotto.clijournal.entrystore;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to translate between entry metadata and an on-disk filename
 */
class EntryMetadataFormatter {
    private final String metadataSeparator;
    private final String tagSeparator;
    private final List<DateTimeFormatter> parseFormatters;
    private final DateTimeFormatter formatFormatter;

    /**
     * @param metadataSeparator String to use to separate metadata elements in filename
     * @param tagSeparator String to use to separate tags in filename
     * @param acceptedTimestampFormats Timestamp formats that we'll accept when parsing entry timestamp format.
     * @param preferredTimestampFormat Format we'll use when formatting timstamp when writing a filename
     */
    EntryMetadataFormatter(
            String metadataSeparator,
            String tagSeparator,
            List<String> acceptedTimestampFormats,
            String preferredTimestampFormat) {
        Preconditions.checkState(
                metadataSeparator != tagSeparator,
                "Metadata separator and tag separator cannot be equal"
        );
        Preconditions.checkState(
                metadataSeparator.length() > 0,
                "Metadata separator must be at least one character"
        );
        Preconditions.checkState(
                tagSeparator.length() > 0,
                "Tag separator must be at least one character"
        );
        this.metadataSeparator = metadataSeparator;
        this.tagSeparator = tagSeparator;

        this.formatFormatter = DateTimeFormatter.ofPattern(preferredTimestampFormat);

        // For simplicity, rather than checking if preferredTimestampFormat is contained in acceptedTimestampFormats,
        //  just add it in
        List<DateTimeFormatter> parseFormatters = acceptedTimestampFormats.stream()
                .map(DateTimeFormatter::ofPattern)
                .collect(Collectors.toList());
        parseFormatters.add(this.formatFormatter);
        this.parseFormatters = parseFormatters;
    }

    public EntryMetadata parseMetadata(String filename) {
        String nameMinusExt = Files.getNameWithoutExtension(filename);
        String extension = Files.getFileExtension(filename);

        String[] nameFragments = nameMinusExt.split(this.metadataSeparator);
        String nameSansExt = nameFragments[0];

        String creationTimestampStr = nameFragments.length >= 2 ? nameFragments[1] : "";
        Optional<LocalDateTime> creationTimestamp = Optional.empty();
        for (DateTimeFormatter parseFormatter : this.parseFormatters) {
            try {
                creationTimestamp = Optional.of(LocalDateTime.parse(creationTimestampStr, parseFormatter));
                break;
            } catch (DateTimeParseException e) {}
        }

        String tagsStr = nameFragments.length >= 3 ? nameFragments[2] : "";
        Set<String> tags = tagsStr.length() > 0 ? Sets.newHashSet(tagsStr.split(this.tagSeparator)) : Sets.newHashSet();

        return new EntryMetadata(
                nameSansExt,
                extension,
                creationTimestamp,
                tags
        );
    }

    public String formatMetadata(EntryMetadata metadata) {
        Optional<LocalDateTime> timestampOpt = metadata.getCreationTimestamp();
        String timestampStr;
        if (timestampOpt.isPresent()) {
            timestampStr = this.formatFormatter.format(timestampOpt.get());
        } else {
            timestampStr = "";
        }
        String tagsStr = String.join(this.tagSeparator, metadata.getTags());
        String extensionLess = String.join(
                this.metadataSeparator,
                metadata.getNameSansExt(),
                timestampStr,
                tagsStr
        );
        String extension = metadata.getExtension();
        return extension.length() > 0 ? extensionLess + "." + metadata.getExtension() : extensionLess;
    }
}
