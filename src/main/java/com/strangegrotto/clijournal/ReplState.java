package com.strangegrotto.clijournal;

public class ReplState {
    private String context;

    public ReplState(String initContext) {
        this.context = initContext;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
