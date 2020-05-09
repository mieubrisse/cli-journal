package com.strangegrotto.clijournal.model;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class abstracting access to the actual filesystem containing the journal entries
 */
public class EntryStore {
    private final Path journalDirpath;
    private final List<String> blacklistedFilenamePatterns;
    private final Map<String, EntryStoreRecord> filenameIndex;
    private final Map<String, EntryStoreRecord> nameIndex;
    private final SetMultimap<String, EntryStoreRecord> tagIndex;

    public EntryStore(Path journalDirpath, List<String> blacklistedFilenamePatterns) throws IOException {
        this.journalDirpath = journalDirpath.toAbsolutePath();
        this.blacklistedFilenamePatterns = blacklistedFilenamePatterns;

        List<Path> entryPaths = Files.list(journalDirpath)
                .filter(path -> isValidJournalEntry(path, this.blacklistedFilenamePatterns))
                .collect(Collectors.toList());

        this.filenameIndex = new HashMap<>();
        this.nameIndex = new HashMap<>();
        this.tagIndex = MultimapBuilder.hashKeys().hashSetValues().build();
        for (Path path : entryPaths) {
            String filename = path.getFileName().toString();
            EntryStoreRecord record = EntryStoreRecord.fromFilename(filename);
            this.filenameIndex.put(filename, record);
            this.nameIndex.put(record.getNameSansExt(), record);
            for (String tag : record.getTags()) {
                this.tagIndex.put(tag, record);
            }
        }
    }

    public Set<Entry> getAllEntries() {
        return
    }

    private static Entry recordToEntry(EntryStoreRecord record) {
        return new Entry(
                record.getFilename()
        )
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
