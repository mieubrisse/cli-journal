package com.strangegrotto.clijournal.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigV1 implements Config {
    private final Map<String, String> contextDirpaths;
    private final String defaultContext;

    @JsonCreator
    public ConfigV1(
            @JsonProperty(value = "contexts", required = true) Map<String, String> contextDirpaths,
            @JsonProperty(value = "default-context", required = true) String defaultContext) {
        this.contextDirpaths = contextDirpaths;
        this.defaultContext = defaultContext;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    public Map<String, String> getContextDirpaths() {
        return contextDirpaths;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    @Override
    public void validate() throws InvalidConfigException {
        Map<String, String> contextDirpaths = getContextDirpaths();
        if (contextDirpaths.size() == 0) {
            throw new InvalidConfigException("No contexts were defined");
        }

        String defaultContext = getDefaultContext();
        if (!contextDirpaths.containsKey(defaultContext)) {
            throw new InvalidConfigException("The default context '"
                    + defaultContext + "' doesn't match any known context");
        }

        List<String> contextDirpathsNeedingFixing = validateContextDirpaths(contextDirpaths);
        if (contextDirpathsNeedingFixing.size() > 0) {
            String brokenContextsStr = String.join(", ", contextDirpathsNeedingFixing);
            throw new InvalidConfigException(
                "The following contexts are not valid directories or could not be opened: " + brokenContextsStr
            );
        }
    }

    private static List<String> validateContextDirpaths(Map<String, String> contextDirpaths) {
        List<String> contextDirpathsNeedingFixing = new ArrayList<>();
        for (Map.Entry<String, String> entry : contextDirpaths.entrySet()) {
            String context = entry.getKey();
            String dirpathStr = entry.getValue();
            Path dirpath = Paths.get(dirpathStr);
            if (!Files.isDirectory(dirpath)) {
                System.out.println(String.format(
                        "Configured path '%s' for context '%s' is not a directory",
                        dirpath,
                        context
                ));
                contextDirpathsNeedingFixing.add(context);
            }

            try {
                Files.list(dirpath);
            } catch (IOException e) {
                System.out.println(String.format(
                        "Error reading path '%s' for context '%s'",
                        dirpath,
                        context
                ));
            }
        }
        return contextDirpathsNeedingFixing;
    }
}
