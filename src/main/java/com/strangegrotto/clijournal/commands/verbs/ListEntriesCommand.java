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

public class ListEntriesCommand extends AbstractEntryListingCommand {
    private final EntryStore entryStore;

    public ListEntriesCommand(EntryStore entryStore) {
        super("ls", "Lists all entries in the journal");
        this.entryStore = entryStore;
    }

    @Override
    protected void configureEntryListingParser(ArgumentParser argumentParser) {}

    @Override
    protected Set<Entry> getEntries(Namespace parsedArgs) {
        return this.entryStore.getAllEntries();
    }
}
