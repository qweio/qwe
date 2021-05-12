package io.zero88.qwe;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.core.Vertx;
import io.zero88.qwe.exceptions.QWEException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(VertxUnitRunner.class)
public class ApplicationVerticleTest {

    private Vertx vertx;
    private MockApplication application;

    @BeforeClass
    public static void beforeSuite() {
        TestHelper.setup();
        ((Logger) LoggerFactory.getLogger("io.zero88")).setLevel(Level.TRACE);
    }

    @Before
    public void before() {
        vertx = Vertx.vertx();
        application = new MockApplication();
    }

    @Test
    public void test_contain_two_component_vertical_having_same_type_should_deploy_only_one(TestContext context) {
        addDummyUnit();
        addDummyUnit();

        Async async = context.async();
        VertxHelper.deploy(vertx, context, new DeploymentOptions(), application, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(2, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_contain_two_component_vertical_having_different_type_should_deploy_both(TestContext context) {
        addDummyUnit();
        addMockUnit();

        Async async = context.async();
        VertxHelper.deploy(vertx, context, new DeploymentOptions(), application, deployId -> {
            context.assertNotNull(deployId);
            TestHelper.testComplete(async);
            Assert.assertEquals(3, vertx.deploymentIDs().size());
        });
    }

    @Test
    public void test_application_throw_exception_cannot_start(TestContext context) {
        application.setError(true);
        addDummyUnit();
        assertDeployError(context, new RuntimeException("Error when starting"));
    }

    @Test
    public void test_application_throw_exception_in_handler_cannot_start(TestContext context) {
        application.setErrorInHandler(true);
        addDummyUnit();
        assertDeployError(context, new IllegalArgumentException("Error in success handler"));
    }

    @Test
    public void test_component_throw_exception_cannot_start(TestContext context) {
        addDummyUnit();
        addMockUnitHavingException();
        assertDeployError(context, new QWEException("UNKNOWN_ERROR | Cause: Error when starting Component Verticle"));
    }

    private void assertDeployError(TestContext context, Throwable error) {
        Async async = context.async();
        VertxHelper.deployFailed(vertx, context, new DeploymentOptions(), application, t -> {
            try {
                Assert.assertTrue(error.getClass().isInstance(t));
                Assert.assertEquals(error.getMessage(), t.getMessage());
                Assert.assertEquals(0, vertx.deploymentIDs().size());
            } finally {
                TestHelper.testComplete(async);
            }
        });
    }

    private void addMockUnit() {
        addMockUnit(false);
    }

    private void addMockUnitHavingException() {
        addMockUnit(true);
    }

    private void addMockUnit(boolean error) {
        MockProvider provider = new MockProvider(error);
        application.addProvider(provider);
    }

    private void addDummyUnit() {
        application.addProvider(new DummyProvider());
    }

    @After
    public void after() {
        vertx.close();
    }

}
