package io.zero88.qwe;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.zero88.qwe.exceptions.ConfigException;

class QWEAppConfigTest {

    @Test
    public void test_deserialize_appCfg_directly() {
        String jsonStr = "{\"__system__\":{},\"__app__\":{\"http.port\":8085}}";
        QWEAppConfig cfg = IConfig.from(jsonStr, QWEAppConfig.class);
        Assertions.assertNotNull(cfg);
        Assertions.assertEquals(1, cfg.other().size());
        Assertions.assertEquals(8085, cfg.lookup("http.port"));
    }

    @Test
    public void test_deserialize_appCfg_invalid_json() {
        TestHelper.assertThrows(() -> IConfig.from("{\"__system__\":{},\"__app__\":8085}}", QWEAppConfig.class),
                                ConfigException.class, DecodeException.class);
    }

    @Test
    public void test_deserialize_appCfg_limitation() {
        QWEAppConfig from = IConfig.from("{\"__system__\":{}}", QWEAppConfig.class);
        Assertions.assertNotNull(from);
    }

    @Test
    public void test_merge_with_empty_json() throws JSONException {
        QWEAppConfig appConfig = IConfig.merge("{\"__app__\":{\"test\":\"1\"}}", "{\"__app__\":{}}",
                                               QWEAppConfig.class);
        JsonHelper.assertJson(new JsonObject("{\"test\":\"1\"}"), appConfig.other());
        JsonHelper.assertJson(new DeliveryOptions().toJson(), appConfig.getDeliveryOptions().toJson());
    }

    @Test
    public void test_merge_app_config() throws JSONException {
        String oldApp = "{\"__kafka__\":{\"__client__\":{\"bootstrap.servers\":[\"localhost:9092\"]}}," +
                        "\"__sql__\":{\"dialect\":\"H2\"}, \"test\":123}";
        String newApp = "{\"__kafka__\":{\"__client__\":{\"bootstrap.servers\":[\"localhost:9094\"]}}}";
        final QWEAppConfig merge = IConfig.merge(oldApp, newApp, QWEAppConfig.class);
        final JsonObject expected = new JsonObject(
            "{\"__kafka__\":{\"__client__\":{\"bootstrap.servers\":[\"localhost:9094\"]}}," +
            "\"__sql__\":{\"dialect\":\"H2\"}, \"test\":123}");
        System.out.println(merge.toJson());
        JsonHelper.assertJson(expected, merge.other());
    }

    @Test
    public void test_merge_with_blank_value() throws JSONException {
        JsonHelper.assertJson(new JsonObject("{\"test\":\"\"}"),
                              IConfig.merge("{\"__app__\":{\"test\":\"1\"}}", "{\"__app__\":{\"test\":\"\"}}",
                                            QWEAppConfig.class).other());
    }

    @Test
    public void test_merge_with_null_value() throws JSONException {
        JsonHelper.assertJson(new JsonObject("{\"test\":\"1\"}"),
                              IConfig.merge("{\"__app__\":{\"test\":\"1\"}}", "{\"__app__\":{\"test\":null}}",
                                            QWEAppConfig.class).other());
    }

}
