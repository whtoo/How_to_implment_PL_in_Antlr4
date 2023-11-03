package org.teachfx.antlr4.ep20.driver;

import org.teachfx.antlr4.ep20.error.CymbalError;
import org.teachfx.antlr4.ep20.parser.Location;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

public interface ErrorIssuer {

    Location pos = null;

    ArrayList<CymbalError> errors = new ArrayList<>();

    /**
     * Add an error.
     *
     * @param error Decaf error
     */
    default void issue(CymbalError error) {
        errors.add(error);
    }

    /**
     * Has any error been added?
     *
     * @return true/false
     */
    default boolean hasError() {
        return !errors.isEmpty();
    }

    /**
     * Print out error messages, sorted by their error positions.
     *
     * @param to where to print
     */
    default void printErrors(PrintStream to) {
        errors.sort(Comparator.comparing(o -> o.location));
        errors.forEach(to::println);
    }
}
