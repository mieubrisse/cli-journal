package com.strangegrotto.clijournal.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "version"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConfigV1.class, name = "1"),
})
public interface Config {
    int getVersion();

    void validate() throws InvalidConfigException;
}
