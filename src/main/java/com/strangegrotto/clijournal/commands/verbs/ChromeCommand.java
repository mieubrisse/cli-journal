package com.strangegrotto.clijournal.commands.verbs;

import com.google.common.collect.Lists;
import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.commands.ResultReferenceTranslator;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChromeCommand extends AbstractCommand {
    private static final String ENTRIES_ARG = "entry";

    private final EntryStore entryStore;
    private final ResultReferenceTranslator referenceTranslator;

    public ChromeCommand(EntryStore entryStore, ResultReferenceTranslator referenceTranslator) {
        super("chrome", "Opens the referenced result in Chrome");
        this.entryStore = entryStore;
        this.referenceTranslator = referenceTranslator;
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {
        argParser.addArgument(ENTRIES_ARG)
                .nargs("+")
                .help("Entries to open in Chrome");
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
        List<String> endArgs = Lists.newArrayList(
                // TODO only works on Mac - support other architectures
                "open",
                "-a",
                "Google Chrome"
        );
        endArgs.addAll(filepaths);

        return CommandResultMetadata.of(
                Optional.of(endArgs),
                Optional.empty()
        );
    }
}
