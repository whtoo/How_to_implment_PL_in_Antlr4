package org.teachfx.antlr4.ep21.pass.codegen;

import java.util.List;

/**
 * Instruction emitter interface for code generation.
 * <p>
 * This interface provides a unified abstraction for emitting VM instructions,
 * labels, comments, and managing code formatting (indentation, scoping).
 * Implementations can target different output formats (assembly, bytecode, etc.).
 * </p>
 */
public interface IEmitter {

    /**
     * Emits a single instruction.
     *
     * @param instruction the instruction string to emit
     */
    void emit(String instruction);

    /**
     * Emits a label definition.
     *
     * @param label the label name
     */
    void emitLabel(String label);

    /**
     * Emits a comment.
     *
     * @param comment the comment text
     */
    void emitComment(String comment);

    /**
     * Emits multiple instructions at once.
     *
     * @param instructions the list of instructions to emit
     */
    void emitAll(List<String> instructions);

    /**
     * Begins a new scope (e.g., function, block).
     *
     * @param scopeName the name of the scope being entered
     */
    void beginScope(String scopeName);

    /**
     * Ends the current scope.
     */
    void endScope();

    /**
     * Flushes and returns all emitted content.
     * <p>
     * After calling this method, the internal buffer is typically cleared.
     * </p>
     *
     * @return the accumulated output string
     */
    String flush();

    /**
     * Clears the internal buffer and resets state.
     */
    void clear();

    /**
     * Gets the current indentation level.
     *
     * @return the current indent level (number of indent units)
     */
    int getIndentLevel();

    /**
     * Sets the indentation level.
     *
     * @param level the new indent level
     */
    void setIndentLevel(int level);
}
