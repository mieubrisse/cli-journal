/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.strangegrotto.clijournal;

import com.google.common.collect.Sets;
import com.strangegrotto.clijournal.commands.CommandParser;
import com.strangegrotto.clijournal.commands.CommandResultMetadata;
import com.strangegrotto.clijournal.commands.CommandResultsRecord;
import com.strangegrotto.clijournal.commands.ResultReferenceTranslator;
import com.strangegrotto.clijournal.commands.verbs.*;
import com.strangegrotto.clijournal.entrystore.EntryStore;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Main {
    private static final Path CONFIG_FILEPATH = Paths.get(
            System.getProperty("user.home"),
            ".clijournal"
    ).toAbsolutePath();
    private static final Set<String> BLACKLISTED_FILENAME_PATTERNS = Sets.newHashSet(
            ".*\\.swp",
            "^\\.git"
    );

    public static void main(String[] args) throws InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        Path journalDirpath = null;
        try {
            journalDirpath = getJournalDirpathFromConfig(reader);
        } catch (IOException e) {
            System.out.println("Fatal error reading config: " + e.getMessage());
            System.exit(1);
        }

        EntryStore entryStore = null;
        try {
            entryStore = new EntryStore(journalDirpath, BLACKLISTED_FILENAME_PATTERNS);
        } catch (IOException e) {
            System.out.println("Fatal error initializing entry store: " + e.getMessage());
            System.exit(1);
        }

        CommandResultsRecord resultsRecord = new CommandResultsRecord();
        ResultReferenceTranslator referenceTranslator = new ResultReferenceTranslator(resultsRecord);
        CommandParser commandParser = new CommandParser(resultsRecord).registerCommand(
                new ListEntriesCommand(entryStore)
        ).registerCommand(
                new ListTagsCommand(entryStore)
        ).registerCommand(
                new FindEntriesCommand(entryStore, referenceTranslator)
        ).registerCommand(
                new VimCommand(entryStore, referenceTranslator)
        ).registerCommand(
                new NewEntryCommand(entryStore, referenceTranslator)
        ).registerCommand(
                new ChromeCommand(entryStore, referenceTranslator)
        ).registerCommand(
                new QuitCommand()
        );

        List<String> endArgs = runInputLoop(reader, commandParser, args);
        if (endArgs.size() > 0) {
            Process process = null;
            try {
                process = new ProcessBuilder(endArgs).inheritIO().start();
            } catch (IOException e) {
                System.out.println("Fatal error starting subprocess: " + e.getMessage());
                System.exit(1);
            }
            process.waitFor();
        }
    }

    /**
     * Main input loop during the user's operation of the program
     * @param reader Reader to use when reading user's input
     * @param commandParser Command parser to parse the user's tokenized input
     * @param cliArgs Optional args that were passed into this CLI from the shell, and which should be run first
     * @return Potential list of arguments to end the CLI with
     * @throws IOException
     */
    private static List<String> runInputLoop(BufferedReader reader, CommandParser commandParser, String[] cliArgs) {
        boolean useCliArgs = cliArgs.length > 0;
        Optional<List<String>> endArgsOpt = Optional.empty();
        while (!endArgsOpt.isPresent()) {
            System.out.print("\n>> ");
            List<String> tokenizedInput;
            if (!useCliArgs) {
                String userInput;
                try {
                    userInput = reader.readLine();
                } catch (IOException e) {
                    System.out.println("An error occurred reading your input; please try again");
                    continue;
                }
                if (null == userInput) {
                    endArgsOpt = Optional.of(List.of());
                    break;
                }

                tokenizedInput = Arrays.asList(userInput.trim().split("\\s+"));
                if (tokenizedInput.size() == 0) {
                    continue;
                }
            } else {
                tokenizedInput = Arrays.asList(cliArgs);
                useCliArgs = false;
            }

            CommandResultMetadata cmdResult = commandParser.parse(tokenizedInput);

            endArgsOpt = cmdResult.getEndArgs();
        }
        return endArgsOpt.get();
    }


    // TODO Make this have a nice intro flow where the user can set the journal dirpath

    /**
     * Gets the user's journal dirpath if one is configured, and runs them through the onboarding flow if not
     * @param reader In case the user doesn't have a journal config set up, use this BufferedReader to read their
     *               input as we take them through the onboarding flow
     * @throws IOException if a unrecoverable error in reading or writing the config file occurred
     * @return
     */
    private static Path getJournalDirpathFromConfig(BufferedReader reader) throws IOException {
        Path journalDirpath = null;
        while (null == journalDirpath) {
            if (!Files.isRegularFile(CONFIG_FILEPATH)) {
                System.out.println("No config file detected; running onboarding...");
                runOnboardingWorkflow(reader);
                continue;
            }

            List<String> configLines;
            try {
                configLines = Files.readAllLines(CONFIG_FILEPATH);
            } catch (IOException e) {
                throw new IOException("Failed to read config filepath!");
            }

            if (configLines.size() == 0) {
                System.out.println("Config file found, but was empty; please enter the *absolute path* to a directory");
                System.out.println(" to use as your journal.");
                runJournalConfigurationWorkflow(reader);
                continue;
            }

            Path tentativeJournalDirpath = Paths.get(configLines.get(0));
            if (!Files.isDirectory(tentativeJournalDirpath)) {
                System.out.println("Path in config file is not a directory; please enter the *absolute path* to a directory");
                System.out.println(" to use as your journal.");
                runJournalConfigurationWorkflow(reader);
                continue;
            }
            journalDirpath = tentativeJournalDirpath;
        }
        return journalDirpath;
    }

    /**
     * Gives the user a little introduction and has them create the dotfile that points to the journal location
     * @param reader Reader to use when reading user input
     * @return
     * @throws IOException if the config filepath couldn't be written during the onboarding workflow (a fatal error)
     */
    private static void runOnboardingWorkflow(BufferedReader reader) throws IOException {
        System.out.println();
        System.out.println("    _____  _     _____     ___  _____ _   _______ _   _   ___   _     ");
        System.out.println("   /  __ \\| |   |_   _|   |_  ||  _  | | | | ___ \\ \\ | | / _ \\ | |    ");
        System.out.println("   | /  \\/| |     | |       | || | | | | | | |_/ /  \\| |/ /_\\ \\| |    ");
        System.out.println("   | |    | |     | |       | || | | | | | |    /| . ` ||  _  || |    ");
        System.out.println("   | \\__/\\| |_____| |_  /\\__/ /\\ \\_/ / |_| | |\\ \\| |\\  || | | || |____");
        System.out.println("    \\____/\\_____/\\___/  \\____/  \\___/ \\___/\\_| \\_\\_| \\_/\\_| |_/\\_____/");
        System.out.println();
        System.out.println("Welcome to CLI Journal by mieubrisse! To get you started, we'll need to set a directory");
        System.out.println(" that you'll use as your journal.");
        System.out.println();
        System.out.println("If this is your first time using cli-journal, please input the *absolute path* to an ");
        System.out.println(" existing, empty directory that will be your journal. For easy cross-machine syncing,");
        System.out.println(" I'd recommend this be in Google Drive or Dropbox.");
        System.out.println();
        System.out.println("If you already have a journal directory, please enter it below.");

        runJournalConfigurationWorkflow(reader);

        System.out.println();
        System.out.println("Successfully configured! From here on, running this CLI will always open your journal.");
        System.out.println();
        System.out.println("To get started, try running 'help' to see the available commands.");
        System.out.println("TIP: All commands have a '--help' flag that prints detailed usage information!");
        System.out.println();
    }

    /**
     * Runs the user through the workflow of configuring their config file pointing the CLI to their journal
     * @param reader Reader to use when reading user input during prompts
     * @throws IOException if the config file couldn't be written (a fatal error)
     */
    private static void runJournalConfigurationWorkflow(BufferedReader reader) throws IOException {
        Path journalDirpath = runJournalDirpathPromptLoop(reader);

        try (FileWriter writer = new FileWriter(CONFIG_FILEPATH.toFile())) {
            writer.write(journalDirpath.toString());
        } catch (IOException e) {
            throw new IOException("Couldn't write to config file");
        }
    }

    private static Path runJournalDirpathPromptLoop(BufferedReader reader) {
        Path chosenJournalDirpath = null;
        boolean journalDirpathChosen = false;
        while (!journalDirpathChosen) {
            System.out.println();
            System.out.print("Journal path: ");

            String userInput;
            try {
                userInput = reader.readLine();
            } catch (IOException e) {
                System.out.println("An error occurred reading your input; please try again");
                continue;
            }

            Path tentativeJournalDirpath = Paths.get(userInput);
            if (!Files.isDirectory(tentativeJournalDirpath)) {
                System.out.println("Not a directory; please input the *absolute path* to an existing directory");
                continue;
            }

            long numFiles;
            try {
                numFiles = Files.list(tentativeJournalDirpath).count();
            } catch (IOException e) {
                System.out.println("Error reading from directory; please ensure the directory is readable");
                continue;
            }

            if (numFiles > 0) {
                System.out.println("This directory contains " + numFiles + " files. If this is your first time using CLI Journal, I");
                System.out.println(" strongly recommend you start with an empty directory.");

                boolean selectedAffirmative = runYesNoPromptLoop("Continue anyway?", reader);
                if (!selectedAffirmative) {
                    continue;
                }
            }

            System.out.println("You're about to use '" + tentativeJournalDirpath + "' as your journal directory. You can");
            System.out.println(" always change this later by modifying the contents of '" + CONFIG_FILEPATH + "'.");

            boolean selectedFinalize = runYesNoPromptLoop("Continue?", reader);
            if (!selectedFinalize) {
                continue;
            }
            chosenJournalDirpath = tentativeJournalDirpath;
            journalDirpathChosen = true;
        }
        return chosenJournalDirpath;
    }

    private static boolean runYesNoPromptLoop(String promptText, BufferedReader reader) {
        String promptTextWithYesNo = promptText + " (y/n): ";

        boolean decisionMade = false;
        boolean selectedAffirmative = true;
        while (!decisionMade) {
            System.out.println();
            System.out.print(promptTextWithYesNo);

            String decisionInput;
            try {
                decisionInput = reader.readLine();
            } catch (IOException e) {
                System.out.println("An error occurred reading your input; please try again");
                continue;
            }

            switch(decisionInput) {
                case "y":
                    decisionMade = true;
                    selectedAffirmative = true;
                    break;
                case "n":
                    decisionMade = true;
                    selectedAffirmative = false;
                    break;
                default:
                    System.out.println("Unknown response; choose either 'y' or 'n'");
                    continue;
            }
        }
        return selectedAffirmative;
    }
}
