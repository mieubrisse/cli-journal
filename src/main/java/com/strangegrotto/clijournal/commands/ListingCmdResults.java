package com.strangegrotto.clijournal.commands;

import java.util.List;

/**
 * POJO encapsulating information about the output of a command that lists results, so that those results can be
 * referenced by future commands.
 */
public class ListingCmdResults {
    private final List<String> resultRefValues;
    private final ListingCmdResultType resultType;

    /**
     * @param resultRefValues Values that will be substituted if this command is referenced in the future
     * @param resultType Type of result values
     */
    public ListingCmdResults(List<String> resultRefValues, ListingCmdResultType resultType) {
        this.resultRefValues = resultRefValues;
        this.resultType = resultType;
    }

    public List<String> getResultRefValues() {
        return resultRefValues;
    }

    public ListingCmdResultType getResultType() {
        return resultType;
    }
}
