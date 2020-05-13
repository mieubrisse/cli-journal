package com.strangegrotto.clijournal.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.strangegrotto.clijournal.config.Config;
import com.strangegrotto.clijournal.config.ConfigV1;
import com.strangegrotto.clijournal.config.InvalidConfigException;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to bootstrap the config, presenting the user with nice workflows to initialize the config if this is their first
 *  time using the tool
 */
public class ConfigLoader {
    public static class NoSuchContextException extends Exception {
        public NoSuchContextException(String message) {
            super(message);
        }
    }

    private static final Path CONFIG_FILEPATH = Paths.get(
            System.getProperty("user.home"),
            ".clijournal.yml"
    ).toAbsolutePath();
    private static final String DEFAULT_CONTEXT_NAME = "default";

    private final BufferedReader reader;
    private final ObjectMapper objectMapper;
    private Optional<ConfigV1> configOpt;

    /**
     *
     * @param reader
     * @throws IOException if the config file can't be read or written
     * @throws InvalidConfigException if the config was parseable, but had a validation error (meaning we need user action,
     *   because rewriting the entire config would likely cause user data loss)
     */
    public ConfigLoader(BufferedReader reader) {
        this.reader = reader;
        this.objectMapper = new ObjectMapper(new YAMLFactory());
        this.configOpt = Optional.empty();
    }

    public ConfigV1 loadConfig() throws IOException, InvalidConfigException {
        if (this.configOpt.isPresent()) {
            return this.configOpt.get();
        } else {
            ConfigV1 config = loadValidConfig(this.reader, this.objectMapper);
            this.configOpt = Optional.of(config);
            return config;
        }
    }

    /**
     * Laods the config object from the config, or runs the user through the onboarding workflow to create one if no
     *  valid config exists.
     * @return Config file, deserialized from disk, that's guaranteed to be valid - all directories exist, etc.
     * @throws IOException if an unrecoverable error occurred when reading or writing the config file
     * @throws InvalidConfigException if the config was parseable but otherwise invalid, requiring the user to fix it
     */
    private static ConfigV1 loadValidConfig(BufferedReader reader, ObjectMapper mapper) throws IOException, InvalidConfigException {
        ConfigV1 result = null;
        while (null == result) {
            if (!Files.isRegularFile(CONFIG_FILEPATH)) {
                System.out.println("No config file detected; running onboarding...");
                runOnboardingWorkflow(reader, mapper);
                continue;
            }

            Config configInterface;
            try {
                configInterface = mapper.readValue(CONFIG_FILEPATH.toFile(), Config.class);
            } catch (JsonParseException | JsonMappingException e) {
                System.out.println("Config file found, but couldn't be parsed due to the following error:");
                System.out.println(e.getMessage());
                System.out.println("Running config init flow...");
                runFullJournalConfigWorkflow(reader, mapper);
                continue;
            }
            // TODO logic for upgrading old versions of the config goes here
            ConfigV1 config = (ConfigV1)configInterface;
            config.validate();
        }

        return result;
    }

    /**
     * Gives the user a little introduction and has them create the dotfile that points to the journal location
     * @param reader Reader to use when reading user input
     * @return
     * @throws IOException if the config filepath couldn't be written during the onboarding workflow (a fatal error)
     */
    private static void runOnboardingWorkflow(BufferedReader reader, ObjectMapper mapper) throws IOException {
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

        runFullJournalConfigWorkflow(reader, mapper);

        System.out.println();
        System.out.println("Successfully configured! From here on, running this CLI will always open your journal.");
        System.out.println();
        System.out.println("To get started, try running 'help' to see the available commands.");
        System.out.println("TIP: All commands have a '--help' flag that prints detailed usage information!");
        System.out.println();
    }

    /**
     * Runs the user through the workflow of configuring their entire config file
     * @param reader Reader to use when reading user input during prompts
     * @throws IOException if an unrecoverable error occurs when writing the config file.
     */

    private static void runFullJournalConfigWorkflow(BufferedReader reader, ObjectMapper mapper) throws IOException {
        Path journalDirpath = runJournalDirpathPromptLoop(reader);

        Map<String, String> contextDirpaths = new HashMap<>();
        contextDirpaths.put(DEFAULT_CONTEXT_NAME, journalDirpath.toString());
        ConfigV1 config = new ConfigV1(contextDirpaths, DEFAULT_CONTEXT_NAME);

        mapper.writeValue(CONFIG_FILEPATH.toFile(), config);
    }

    private static Path runJournalDirpathPromptLoop(BufferedReader reader) {
        System.out.println("PLease input the *absolute path* to an existing directory.");
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
