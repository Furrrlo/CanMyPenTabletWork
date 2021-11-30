package com.github.furrrlo.cmptw.windows.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Futures {

    private Futures() {
    }

    public static <T, EX extends Throwable> T waitFutureAndPropagateException(Future<T> future,
                                                                              Function<Throwable, EX> exceptionSupplier) throws EX {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw exceptionSupplier.apply(e);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error || cause instanceof RuntimeException) {
                // Add this thread stacktrace to the exception
                cause.addSuppressed(new Exception("Called from here"));

                if (cause instanceof Error)
                    throw (Error) cause;
                // if(cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            }
            throw exceptionSupplier.apply(cause != null ? cause : e);
        }
    }
}
