package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ListTagsCommand extends AbstractListingCommand<String> {
    private static final Comparator TAG_SORTER = Comparator.comparing(Function.identity());

    private final EntryStore entryStore;
    public ListTagsCommand(EntryStore entryStore) {
        super("tags", "Lists the tags currently in use in the journal");
        this.entryStore = entryStore;
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {}

    @Override
    protected ListingCmdResultType getResultType() {
        return ListingCmdResultType.TAG;
    }

    @Override
    protected List<String> getResults(Namespace parsedArgs) {
        List<String> tags = new ArrayList<>(this.entryStore.getAllTags());
        // If we want, one day we can make this configurable
        tags.sort(TAG_SORTER);
        return tags;
    }

    @Override
    protected String getResultReferenceValue(String result) {
        return result;
    }

    @Override
    protected String renderResult(String result, Namespace parsedArgs) {
        return result;
    }
}
