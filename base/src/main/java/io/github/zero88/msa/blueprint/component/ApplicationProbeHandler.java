package io.github.zero88.msa.blueprint.component;

import java.util.Arrays;
import java.util.Collection;

import io.github.zero88.msa.blueprint.dto.ErrorData;
import io.github.zero88.msa.blueprint.dto.msg.RequestData;
import io.github.zero88.msa.blueprint.event.EventAction;
import io.github.zero88.msa.blueprint.event.EventContractor;
import io.github.zero88.msa.blueprint.event.EventListener;
import io.github.zero88.msa.blueprint.event.EventPattern;

import lombok.NonNull;

/**
 * Application probe handler
 * <p>
 * It is handler by pattern {@link EventPattern#PUBLISH_SUBSCRIBE}
 */
public interface ApplicationProbeHandler extends EventListener {

    @Override
    default @NonNull Collection<EventAction> getAvailableEvents() {
        return Arrays.asList(EventAction.NOTIFY, EventAction.NOTIFY_ERROR);
    }

    @EventContractor(action = "NOTIFY", returnType = boolean.class)
    boolean success(@NonNull RequestData requestData);

    @EventContractor(action = "NOTIFY_ERROR", returnType = boolean.class)
    boolean error(@NonNull ErrorData error);

    /**
     * Application readiness handler
     *
     * @see ApplicationProbe#readiness()
     */
    interface ApplicationReadinessHandler extends ApplicationProbeHandler {}


    /**
     * Application liveness handler
     *
     * @see ApplicationProbe#liveness()
     */
    interface ApplicationLivenessHandler extends ApplicationProbeHandler {}

}
