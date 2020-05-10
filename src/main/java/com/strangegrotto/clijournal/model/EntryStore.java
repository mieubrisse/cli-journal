package com.strangegrotto.clijournal.model;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class abstracting access to the actual filesystem containing the journal entries
 */
public class EntryStore {
    private static final String METADATA_SEPARATOR = "~";
    private static final String TAG_SEPARATOR = ",";
    private static final String PREFERRED_TIMESTAMP_FORMAT = "yyyy-MM-dd_HH:mm:ss";
    private static final Set<String> ACCEPTED_TIMESTAMP_FORMATS = Sets.newHashSet(
            PREFERRED_TIMESTAMP_FORMAT,
            "yyyy-MM-dd"
    );


    private final Path journalDirpath;
    private final List<String> blacklistedFilenamePatterns;
    private final EntryMetadataFormatter metadataFormatter;
    private final Set<String> filenames;
    private final SetMultimap<String, String> nameIndex;
    private final SetMultimap<String, String> tagIndex;

    /**
     * Constructs an entry store using the given parameters.
     * @param journalDirpath Directory where journal entries live
     * @param blacklistedFilenamePatterns Regexes containing file names which will not be loaded into the store
     * @throws IOException if an error reading the entries from the filesystem occurs
     */
    public EntryStore(Path journalDirpath, List<String> blacklistedFilenamePatterns) throws IOException {
        this.journalDirpath = journalDirpath.toAbsolutePath();
        this.blacklistedFilenamePatterns = blacklistedFilenamePatterns;
        this.metadataFormatter = new EntryMetadataFormatter(
                METADATA_SEPARATOR,
                TAG_SEPARATOR,
                ACCEPTED_TIMESTAMP_FORMATS,
                PREFERRED_TIMESTAMP_FORMAT
        );

        this.filenames = new HashSet<>();
        this.nameIndex = MultimapBuilder.hashKeys().hashSetValues().build();
        this.tagIndex = MultimapBuilder.hashKeys().hashSetValues().build();

        this.reindex();
    }

    /**
     * Re-loads the state of the EntryStore by reading the state of the filesystem. Use this method when the
     * files on disk have changed via a method other than the CLI itself.
     * @throws IOException if an error reading files occurs
     */
    public void reindex() throws IOException {
        List<Path> entryPaths = Files.list(journalDirpath)
                .filter(path -> isValidJournalEntry(path, this.blacklistedFilenamePatterns))
                .collect(Collectors.toList());

        this.filenames.clear();
        this.nameIndex.clear();
        this.tagIndex.clear();
        for (Path path : entryPaths) {
            String filename = path.getFileName().toString();
            EntryMetadata metadata = this.metadataFormatter.parseMetadata(filename);
            this.filenames.add(filename);
            this.nameIndex.put(metadata.getNameSansExt(), filename);
            for (String tag : metadata.getTags()) {
                this.tagIndex.put(tag, filename);
            }
        }
    }

    public Set<Entry> getAllEntries() {
        return this.filenames.stream()
                .map(this::entryFromFilename)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllTags() {
        return this.tagIndex.keySet();
    }

    public Set<Entry> getByIds(Set<String> ids) {
        return ids.stream()
                .filter(this.filenames::contains)
                .map(this::entryFromFilename)
                .collect(Collectors.toSet());
    }

    public Set<Entry> getByTag(String tag) {
        return this.tagIndex.get(tag).stream()
                .map(this::entryFromFilename)
                .collect(Collectors.toSet());
    }

    public Set<Entry> getByName(String keyword) {
        return this.nameIndex.keySet().stream()
                .filter(name -> name.contains(keyword))
                .flatMap(matchingName -> this.nameIndex.get(matchingName).stream())
                .map(this::entryFromFilename)
                .collect(Collectors.toSet());
    }

    // TODO Not suuuuper happy with this method - feels like it should be higher-level somehow
    public Path getNewEntryFilepath(String name, LocalDateTime creationTimestamp, Set<String> tags) {
        String nameSansExt = com.google.common.io.Files.getNameWithoutExtension(name);
        String extension = com.google.common.io.Files.getFileExtension(name);
        EntryMetadata metadata = new EntryMetadata(
                nameSansExt,
                extension,
                Optional.of(creationTimestamp),
                tags
        );
        String filename = this.metadataFormatter.formatMetadata(metadata);
        return Paths.get(this.journalDirpath.toString(), filename);
    }

    private Entry entryFromFilename(String filename) {
        // TODO Cache these values if it's too slow to parse it every time
        EntryMetadata metadata = this.metadataFormatter.parseMetadata(filename);
        Path absFilepath = Paths.get(this.journalDirpath.toString(), filename);
        return new Entry(
                absFilepath,
                metadata
        );
    }

    private static boolean isValidJournalEntry(Path path, List<String> blacklistedFilenamePatterns) {
        String filename = path.getFileName().toString();
        for (String pattern : blacklistedFilenamePatterns) {
            if (filename.matches(pattern)) {
                return false;
            }
        }
        return Files.isRegularFile(path);

    }
}
