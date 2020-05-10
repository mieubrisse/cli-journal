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

        // For convenience, we don't record results that didn't return anything
        if (listingCmdResultsOpt.isPresent() && listingCmdResultsOpt.get().getResultRefValues().size() > 0) {
            this.lastResults = listingCmdResultsOpt;
        }
    }

    public Optional<ListingCmdResults> getLastListingCmdResults() {
        return this.lastResults;
    }
}
