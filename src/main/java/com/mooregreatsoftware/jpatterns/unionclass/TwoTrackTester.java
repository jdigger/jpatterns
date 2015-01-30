package com.mooregreatsoftware.jpatterns.unionclass;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.LongStream;


/**
 * Provides some simple examples of using {@link SuccessOrFailure}.
 *
 * @see #simpleSucceedOrFail()
 * @see #seriesOfFunctions()
 * @see #usingStreams()
 */
public class TwoTrackTester {

    /**
     * Simple example of making a call that can either succeed or fail
     */
    @SuppressWarnings("ConstantConditions")
    private static void simpleSucceedOrFail() {

        SuccessOrFailure<Date> retVal;

        // do something successfully
        retVal = doSomethingWithFailure(true);
        if (retVal.success() != null) {
            System.out.println("Success: " + retVal.success().get());
        }

        // do something but fail
        retVal = doSomethingWithFailure(false);
        if (retVal.success() == null) {
            System.out.println("Failed: " + retVal.failure().errorMessage());
        }
    }


    /**
     * Create a function that gets the current Date and converts it to a String.
     * The "bind" converts a "normal" function to return a SuccessOrFailure.
     * The "map" converts a "normal" function to accept a SuccessOrFailure and
     * return SuccessOrFailure.
     */
    private static void seriesOfFunctions() {
        Function<Boolean, SuccessOrFailure<String>> funcDateAsString =
            SuccessOrFailure.<Boolean, Date>bind((shouldSucceed) -> {
                if (shouldSucceed) return new Date();
                else throw new IllegalArgumentException("Could not get Date");
            }).andThen(SuccessOrFailure.map(Date::toString));

        Consumer<String> successPrinter = x -> System.out.println("Good: " + x);
        Consumer<Failure> failurePrinter = x -> System.out.println("Boom: " + x.errorMessage());

        // Add Consumers to the end of the function chain and call by applying some arguments
        funcDateAsString.
            andThen(SuccessOrFailure.map(successPrinter, failurePrinter)).apply(true);
        funcDateAsString.
            andThen(SuccessOrFailure.map(successPrinter, failurePrinter)).apply(false);
    }


    /**
     * This is where it really comes into its own:
     * <p>
     * When the same techniques are applied to a Stream, it allows data to cleanly move from one end of
     * the Stream to the other. It's trivial to add validators, enrichers, transformers, wiretaps, etc.
     * <p>
     * In contrast to an EIP framework (such as Apache Camel or Spring Integration), there's fewer moving parts
     * (e.g., Messages, Exchanges, etc.) so it's both more efficient and easier to reason about at the cost of
     * not being quite as powerful.
     * <p>
     * In contrast to reactive programming, this provides a very serial path, which is much easier to
     * reason about (and the call stack is actually useful).
     * <p>
     * Of course it also means that any concurrent execution will consume that thread/process until the
     * Stream is finished because it isn't taking advantage of an event reactor/dispatcher.
     * <p>
     * Use whatever tool works best for the problem at hand.
     */
    private static void usingStreams() {
        Consumer<String> successPrinter = x -> System.out.println("Good: " + x);
        Consumer<Failure> failurePrinter = x -> System.out.println("Boom: " + x.errorMessage());

        LongStream longStream = new Random().longs();

        // only even longs are allowed through (for whatever arbitrary reason :-)
        Function<Long, SuccessOrFailure<Long>> validator =
            x -> (x % 2 == 0) ? SuccessOrFailure.success(x) : SuccessOrFailure.failure("Could not get an odd value");

        longStream.limit(10).parallel().
            mapToObj(validator::apply). // validate the data (fail odd numbers)
            map(SuccessOrFailure.map((Function<Long, Date>)Date::new)). // transform long to Date
            map(SuccessOrFailure.map(Date::toString)). // transform Date to String
            peek(x -> System.out.println("Seeing " + x + " pass by")). // put a "wiretap" in to see traffic through the Stream
            forEach(SuccessOrFailure.merge(successPrinter, failurePrinter)); // print the results
    }


    public static void main(String[] args) {
        simpleSucceedOrFail();
        seriesOfFunctions();
        usingStreams();
    }


    @Nonnull
    static SuccessOrFailure<Date> doSomethingWithFailure(boolean shouldSucceed) {
        return (shouldSucceed) ? SuccessOrFailure.success(new Date()) : SuccessOrFailure.failure("Got a failure");
    }

}
