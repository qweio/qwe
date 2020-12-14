package io.github.zero88.msa.blueprint.component;

/**
 * Application probe
 */
public interface ApplicationProbe {

    /**
     * Check is able to serve requests.
     *
     * @return {@code true} if application is able to serve requests, otherwise is {@code false}
     */
    boolean readiness();

    /**
     * Health check for an application is running, but unable to make progress
     *
     * @return {@code true} if application is healthy
     */
    boolean liveness();

}
