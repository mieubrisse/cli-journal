package com.strangegrotto.clijournal.commands;

import com.google.common.base.Preconditions;
import com.strangegrotto.clijournal.commands.verbs.Command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {
    private static final int HELP_ALIAS_PAD_WIDTH = 25;

    private final CommandResultsRecord resultsRecord;
    private final Map<String, Command> aliases;

    private class HelpCommand implements Command {

        @Override
        public String getAlias() {
            return "help";
        }

        @Override
        public CommandResultMetadata execute(List<String> args) {
            System.out.println();
            for (Map.Entry<String, Command> entry : CommandParser.this.aliases.entrySet()) {
                String outStr = String.format(
                        "%" + HELP_ALIAS_PAD_WIDTH + "s%s",
                        entry.getKey(),
                        entry.getValue().getHelpString()
                );
                System.out.println(outStr);
            }
            return CommandResultMetadata.empty();
        }

        @Override
        public String getHelpString() {
            return "Prints help data for all commands";
        }
    }

    public CommandParser(CommandResultsRecord resultsRecord) {
        this.resultsRecord = resultsRecord;
        this.aliases = new HashMap<>();
        this.registerCommand(new HelpCommand());
    }

    public CommandParser registerCommand(Command command) {
        String cleanedAlias = command.getAlias().trim().toLowerCase();
        Preconditions.checkState(
                !this.aliases.containsKey(cleanedAlias),
                "Alias '%s' is already registered",
                cleanedAlias
        );
        this.aliases.put(cleanedAlias, command);
        return this;
    }

    public CommandResultMetadata parse(List<String> tokenizedInput) {
        if (tokenizedInput.size() == 0) {
            return CommandResultMetadata.empty();
        }
        String alias = tokenizedInput.get(0).toLowerCase();
        List<String> remainingTokens = tokenizedInput.subList(1, tokenizedInput.size());

        CommandResultMetadata result;
        if (this.aliases.containsKey(alias)) {
            result = this.aliases.get(alias).execute(remainingTokens);
        } else {
            String out = String.format(
                    "Unknown command '%s'",
                    alias
            );
            System.out.println(out);
            result = CommandResultMetadata.empty();
        }
        this.resultsRecord.observeResult(result);
        return result;
    }
}
