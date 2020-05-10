package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryMetadata;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListEntriesCommand extends AbstractListingCommand<Entry> {
    private enum SortType {
        TIME(entry -> entry.getMetadata().getCreationTimestamp().orElse(MISSING_TIMESTAMP_SORT_VALUE)),
        NAME(entry -> entry.getMetadata().getNameSansExt());

        private final Function<Entry, Comparable> sortAttributeSupplier;

        SortType(Function<Entry, Comparable> sortAttributeSupplier) {
            this.sortAttributeSupplier = sortAttributeSupplier;
        }

        public Function<Entry, Comparable> getSortAttributeSupplier() {
            return this.sortAttributeSupplier;
        }
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss"
    );
    private static final SortType DEFAULT_SORT_TYPE = SortType.TIME;
    private static final String SORT_TYPE_ARG = "sort_type";
    private static final String REVERSE_ARG = "reverse";
    private static final LocalDateTime MISSING_TIMESTAMP_SORT_VALUE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final String MISSING_TIMESTAMP_RENDER_STR = "<no timestamp>";

    private final EntryStore entryStore;

    public ListEntriesCommand(EntryStore entryStore) {
        super("ls", "Lists all entries in the journal");
        this.entryStore = entryStore;
    }

    @Override
    protected void configureParser(ArgumentParser argumentParser) {
        List<String> sortChoices = Arrays.stream(SortType.values())
                .map(sortType -> sortType.name().toLowerCase())
                .collect(Collectors.toList());
        String defaultSortName = DEFAULT_SORT_TYPE.name().toLowerCase();
        argumentParser.addArgument("-r")
                .dest(REVERSE_ARG)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Reverse sort direction");
        argumentParser.addArgument("-s")
                .dest(SORT_TYPE_ARG)
                .choices(sortChoices)
                .setDefault(SortType.TIME)
                .help("Sort the results by the given value (default: " + defaultSortName + ")");
    }

    @Override
    protected ListingCmdResultType getResultType() {
        return ListingCmdResultType.JOURNAL_ENTRY;
    }

    @Override
    protected List getResults(Namespace parsedArgs) {
        SortType sortType = SortType.valueOf(parsedArgs.getString(SORT_TYPE_ARG).toUpperCase());
        Function<Entry, Comparable> sortAttributeSupplier = sortType.getSortAttributeSupplier();
        Set<Entry> entries = this.entryStore.getAllEntries();

        List<Entry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(Comparator.comparing(sortAttributeSupplier));
        return sortedEntries;
    }

    @Override
    protected String getResultReferenceValue(Entry result) {
        return result.getId();
    }

    @Override
    protected String renderResult(Entry result, Namespace parsedArgs) {
        EntryMetadata metadata = result.getMetadata();
        String pseudoName = metadata.getNameSansExt() + "." + metadata.getExtension();
        Optional<LocalDateTime> timestampOpt = metadata.getCreationTimestamp();
        String timestampStr;
        if (timestampOpt.isPresent()) {
            timestampStr = TIMESTAMP_FORMATTER.format(timestampOpt.get());
        } else {
            timestampStr = MISSING_TIMESTAMP_RENDER_STR;
        }
        String tagsStr = String.join(" ", metadata.getTags());

        return String.format(
                "%s    %s    %s",
                timestampStr,
                pseudoName,
                tagsStr
        );
    }
}
