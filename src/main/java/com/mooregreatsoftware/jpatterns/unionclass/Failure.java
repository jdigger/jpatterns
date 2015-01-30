package com.mooregreatsoftware.jpatterns.unionclass;

import javax.annotation.Nonnull;

/**
 * Indicates there was an error.
 */
public interface Failure {
    /**
     * Returns the error.
     */
    @Nonnull
    String errorMessage();
}
