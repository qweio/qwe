package io.github.zero88.msa.blueprint.transport;

import io.vertx.core.Vertx;

/**
 * Transporter can be {@code HTTP client}, {@code Eventbus client}, {@code Kafka client}, {@code MQTT client}
 *
 * @since 1.0.0
 */
public interface Transporter {

    /**
     * Gets the associate vertx.
     *
     * @return the vertx
     * @since 1.0.0
     */
    Vertx getVertx();

}
