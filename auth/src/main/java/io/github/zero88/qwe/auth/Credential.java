package io.github.zero88.qwe.auth;

import java.util.Collection;
import java.util.HashSet;

import io.github.zero88.qwe.dto.JsonData;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

public interface Credential extends JsonData {

    @JsonUnwrapped
    CredentialType getType();

    String getUser();

    String getHeaderAuthType();

    @Getter
    @SuperBuilder
    abstract class AbstractCredential implements Credential {

        protected final String user;
        protected CredentialType type;

        protected abstract Collection<String> sensitiveFields();

        @Override
        public String toString() {
            return "User: " + user + "::Type: " + getType();
        }

        @Override
        public JsonObject toJson(@NonNull ObjectMapper mapper) {
            return toJson(mapper, new HashSet<>(sensitiveFields()));
        }

        @JsonProperty("type")
        protected AbstractCredential type(String type) {
            this.type = CredentialType.factory(type);
            return this;
        }

    }

}
