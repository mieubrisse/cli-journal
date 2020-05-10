package com.strangegrotto.clijournal.commands.verbs;

import com.google.common.collect.Ordering;
import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import com.strangegrotto.clijournal.commands.ListingCmdResultType;
import com.strangegrotto.clijournal.commands.ListingCmdResults;
import com.strangegrotto.clijournal.entrystore.Entry;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractListingCommand<T> extends AbstractCommand {
    private static final int LIST_INDEX_PAD_WIDTH = 6;

    public AbstractListingCommand(String alias, String helpStr) {
        super(alias, helpStr);
    }

    @Override
    protected void configureParser(ArgumentParser argParser) {
    }

    @Override
    public final CommandResultMetadata runCommandLogic(Namespace parsedArgs) {
        List<T> results = this.getResults(parsedArgs);
        if (results.size() > 0) {
            for (int i = 0; i < results.size(); i++) {
                T result = results.get(i);
                String out = String.format(
                        "%-" + LIST_INDEX_PAD_WIDTH + "s%s",
                        i,
                        this.renderResult(result, parsedArgs)
                );
                System.out.println(out);
            }
        } else {
            System.out.println("No results");
        }
        List<String> resultRefValues = results.stream()
                .map(this::getResultReferenceValue)
                .collect(Collectors.toList());
        ListingCmdResultType resultType = this.getResultType();
        ListingCmdResults cmdResults = new ListingCmdResults(resultRefValues, resultType);
        return CommandResultMetadata.of(
                Optional.empty(),
                Optional.of(cmdResults)
        );
    }

    /**
     * @return The type of result that this command produces
     */
    protected abstract ListingCmdResultType getResultType();

    /**
     * Hook to get the results that this command will display, sorted in the manner that they
     * should be displayed.
     * @param parsedArgs Remaining args
     * @return
     */
    protected abstract List<T> getResults(Namespace parsedArgs);

    /**
     * Translates the result type expected by the command to a string that is the value which will be used
     * for that result when it's referenced in the future.
     * @param result Command result object
     * @return String value which will be the value used when this result is referenced in the future
     */
    protected abstract String getResultReferenceValue(T result);

    /**
     * Renders a given result, with the resulting string being what's displayed in the command output
     * @param result Command result to render
     * @return Pretty version of the result
     */
    protected abstract String renderResult(T result, Namespace parsedArgs);
}
