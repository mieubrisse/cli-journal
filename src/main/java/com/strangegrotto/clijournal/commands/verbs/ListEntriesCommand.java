package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.Set;

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
