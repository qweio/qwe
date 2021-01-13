package io.github.zero88.msa.bp.micro;

import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;

import io.github.zero88.msa.bp.IConfig;
import io.github.zero88.msa.bp.TestHelper;
import io.github.zero88.msa.bp.TestHelper.EventbusHelper;
import io.github.zero88.msa.bp.TestHelper.JsonHelper;
import io.github.zero88.msa.bp.component.SharedDataDelegate;
import io.github.zero88.msa.bp.dto.msg.RequestData;
import io.github.zero88.msa.bp.event.DeliveryEvent;
import io.github.zero88.msa.bp.event.EventAction;
import io.github.zero88.msa.bp.event.EventbusClient;
import io.github.zero88.msa.bp.micro.ServiceGatewayIndex.Params;
import io.github.zero88.msa.bp.micro.metadata.EventMethodDefinition;
import io.github.zero88.msa.bp.micro.mock.MockEventbusService;
import io.github.zero88.msa.bp.micro.type.EventMessageService;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpLocation;

@RunWith(VertxUnitRunner.class)
public class MicroContextTest {

    private Vertx vertx;
    private MicroContext micro;

    @BeforeClass
    public static void init() {
        TestHelper.setup();
    }

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown() {
        if (Objects.nonNull(micro)) {
            micro.unregister(Promise.promise());
        }
        vertx.close();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_cluster() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getClusterController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_serviceDiscovery_local() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getLocalController()
                          .get();
    }

    @Test(expected = NullPointerException.class)
    public void test_not_enable_circuitBreaker() {
        new MicroContext().setup(vertx, IConfig.fromClasspath("micro.json", MicroConfig.class))
                          .getBreakerController()
                          .get();
    }

