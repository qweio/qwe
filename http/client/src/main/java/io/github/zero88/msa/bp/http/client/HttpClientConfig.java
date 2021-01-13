package io.github.zero88.msa.bp.http.client;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.github.zero88.msa.bp.BlueprintConfig.AppConfig;
import io.github.zero88.msa.bp.IConfig;
import io.github.zero88.msa.bp.dto.JsonData;
import io.github.zero88.msa.bp.http.HostInfo;
import io.github.zero88.msa.bp.http.client.handler.HttpErrorHandler;
import io.github.zero88.msa.bp.http.client.handler.HttpRequestMessageComposer;
import io.github.zero88.msa.bp.http.client.handler.HttpResponseBinaryHandler;
import io.github.zero88.msa.bp.http.client.handler.HttpResponseTextBodyHandler;
import io.github.zero88.msa.bp.http.client.handler.WebSocketConnectErrorHandler;
import io.github.zero88.msa.bp.http.client.handler.WebSocketResponseDispatcher;
import io.github.zero88.msa.bp.http.client.handler.WebSocketResponseErrorHandler;
import io.github.zero88.utils.Reflections.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpClientConfig implements IConfig {

    public static final int CONNECT_TIMEOUT_SECOND = 45;
    public static final int HTTP_IDLE_TIMEOUT_SECOND = 15;
    public static final int WS_IDLE_TIMEOUT_SECOND = 1200;
    private String userAgent = "zbp.httpclient";
    private HostInfo hostInfo;
    private HttpClientOptions options;
    private HandlerConfig handlerConfig = new HandlerConfig();

    HttpClientConfig() {
        this(new HttpClientOptions().setIdleTimeout(HTTP_IDLE_TIMEOUT_SECOND)
                                    .setIdleTimeoutUnit(TimeUnit.SECONDS)
                                    .setConnectTimeout(CONNECT_TIMEOUT_SECOND * 1000)
                                    .setTryUseCompression(true)
                                    .setWebSocketCompressionLevel(6)
                                    .setWebSocketCompressionAllowClientNoContext(true)
                                    .setWebSocketCompressionRequestServerNoContext(true)
                                    .setTryUsePerFrameWebSocketCompression(false)
                                    .setTryUsePerMessageWebSocketCompression(true));
    }

    HttpClientConfig(@NonNull HttpClientOptions options) {
        this.options = options;
    }

    public static HttpClientConfig create(String userAgent, @NonNull HostInfo info) {
        final HttpClientConfig config = new HttpClientConfig(new HttpClientOptions());
        config.hostInfo = info;
        config.userAgent = Strings.isBlank(userAgent) ? config.userAgent : userAgent;
        return config;
    }

    @JsonCreator
    static HttpClientConfig create(@JsonProperty("userAgent") String userAgent,
                                   @JsonProperty("options") JsonObject options,
                                   @JsonProperty("handlerConfig") JsonObject handlerConfig) {
        return new HttpClientConfig(userAgent, null, new HttpClientOptions(options),
                                    JsonData.convert(handlerConfig, HandlerConfig.class));
    }

    @Override
    public String key() { return "__httpClient__"; }

    @Override
    public Class<? extends IConfig> parent() { return AppConfig.class; }

    public HostInfo getHostInfo() {
        if (Objects.nonNull(hostInfo)) {
            return hostInfo;
        }
        return initHostInfo();
    }

    private synchronized HostInfo initHostInfo() {
        hostInfo = HostInfo.builder()
                           .host(this.getOptions().getDefaultHost())
                           .port(this.getOptions().getDefaultPort())
                           .ssl(this.getOptions().isSsl())
                           .build();
        return hostInfo;
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject().put("options", options.toJson())
                               .put("handlerConfig", JsonData.tryParse(this.handlerConfig).toJson())
                               .put("userAgent", this.userAgent);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class HandlerConfig {

        private Class<? extends HttpRequestMessageComposer> requestComposerCls = HttpRequestMessageComposer.class;
        private Class<? extends HttpResponseTextBodyHandler> responseTextHandlerCls = HttpResponseTextBodyHandler.class;
        private Class<? extends HttpResponseBinaryHandler> responseBinaryHandlerCls = HttpResponseBinaryHandler.class;
        private Class<? extends HttpErrorHandler> httpErrorHandlerCls = HttpErrorHandler.class;
        private Class<? extends WebSocketConnectErrorHandler> webSocketConnectErrorHandlerCls
            = WebSocketConnectErrorHandler.class;
        private Class<? extends WebSocketResponseErrorHandler> webSocketErrorHandlerCls
            = WebSocketResponseErrorHandler.class;
        private Class<? extends WebSocketResponseDispatcher> webSocketResponseDispatcherCls
            = WebSocketResponseDispatcher.class;

        @JsonCreator
        HandlerConfig(@JsonProperty("requestComposerCls") String requestComposerCls,
                      @JsonProperty("responseTextHandlerCls") String responseTextHandlerCls,
                      @JsonProperty("responseBinaryHandlerCls") String responseBinaryHandlerCls,
                      @JsonProperty("httpErrorHandlerCls") String httpErrorHandlerCls,
                      @JsonProperty("webSocketConnectErrorHandlerCls") String webSocketConnectErrorHandlerCls,
                      @JsonProperty("webSocketErrorHandlerCls") String webSocketErrorHandlerCls,
                      @JsonProperty("webSocketResponseDispatcherCls") String webSocketResponseDispatcherCls) {
            this.requestComposerCls = Strings.isBlank(requestComposerCls)
                                      ? HttpRequestMessageComposer.class
                                      : ReflectionClass.findClass(requestComposerCls);
            this.responseTextHandlerCls = Strings.isBlank(responseTextHandlerCls)
                                          ? HttpResponseTextBodyHandler.class
                                          : ReflectionClass.findClass(responseTextHandlerCls);
            this.responseBinaryHandlerCls = Strings.isBlank(responseBinaryHandlerCls)
                                            ? HttpResponseBinaryHandler.class
                                            : ReflectionClass.findClass(responseBinaryHandlerCls);
            this.httpErrorHandlerCls = Strings.isBlank(httpErrorHandlerCls)
                                       ? HttpErrorHandler.class
                                       : ReflectionClass.findClass(httpErrorHandlerCls);
            this.webSocketConnectErrorHandlerCls = Strings.isBlank(webSocketConnectErrorHandlerCls)
                                                   ? WebSocketConnectErrorHandler.class
                                                   : ReflectionClass.findClass(webSocketConnectErrorHandlerCls);
            this.webSocketErrorHandlerCls = Strings.isBlank(webSocketErrorHandlerCls)
                                            ? WebSocketResponseErrorHandler.class
                                            : ReflectionClass.findClass(webSocketErrorHandlerCls);
            this.webSocketResponseDispatcherCls = Strings.isBlank(webSocketResponseDispatcherCls)
                                                  ? WebSocketResponseDispatcher.class
                                                  : ReflectionClass.findClass(webSocketResponseDispatcherCls);
        }

    }

}
