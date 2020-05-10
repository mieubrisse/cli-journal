package com.strangegrotto.clijournal.commands.verbs;

import com.google.common.collect.Sets;
import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.commands.ResultReferenceTranslator;
import com.strangegrotto.clijournal.entrystore.EntryStore;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class NewEntryCommand extends AbstractCommand {
    private static final String ENTRY_PSEUDO_FILENAME_ARG = "entry_filename";
    private static final String TAGS_ARG = "tag";

    private final EntryStore entryStore;
    private final ResultReferenceTranslator referenceTranslator;

    public NewEntryCommand(EntryStore entryStore, ResultReferenceTranslator referenceTranslator) {
        super("new", "Adds a new journal entry with the given parameters");
        this.entryStore = entryStore;
        this.referenceTranslator = referenceTranslator;
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {
        argParser.addArgument(ENTRY_PSEUDO_FILENAME_ARG)
                .help("Entry name, in form 'name.ext'");
        argParser.addArgument(TAGS_ARG)
                .nargs("*")
                .setDefault(new ArrayList<String>())
                .help("Tags to give to the new entry");
    }

    @Override
    public CommandResultMetadata runCommandLogic(Namespace parsedArgs) {
        String pseudoFilename = parsedArgs.getString(ENTRY_PSEUDO_FILENAME_ARG);
        List<String> tagTokens = parsedArgs.getList(TAGS_ARG);
        List<String> dereferencedTagTokens;
        try {
            dereferencedTagTokens = this.referenceTranslator.dereferenceTokens(
                    ListingCmdResultType.TAG,
                    tagTokens
            );
        } catch (ResultReferenceTranslator.ResultDereferenceException e) {
            System.out.println(e.getMessage());
            return CommandResultMetadata.empty();
        }

        Path filepath = this.entryStore.getNewEntryFilepath(
                pseudoFilename,
                LocalDateTime.now(),
                Sets.newHashSet(dereferencedTagTokens)
        );
        return CommandResultMetadata.of(
                Optional.of(Arrays.asList("vim", filepath.toString())),
                Optional.empty()
        );
    }
}