    @Test
    public void test_enable_serviceDiscovery_local_and_circuitBreaker() {
        MicroContext context = new MicroContext().setup(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        context.getLocalController().get();
        context.getBreakerController().get();
    }

    @Test
    public void test_serviceDiscovery_local_register_eventbus(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, IConfig.fromClasspath("local.json", MicroConfig.class));
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"address1\"},\"metadata\":{\"service" +
                                             ".interface\":\"io.github.zero88.msa.bp.micro.mock" +
                                             ".MockEventbusService\"}," + "\"name\":\"test\",\"status\":\"UP\"," +
                                             "\"type\":\"eventbus-service-proxy\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        EventbusClient controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addRecord(EventBusService.createRecord("test", "address1", MockEventbusService.class))
             .subscribe(record -> {
                 final JsonObject indexExpected = new JsonObject(
                     "{\"status\":\"SUCCESS\",\"action\":\"GET_LIST\",\"data\":{\"apis\":[{\"name\":\"test\"," +
                     "\"type\":\"eventbus-service-proxy\",\"status\":\"UP\",\"location\":\"address1\"}]}}");
                 final JsonObject payload = RequestData.builder()
                                                       .filter(
                                                           new JsonObject().put(Params.SCOPE, ServiceScope.INTERNAL))
                                                       .build()
                                                       .toJson();
                 controller.fire(DeliveryEvent.builder()
                                              .address(config.getGatewayConfig().getIndexAddress())
                                              .payload(payload)
                                              .action(EventAction.GET_LIST)
                                              .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
             });
    }

    @Test
    public void test_serviceDiscovery_local_register_http(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, config);
        JsonObject expected = new JsonObject("{\"location\":{\"endpoint\":\"http://123.456.0.1:1234/api\"," +
                                             "\"host\":\"123.456.0.1\",\"port\":1234,\"root\":\"/api\"," +
                                             "\"ssl\":false},\"metadata\":{\"meta\":\"test\"},\"name\":\"http.test\"," +
                                             "\"status\":\"UP\",\"type\":\"http-endpoint\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected));
        EventbusClient controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addHttpRecord("http.test", new HttpLocation().setHost("123.456.0.1").setPort(1234).setRoot("/api"),
                            new JsonObject().put("meta", "test"))
             .subscribe(record -> {
                 final JsonObject indexExpected = new JsonObject(
                     "{\"status\":\"SUCCESS\",\"action\":\"GET_LIST\",\"data\":{\"apis\":[{\"name\":\"http.test\"," +
                     "\"status\":\"UP\",\"type\":\"http-endpoint\",\"location\":\"http://123.456.0.1:1234/api\"}]}}");
                 controller.fire(DeliveryEvent.builder()
                                              .address(config.getGatewayConfig().getIndexAddress())
                                              .payload(RequestData.builder().build().toJson())
                                              .action(EventAction.GET_LIST)
                                              .build(), EventbusHelper.replyAsserter(context, async, indexExpected));
             });
    }

    @Test
    public void test_serviceDiscovery_local_register_eventMessage(TestContext context) {
        final Async async = context.async(2);
        final MicroConfig config = IConfig.fromClasspath("local.json", MicroConfig.class);
        micro = new MicroContext().setup(vertx, config);
        JsonObject expected = new JsonObject(
            "{\"location\":{\"endpoint\":\"address.1\"},\"metadata\":{\"eventMethods\":{\"servicePath\":\"/path\"," +
            "\"mapping\":[{\"action\":\"GET_LIST\",\"method\":\"GET\",\"capturePath\":\"/path\"," +
            "\"regexPath\":\"/path\"},{\"action\":\"UPDATE\",\"method\":\"PUT\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"CREATE\",\"method\":\"POST\",\"capturePath\":\"/path\"," +
            "\"regexPath\":\"/path\"},{\"action\":\"PATCH\",\"method\":\"PATCH\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"GET_ONE\",\"method\":\"GET\",\"capturePath\":\"/path/:param\"," +
            "\"regexPath\":\"/path/.+\"},{\"action\":\"REMOVE\",\"method\":\"DELETE\"," +
            "\"capturePath\":\"/path/:param\",\"regexPath\":\"/path/.+\"}],\"useRequestData\":true}}," +
            "\"name\":\"event-message\",\"status\":\"UP\",\"type\":\"eventbus-message-service\"}");
        EventbusHelper.assertReceivedData(vertx, async, micro.getLocalController().getConfig().getAnnounceAddress(),
                                          JsonHelper.asserter(context, async, expected, JSONCompareMode.LENIENT));
        EventbusClient controller = SharedDataDelegate.getEventController(vertx, MicroContext.class.getName());
        micro.getLocalController()
             .addRecord(EventMessageService.createRecord("event-message", "address.1",
                                                         EventMethodDefinition.createDefault("/path", "/:param")))
             .subscribe(record -> {
                 final JsonObject indexExpected = new JsonObject("{\"status\":\"SUCCESS\",\"action\":\"GET_LIST\"," +
                                                                 "\"data\":{\"apis\":[{\"name\":\"event-message\"," +
                                                                 "\"status\":\"UP\",\"location\":\"address.1\"," +
                                                                 "\"endpoints\":[{\"method\":\"GET\"," +
                                                                 "\"path\":\"/path\"},{\"method\":\"PATCH\"," +
                                                                 "\"path\":\"/path/:param\"},{\"method\":\"PUT\"," +
                                                                 "\"path\":\"/path/:param\"},{\"method\":\"POST\"," +
                                                                 "\"path\":\"/path\"},{\"method\":\"DELETE\"," +
                                                                 "\"path\":\"/path/:param\"},{\"method\":\"GET\"," +
                                                                 "\"path\":\"/path/:param\"}]}]}}");
                 controller.fire(DeliveryEvent.builder()
                                              .address(config.getGatewayConfig().getIndexAddress())
                                              .payload(RequestData.builder().build().toJson())
                                              .action(EventAction.GET_LIST)
                                              .build(),
                                 EventbusHelper.replyAsserter(context, async, indexExpected, JSONCompareMode.LENIENT));
             });
    }

}
