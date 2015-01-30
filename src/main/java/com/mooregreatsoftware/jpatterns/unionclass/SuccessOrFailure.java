package com.mooregreatsoftware.jpatterns.unionclass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * An example of creating a "closed union class" in Java.
 * <p>
 * This technique is useful for any case where you need to use a constrained possible set of types,
 * but they don't all inherit from the same hierarchy, so "normal" polymorphism does not apply.
 * <p>
 * It's a very typical thing to do in environments with a much more robust type system, but can be
 * "forced" even in Java, as seen below. See the "main" method for an example of usage.
 * <p>
 * Inspired by http://www.slideshare.net/ScottWlaschin/fp-patterns-buildstufflt
 */
public class SuccessOrFailure<T> {
    private final Object val;  // the types don't share a hierarchy, so this is the best we can do...


    /**
     * We are guaranteeing that this can only be created with a Success or a Failure through the factory methods.
     */
    private SuccessOrFailure(@Nonnull Object val) {
        this.val = val;
    }


    /**
     * Returns a SuccessOrFailure that contains a Success.
     */
    @Nonnull
    public static <T> SuccessOrFailure<T> success(@Nullable T val) {
        // taking advantage of Java 8 lambda support; otherwise can be done
        // more clumsily and slowly with an inner class
        return new SuccessOrFailure<>(((Success)() -> val));
    }


    /**
     * Returns a SuccessOrFailure that contains a Failure with the given message.
     */
    @Nonnull
    public static <T> SuccessOrFailure<T> failure(@Nonnull String message) {
        return new SuccessOrFailure<>((Failure)() -> message);
    }


    /**
     * Returns a SuccessOrFailure that contains a Failure with the given Throwable.
     */
    @Nonnull
    public static <T> SuccessOrFailure<T> failure(@Nonnull Throwable throwable) {
        return new SuccessOrFailure<>(new ThrowableFailure(throwable));
    }


    /**
     * If this contains a Success, return it; otherwise returns a null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public Success<T> success() {
        return (val instanceof Success) ? (Success<T>)val : null;
    }


    /**
     * If this contains a Failure, return it; otherwise returns a null
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public Failure failure() {
        return (val instanceof Failure) ? (Failure)val : null;
    }


    /**
     * Convert a "normal" function to return a SuccessOrFailure.
     * The only thing that will generate a Failure is if the function throws an exception.
     *
     * @param func the function to translate to return SuccessOrFailure
     * @param <P>  the type of the parameter to the function
     * @param <R>  the type of the return value
     */
    @Nonnull
    public static <P, R> Function<P, SuccessOrFailure<R>> bind(Function<P, R> func) {
        // prior to Java 8 this can be done with a Callable or the like, but... ugh
        return (x) -> {
            try {
                return SuccessOrFailure.success(func.apply(x));
            }
            catch (Throwable exp) {
                return SuccessOrFailure.failure(exp);
            }
        };
    }


    /**
     * Merge a pair of "normal" Consumers into a single Consumer that can handle a SuccessOrFailure.
     *
     * @param successConsumer the Consumer to call if a Success is passed in
     * @param failureConsumer the Consumer to call if a Failure is passed in
     * @param <P>             the type of the parameter for Success
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <P> Consumer<SuccessOrFailure<P>> merge(Consumer<P> successConsumer, Consumer<Failure> failureConsumer) {
        return x -> {
            if (x.failure() == null)
                successConsumer.accept(x.success().get());
            else
                failureConsumer.accept(x.failure());
        };
    }


    /**
     * Convert a "normal" function to accept a SuccessOrFailure and return a SuccessOrFailure.
     * The only thing that will generate a Failure is if the function throws an exception.
     * If a Failure is passed in, it is simply returned.
     *
     * @param func the function to translate to return SuccessOrFailure
     * @param <P>  the type of the parameter to the function
     * @param <R>  the type of the return value
     */
    @Nonnull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static <P, R> Function<SuccessOrFailure<P>, SuccessOrFailure<R>> map(Function<P, R> func) {
        return (x) -> {
            if (x.failure() != null) return (SuccessOrFailure<R>)x;

            try {
                return SuccessOrFailure.success(func.apply(x.success().get()));
            }
            catch (Throwable exp) {
                return SuccessOrFailure.failure(exp);
            }
        };
    }


    /**
     * Maps a pair of "normal" Consumers into a Function that can handle a SuccessOrFailure.
     * Depending on if Success or Failure is passed in, the appropriate Consumer is called and the
     * same SuccessOrFailure is returned to be propagated along the chain.
     *
     * @param successConsumer the Consumer to call if a Success is passed in
     * @param failureConsumer the Consumer to call if a Failure is passed in
     * @param <P>             the type of the parameter for Success
     */
    @Nonnull
    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static <P> Function<SuccessOrFailure<P>, SuccessOrFailure<P>> map(Consumer<P> successConsumer, Consumer<Failure> failureConsumer) {
        return (x) -> {
            Failure failure = x.failure();
            if (failure != null)
                failureConsumer.accept(failure);
            else
                successConsumer.accept(x.success().get());
            return x;
        };
    }


    @Override
    public String toString() {
        return (val instanceof Success) ? "Success(" + ((Success)val).get() + ")" : "Failure(" + ((Failure)val).errorMessage() + ")";
    }

}
