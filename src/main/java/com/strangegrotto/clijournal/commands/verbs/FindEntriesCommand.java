package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.Set;
import java.util.function.BiFunction;

public class FindEntriesCommand extends AbstractEntryListingCommand {
    private enum SearchType {
        TAG((entryStore, keyword) -> entryStore.getByTag(keyword)),
        NAME((entryStore, keyword) -> entryStore.getByName(keyword));

        private final BiFunction<EntryStore, String, Set<Entry>> storeQueryFunc;

        SearchType(BiFunction<EntryStore, String, Set<Entry>> storeQueryFunc) {
            this.storeQueryFunc = storeQueryFunc;
        }

        public BiFunction<EntryStore, String, Set<Entry>> getStoreQueryFunc() {
            return storeQueryFunc;
        }
    }
    private static final String SEARCH_TYPE_ARG = "find_type";
    private static final String SEARCH_TERM_ARG = "search_term";

    private final EntryStore entryStore;

    public FindEntriesCommand(EntryStore entryStore) {
        super("find", "Finds entries matching the given parameters");
        this.entryStore = entryStore;
    }

    @Override
    protected void configureEntryListingParser(ArgumentParser argumentParser) {
        argumentParser.addArgument("-t")
                .dest(SEARCH_TYPE_ARG)
                .setDefault(SearchType.NAME.toString())
                .action(Arguments.storeConst())
                .setConst(SearchType.TAG.toString())
                .help("Searches for a tag");
        argumentParser.addArgument(SEARCH_TERM_ARG)
                .help("Term to search for");
    }

    @Override
    protected Set<Entry> getEntries(Namespace parsedArgs) {
        String searchTerm = parsedArgs.getString(SEARCH_TERM_ARG);
        SearchType searchType = SearchType.valueOf(parsedArgs.getString(SEARCH_TYPE_ARG));
        return searchType.getStoreQueryFunc().apply(this.entryStore, searchTerm);
    }
}
