package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.List;

public abstract class AbstractCommand implements Command {
    private final String alias;
    private final String helpStr;
    private final ArgumentParser argParser;

    protected AbstractCommand(String alias, String helpStr) {
        this.alias = alias;
        this.helpStr = helpStr;
        this.argParser = ArgumentParsers.newFor(this.alias).build()
                .defaultHelp(true)
                .description(helpStr);
        this.configureParser(argParser);
    }

    @Override
    public final CommandResultMetadata execute(List<String> args) {
        return null;
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
