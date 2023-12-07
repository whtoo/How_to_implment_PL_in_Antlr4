package org.teachfx.antlr4.ep20.utils;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Stream<? super Pair<Integer, T>> mapWithIndex(Stream<T> stream,Function<? super Pair<Integer, T>,? super Pair<Integer, T>> mapper) {
        AtomicInteger cnt = new AtomicInteger();
        return stream.map(s -> Pair.of( cnt.getAndIncrement(),s)).map(mapper);
    }

    public static <T> Stream<Pair<Integer,T>> indexStream(Stream<T> stream) {
        AtomicInteger cnt = new AtomicInteger();
        return stream.map(s -> Pair.of(cnt.getAndIncrement(),s));
    }

    public static <T> int indexOf(Stream<T> stream, Predicate<T> predicate) {
       return (int) StreamUtils.indexStream(stream).filter(tIntegerPair ->
            predicate.test(tIntegerPair.getRight())
        ).findFirst().map(Pair::getLeft).orElse(-1);
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


