package com.strangegrotto.clijournal.commands;

import java.util.List;
import java.util.Optional;

/**
 * Object which each command must return that contains metadata about how the CLI should proceed after the command
 */
public class CommandResultMetadata {
    private final Optional<List<String>> endArgs;
    private final Optional<ListingCmdResults> listingCmdResults;

    private CommandResultMetadata(Optional<List<String>> endArgs, Optional<ListingCmdResults> listingCmdResults) {
        this.endArgs = endArgs;
        this.listingCmdResults = listingCmdResults;
    }

    public static CommandResultMetadata empty() {
        return new CommandResultMetadata(
                Optional.empty(),
                Optional.empty()
        );
    }

    public static CommandResultMetadata of(
            Optional<List<String>> endArgs,
            Optional<ListingCmdResults> listingCmdResults) {
        return new CommandResultMetadata(endArgs, listingCmdResults);
    }

    public Optional<List<String>> getEndArgs() {
        return endArgs;
    }

    public Optional<ListingCmdResults> getListingCmdResults() {
        return listingCmdResults;
    }
}
