package io.zero88.qwe;

import io.vertx.core.Handler;

import lombok.Setter;

@Setter
public final class MockApplication extends ApplicationVerticle {

    private boolean errorOnStart;
    private boolean errorOnCompleted;
    private Handler<ContextLookup> onCompletedHandler;

    @Override
    public void onStart() {
        if (errorOnStart) {
            throw new RuntimeException("Error when starting");
        }
    }

    @Override
    public void onInstallCompleted(ContextLookup lookup) {
        if (errorOnCompleted) {
            throw new IllegalArgumentException("Error onInstallCompleted");
        }
        if (onCompletedHandler != null) {
            onCompletedHandler.handle(lookup);
        }
    }

    @Override
    public String configFile() {
        return "mock-container.json";
    }

}
