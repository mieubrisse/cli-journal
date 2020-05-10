package com.strangegrotto.clijournal.commands;

import java.util.*;

/**
 * Base class for classes that will consume the output of previous commands
 */
public class ResultReferenceTranslator {
    public static class ResultDereferenceException extends Exception {
        public ResultDereferenceException(String message) {
            super(message);
        }
    }

    private static final char REFERENCE_LEADER = '@';
    private static final String DISJUNCTION_SEPARATOR = ",";
    private final CommandResultsRecord resultsRecord;

    public ResultReferenceTranslator(CommandResultsRecord resultsRecord) {
        this.resultsRecord = resultsRecord;
    }

    /**
     * @param expectedResultType Expected type of the last that's being referenced (will throw an error if the types don't match)
     * @param tokens User-inputted tokens to dereference
     * @return New (potentially-larger!) list of tokens, with references translated into the reference values of the
     *  last listing command output
     */
    public List<String> dereferenceTokens(
            ListingCmdResultType expectedResultType,
            List<String> tokens) throws ResultDereferenceException {
        performDereferenceSanityChecks(expectedResultType, tokens);

        List<String> dereferencedTokens = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            List<String> singleTokenDereferences = this.dereferenceToken(token);
            dereferencedTokens.addAll(singleTokenDereferences);
        }
        return dereferencedTokens;
    }

    private void performDereferenceSanityChecks(
            ListingCmdResultType expectedResultType,
            List<String> tokens) throws ResultDereferenceException {
        Optional<ListingCmdResults> lastResultsOpt = this.resultsRecord.getLastListingCmdResults();

        boolean hasReferences = false;
        for (String token : tokens) {
            if (token.charAt(0) == REFERENCE_LEADER) {
                hasReferences = true;
                break;
            }
        }

        if (hasReferences) {
            if (!lastResultsOpt.isPresent()) {
                throw new ResultDereferenceException("No previous command results to reference");
            }
            ListingCmdResults lastResults = lastResultsOpt.get();

            ListingCmdResultType lastResultType = lastResults.getResultType();
            if (lastResultType != expectedResultType) {
                throw new ResultDereferenceException(
                        String.format(
                                "Expected result type '%s' but the last command's result type was '%s'",
                                expectedResultType,
                                lastResultType
                        )
                );
            }
        }
    }

    private List<String> dereferenceToken(String token) throws ResultDereferenceException {
        if (token.charAt(0) != REFERENCE_LEADER) {
            return List.of(token);
        }

        String minusLeader = token.substring(1);
        String[] references = minusLeader.split(DISJUNCTION_SEPARATOR);
        List<Integer> refsToPull = new ArrayList<>(references.length);
        for (int referenceIdx = 0; referenceIdx < references.length; referenceIdx++) {
            String referenceStr = references[referenceIdx];
            try {
                refsToPull.add(Integer.parseInt(referenceStr));
            } catch (NumberFormatException e) {
                throw getExceptionForToken(token, "Reference '" + referenceStr + "' could not be parsed to integer");
            }
        }

        ListingCmdResults lastCmdResults = this.resultsRecord.getLastListingCmdResults().get();
        List<String> lastResultRefValues = lastCmdResults.getResultRefValues();
        List<String> dereferencedValues = new ArrayList<>(refsToPull.size());
        for (Integer reference : refsToPull) {
            int unboxedRef = reference.intValue();
            if (unboxedRef < 0 || unboxedRef >= lastResultRefValues.size()) {
                throw getExceptionForToken(token, "Reference index out-of-range with last results");
            }
            dereferencedValues.add(lastResultRefValues.get(reference));
        }
        return dereferencedValues;
    }

    private static ResultDereferenceException getExceptionForToken(String token, String message) {
        String exceptionMessage = String.format(
                "Reference error with '%s': %s",
                token,
                message
        );
        return new ResultDereferenceException(exceptionMessage);

    }
}

