package io.zero88.qwe.http.server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import io.github.zero88.utils.Strings;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.zero88.qwe.IConfig;
import io.zero88.qwe.PluginProvider;
import io.zero88.qwe.PluginTestHelper.PluginDeployTest;
import io.zero88.qwe.TestHelper;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RunWith(VertxUnitRunner.class)
public abstract class HttpServerPluginTestBase implements PluginDeployTest<HttpServerPlugin>, HttpServerTestHelper {

    protected static final String DEFAULT_HOST = "127.0.0.1";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Getter
    protected Vertx vertx;
    protected HttpServerConfig httpConfig;
    @Getter
    protected HttpClient client;
    @Getter
    protected RequestOptions requestOptions;

    @BeforeClass
    public static void beforeSuite() { TestHelper.setup(); }

    @Override
    public Path testDir() {
        return folder.getRoot().toPath();
    }

    @Before
    public void before(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        client = vertx.createHttpClient(createClientOptions());
        requestOptions = new RequestOptions().setHost(DEFAULT_HOST).setPort(httpConfig.getPort());
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    protected String httpConfigFile() {
        return "httpServer.json";
    }

    protected HttpClientOptions createClientOptions() {
        return new HttpClientOptions().setConnectTimeout(TestHelper.TEST_TIMEOUT_SEC);
    }

    protected HttpServerPlugin startServer(TestContext context, HttpServerRouter httpRouter) {
        return deploy(vertx, context, httpConfig, new HttpServerPluginProvider(httpRouter));
    }

    protected void startServer(TestContext context, HttpServerRouter httpRouter, Consumer<Throwable> consumer) {
        deployFailed(vertx, context, httpConfig, new HttpServerPluginProvider(httpRouter), consumer);
    }

    protected JsonObject notFoundResponse(int port, String path) {
        return new JsonObject().put("message", "Resource not found")
                               .put("uri", Strings.format("http://{0}:{1}{2}", DEFAULT_HOST, port, path));
    }

    @Override
    public HttpServerConfig initConfig() {
        return httpConfig = IConfig.fromClasspath(httpConfigFile(), HttpServerConfig.class)
                                   .setHost(DEFAULT_HOST)
                                   .setPort(TestHelper.getRandomPort());
    }

    @Override
    public PluginProvider<HttpServerPlugin> initProvider() {
        throw new UnsupportedOperationException("Init plugin per test");
    }

}
