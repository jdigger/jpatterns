package com.mooregreatsoftware.jpatterns.unionclass;

import javax.annotation.Nonnull;

public class ThrowableFailure implements Failure {
    private Throwable throwable;


    public ThrowableFailure(@Nonnull Throwable throwable) {
        this.throwable = throwable;
    }


    @Nonnull
    @Override
    public String errorMessage() {
        return this.throwable.toString();
    }


    @SuppressWarnings("UnusedDeclaration")
    public Throwable getThrowable() {
        return throwable;
    }
}
