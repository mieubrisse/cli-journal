package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.commands.ResultReferenceTranslator;
import com.strangegrotto.clijournal.entrystore.Entry;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VimCommand extends AbstractCommand {
    private static final String ENTRIES_ARG = "entry";

    private final EntryStore entryStore;
    private final ResultReferenceTranslator referenceTranslator;

    public VimCommand(EntryStore entryStore, ResultReferenceTranslator referenceTranslator) {
        super("vim", "Opens the referenced result in vim, using vertical splits if more than one result");
        this.entryStore = entryStore;
        this.referenceTranslator = referenceTranslator;
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {
        argParser.addArgument(ENTRIES_ARG)
                .nargs("+")
                .help("Entries to open in Vim");
    }

    @Override
    public CommandResultMetadata runCommandLogic(Namespace parsedArgs) {
        List<String> entryTokens = parsedArgs.getList(ENTRIES_ARG);
        List<String> entryIds;
        try {
            entryIds = this.referenceTranslator.dereferenceTokens(
                    ListingCmdResultType.JOURNAL_ENTRY,
                    entryTokens
            );
        } catch (ResultReferenceTranslator.ResultDereferenceException e) {
            System.out.println(e.getMessage());
            return CommandResultMetadata.empty();
        }
        List<String> filepaths = this.entryStore.getByIds(entryIds).stream()
                .map(entry -> entry.getFilepath().toString())
                .collect(Collectors.toList());
        List<String> endArgs = new ArrayList<>();
        endArgs.add("vim");
        if (filepaths.size() > 1) {
            endArgs.add("-O");
        }
        endArgs.addAll(filepaths);

        return CommandResultMetadata.of(
                Optional.of(endArgs),
                Optional.empty()
        );
    }
}
