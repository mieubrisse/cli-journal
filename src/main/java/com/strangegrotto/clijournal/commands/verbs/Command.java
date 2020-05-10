package com.strangegrotto.clijournal.commands.verbs;

import com.strangegrotto.clijournal.commands.CommandResultMetadata;

import java.util.List;

public interface Command {
    String getAlias();

    CommandResultMetadata execute(List<String> args);

    String getHelpString();
}
