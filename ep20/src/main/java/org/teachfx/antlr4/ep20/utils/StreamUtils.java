package org.teachfx.antlr4.ep20.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> int indexOf(Stream<T> stream, Predicate<T> predicate) {
        AtomicInteger cnt = new AtomicInteger();
        return stream.map(s -> Pair.of(s, cnt.getAndIncrement())).filter(tIntegerPair ->
            predicate.test(tIntegerPair.getLeft())
        ).findFirst().map(Pair::getRight).orElse(-1);
    }
    // find matched object
    public static <T> T find(Stream<T> stream, Predicate<T> predicate) {
        return stream.filter(predicate).findFirst().orElse(null);
    }

    // remove if find any matched object
    public static <T> Stream<T> removeIf(Stream<T> stream, Predicate<T> predicate) {
        return stream.filter(predicate.negate());
    }
    // generate code for flatmap
    public static <T, R> Stream<R> flatMap(Stream<T> stream, Function<T, Stream<R>> mapper) {
        return stream.flatMap(mapper);
    }
}


