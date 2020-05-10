package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryMetadata;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractEntryListingCommand extends AbstractListingCommand<Entry> {
    private enum SortType {
        TIME(Comparator.comparing(
                entry -> entry.getMetadata().getCreationTimestamp().orElse(MISSING_TIMESTAMP_SORT_VALUE)
        )),
        NAME(Comparator.comparing(
                entry -> entry.getMetadata().getNameSansExt()
        ));

        private final Comparator<Entry> comparator;

        SortType(Comparator<Entry> comparator) {
            this.comparator = comparator;
        }

        public Comparator<Entry> getComparator() {
            return this.comparator;
        }
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss"
    );
    private static final SortType DEFAULT_SORT_TYPE = SortType.TIME;
    private static final String SORT_TYPE_ARG = "sort_type";
    private static final String REVERSE_SORT_ARG = "reverse";
    private static final LocalDateTime MISSING_TIMESTAMP_SORT_VALUE = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
    private static final String MISSING_TIMESTAMP_RENDER_STR = "  <no timestamp>   ";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public AbstractEntryListingCommand(String alias, String helpStr) {
        super(alias, helpStr);
    }

    @Override
    protected final void configureParser(ArgumentParser argumentParser) {
        List<String> sortChoices = Arrays.stream(SortType.values())
                .map(sortType -> sortType.name().toLowerCase())
                .collect(Collectors.toList());
        argumentParser.addArgument("-r")
                .dest(REVERSE_SORT_ARG)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Reverse sort direction");
        argumentParser.addArgument("-n")
                .dest(SORT_TYPE_ARG)
                .setDefault(DEFAULT_SORT_TYPE)
                .action(Arguments.storeConst())
                .setConst(SortType.NAME.toString())
                .help("Sort the results by name");
        this.configureEntryListingParser(argumentParser);
    }

    @Override
    protected ListingCmdResultType getResultType() {
        return ListingCmdResultType.JOURNAL_ENTRY;
    }

    @Override
    protected List<Entry> getResults(Namespace parsedArgs) {
        SortType sortType = SortType.valueOf(parsedArgs.getString(SORT_TYPE_ARG));
        Comparator<Entry> comparator = sortType.getComparator();

        boolean reverseSort = parsedArgs.getBoolean(REVERSE_SORT_ARG);
        comparator = reverseSort ? comparator.reversed() : comparator;

        Set<Entry> entries = this.getEntries(parsedArgs);

        List<Entry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort(comparator);
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
                "%s%s    %s%s    %s%s%s",
                ANSI_YELLOW,
                timestampStr,
                ANSI_WHITE,
                pseudoName,
                ANSI_PURPLE,
                tagsStr,
                ANSI_RESET
        );
    }

    protected abstract Set<Entry> getEntries(Namespace parsedArgs);

    protected abstract void configureEntryListingParser(ArgumentParser argumentParser);
}
