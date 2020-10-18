package com.strangegrotto.clijournal.commands.verbs;

import com.google.common.collect.Lists;
import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.commands.ResultReferenceTranslator;
import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.HashSet;
import java.util.List;
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
    private final ResultReferenceTranslator refTranslator;

    public FindEntriesCommand(EntryStore entryStore, ResultReferenceTranslator refTranslator) {
        super("find", "Finds entries containing the given parameters");
        this.entryStore = entryStore;
        this.refTranslator = refTranslator;
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

    // TODO Make this function Go-like, and return both a return value and an error!
    @Override
    protected Set<Entry> getEntries(Namespace parsedArgs) {
        String searchTerm = parsedArgs.getString(SEARCH_TERM_ARG);

        SearchType searchType;
        try {
            searchType = SearchType.valueOf(parsedArgs.getString(SEARCH_TYPE_ARG));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            // TODO Make this type a combination of error and result!!
            return new HashSet<>();
        }

        ListingCmdResultType expectedPreviousCmdResultType;
        switch (searchType) {
            // TODO it doesn't make much sense to do a dereference by name, since if you already have the name (from a
            //  previous find command) you could just open that directly
            case NAME:
                expectedPreviousCmdResultType = ListingCmdResultType.JOURNAL_ENTRY;
                break;
            case TAG:
                expectedPreviousCmdResultType = ListingCmdResultType.TAG;
                break;
            default:
                System.out.println("Error: Unhandled search type '" + searchType + "'; this is a code error");
                return new HashSet<>();
        }

        List<String> dereferencedSearchTerms;
        try {
            dereferencedSearchTerms = this.refTranslator.dereferenceTokens(
                    expectedPreviousCmdResultType,
                    Lists.newArrayList(searchTerm)
            );
        } catch (ResultReferenceTranslator.ResultDereferenceException e) {
            System.out.println(e.getMessage());
            return new HashSet<>();
        }

        // TODO Upgrade the entry store to allow multiple search terms
        if (dereferencedSearchTerms.size() != 1) {
            System.out.println("Error: Expected exactly one search term but got " + dereferencedSearchTerms.size() + ": " + dereferencedSearchTerms);
            return new HashSet<>();
        }
        String dereferencedSearchTerm = dereferencedSearchTerms.get(0);

        // TODO Debugging
        System.out.println("Search Term: " + dereferencedSearchTerm);

        // TODO Debugging
        System.out.println("Search Type: " + searchType);

        return searchType.getStoreQueryFunc().apply(this.entryStore, dereferencedSearchTerm);
    }
}
