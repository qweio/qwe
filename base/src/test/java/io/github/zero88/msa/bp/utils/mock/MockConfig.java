package io.github.zero88.msa.bp.utils.mock;

import io.github.zero88.msa.bp.BlueprintConfig.AppConfig;
import io.github.zero88.msa.bp.IConfig;

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