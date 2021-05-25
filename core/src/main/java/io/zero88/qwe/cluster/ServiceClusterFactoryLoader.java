package io.zero88.qwe.cluster;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.vertx.core.ServiceHelper;

public final class ServiceClusterFactoryLoader {

    private final Map<ClusterType, ClusterManagerFactory> factories;

    public ServiceClusterFactoryLoader() {
        this.factories = ServiceHelper.loadFactories(ClusterManagerFactory.class, getClass().getClassLoader())
                                      .stream()
                                      .collect(Collectors.toMap(ClusterManagerFactory::type, Function.identity()));
    }

    public ClusterManagerFactory lookup(ClusterType clusterType) {
        return factories.get(clusterType);
    }

}
