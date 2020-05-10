package com.strangegrotto.clijournal.entrystore;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class abstracting access to the actual filesystem containing the journal entries
 */
public class EntryStore {
    private static final String METADATA_SEPARATOR = "~";
    private static final String TAG_SEPARATOR = ",";
    private static final String PREFERRED_TIMESTAMP_FORMAT = "yyyy-MM-dd_HH-mm-ss";
    private static final List<String> ACCEPTED_TIMESTAMP_FORMATS = Arrays.asList(
            "yyyy-MM-dd_HH-mm",
            PREFERRED_TIMESTAMP_FORMAT,
            "yyyy-MM-dd_HH:mm:ss"
    );


    private final Path journalDirpath;
    private final Set<String> blacklistedFilenamePatterns;
    private final EntryMetadataFormatter metadataFormatter;
    private final Map<String, EntryMetadata> filenamesAndMetadata; // We keep the metadata here only as a cache
    private final SetMultimap<String, String> nameIndex;
    private final SetMultimap<String, String> tagIndex;

    /**
     * Constructs an entry store using the given parameters.
     * @param journalDirpath Directory where journal entries live
     * @param blacklistedFilenamePatterns Regexes containing file names which will not be loaded into the store
     * @throws IOException if an error reading the entries from the filesystem occurs
     */
    public EntryStore(Path journalDirpath, Set<String> blacklistedFilenamePatterns) throws IOException {
        this.journalDirpath = journalDirpath.toAbsolutePath();
        this.blacklistedFilenamePatterns = blacklistedFilenamePatterns;
        this.metadataFormatter = new EntryMetadataFormatter(
                METADATA_SEPARATOR,
                TAG_SEPARATOR,
                ACCEPTED_TIMESTAMP_FORMATS,
                PREFERRED_TIMESTAMP_FORMAT
        );

        this.filenamesAndMetadata = new HashMap<>();
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

        this.filenamesAndMetadata.clear();
        this.nameIndex.clear();
        this.tagIndex.clear();
        for (Path path : entryPaths) {
            String filename = path.getFileName().toString();
            EntryMetadata metadata = this.metadataFormatter.parseMetadata(filename);
            this.filenamesAndMetadata.put(filename, metadata);
            this.nameIndex.put(metadata.getNameSansExt(), filename);
            for (String tag : metadata.getTags()) {
                this.tagIndex.put(tag, filename);
            }
        }
    }

    public Set<Entry> getAllEntries() {
        return this.filenamesAndMetadata.keySet().stream()
                .map(this::buildEntry)
                .collect(Collectors.toSet());
    }

    public Set<String> getAllTags() {
        return this.tagIndex.keySet();
    }

    public List<Entry> getByIds(List<String> ids) {
        return ids.stream()
                .filter(this.filenamesAndMetadata::containsKey)
                .map(this::buildEntry)
                .collect(Collectors.toList());
    }

    public Set<Entry> getByTag(String tag) {
        return this.tagIndex.get(tag).stream()
                .map(this::buildEntry)
                .collect(Collectors.toSet());
    }

    public Set<Entry> getByName(String keyword) {
        return this.nameIndex.keySet().stream()
                .filter(name -> name.contains(keyword))
                .flatMap(matchingName -> this.nameIndex.get(matchingName).stream())
                .map(this::buildEntry)
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

    // Technically we don't have to pass the metadata in - we could get it from the filename alone - but doing so
    //  is very slow
    private Entry buildEntry(String filename) {
        // TODO Cache these values if it's too slow to parse it every time
        Path absFilepath = Paths.get(this.journalDirpath.toString(), filename);
        EntryMetadata metadata = this.filenamesAndMetadata.get(filename);
        return new Entry(
                absFilepath,
                metadata
        );
    }

    private static boolean isValidJournalEntry(Path path, Set<String> blacklistedFilenamePatterns) {
        String filename = path.getFileName().toString();
        for (String pattern : blacklistedFilenamePatterns) {
            if (filename.matches(pattern)) {
                return false;
            }
        }
        return Files.isRegularFile(path);

    }
}
