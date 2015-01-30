package com.mooregreatsoftware.jpatterns.unionclass;

import javax.annotation.Nullable;

/**
 * Indicates that the call was successful.
 */
public interface Success<T> {
    /**
     * The value of the invocation.
     */
    @Nullable
    T get();
}
