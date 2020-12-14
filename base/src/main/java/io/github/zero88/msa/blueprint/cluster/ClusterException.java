package io.github.zero88.msa.blueprint.cluster;

import io.github.zero88.msa.blueprint.exceptions.ErrorCode;
import io.github.zero88.msa.blueprint.exceptions.EngineException;

public final class ClusterException extends EngineException {

    public static final io.github.zero88.exceptions.ErrorCode CLUSTER_ERROR = new ErrorCode("CLUSTER_ERROR");

    public ClusterException(String message, Throwable e) {
        super(CLUSTER_ERROR, message, e);
    }

    public ClusterException(String message) { this(message, null); }

    public ClusterException(Throwable e)    { this(null, e); }

}

