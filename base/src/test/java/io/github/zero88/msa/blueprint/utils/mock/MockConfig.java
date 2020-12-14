package io.github.zero88.msa.blueprint.utils.mock;

import io.github.zero88.msa.blueprint.BlueprintConfig.AppConfig;
import io.github.zero88.msa.blueprint.IConfig;

import lombok.Getter;
import lombok.Setter;

public class MockConfig implements IConfig {

    @Setter
    @Getter
    private String name;

    @Override
    public String key() {
        return "mock";
    }

    @Override
    public Class<? extends IConfig> parent() {
        return AppConfig.class;
    }

}
