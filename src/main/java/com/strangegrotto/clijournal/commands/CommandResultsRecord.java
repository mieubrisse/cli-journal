package com.strangegrotto.clijournal.commands;

import java.util.Optional;

/**
 * Class designed to record information about historical command output for later retrieval
 */
public class CommandResultsRecord {
    private Optional<ListingCmdResults> lastResults;

    public CommandResultsRecord() {
        this.lastResults = Optional.empty();
    }

    public void observeResult(CommandResultMetadata result) {
        Optional<ListingCmdResults> listingCmdResultsOpt = result.getListingCmdResults();
        this.lastResults = listingCmdResultsOpt.isPresent() ? listingCmdResultsOpt : this.lastResults;
    }

    public Optional<ListingCmdResults> getLastListingCmdResults() {
        return this.lastResults;
    }
}
