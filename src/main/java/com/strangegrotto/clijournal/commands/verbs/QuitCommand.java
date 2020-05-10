package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;
import java.util.Optional;

public class QuitCommand extends AbstractCommand {

    public QuitCommand() {
        super("quit", "Quits the CLI");
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {}

    @Override
    public CommandResultMetadata runCommandLogic(Namespace parsedArgs) {
        return CommandResultMetadata.of(
                Optional.of(List.of()),
                Optional.empty()
        );
    }
}
