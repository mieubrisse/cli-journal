package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;

public abstract class AbstractCommand implements Command {
    private final String alias;
    private final String helpStr;
    private final ArgumentParser argParser;

    protected AbstractCommand(String alias, String helpStr) {
        this.alias = alias;
        this.helpStr = helpStr;
        this.argParser = ArgumentParsers.newFor(this.alias).terminalWidthDetection(false).build()
                .defaultHelp(true)
                .description(helpStr);
        this.configureParser(argParser);
    }

    @Override
    public final CommandResultMetadata execute(List<String> args) {
        Namespace parsedArgs;
        try {
            parsedArgs = this.argParser.parseArgs(args.toArray(new String[0]));
        } catch (ArgumentParserException e) {
            argParser.handleError(e);
            // For some reason, we get weird ordering of output lines if this isn't flushed right here
            System.out.flush();
            return CommandResultMetadata.empty();
        }
        return this.runCommandLogic(parsedArgs);
    }

    @Override
    public final String getAlias() {
        return this.alias;
    }

    @Override
    public final String getHelpString() {
        return this.helpStr;
    }

    /**
     * Hook to allow subclasses to add custom arguments, called on construction
     * @param argParser Argument parser to configure
     */
    protected abstract void configureParser(ArgumentParser argParser);

    public abstract CommandResultMetadata runCommandLogic(Namespace parsedArgs);
}
