package org.teachfx.antlr4.ep21.pass.codegen;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result object for code generation operations.
 * <p>
 * Contains the generated code output, metadata, and any errors that occurred
 * during code generation. Provides utility methods for checking success status.
 * </p>
 */
public class CodeGenerationResult {

    private final String output;
    private final List<String> errors;
    private final String targetVM;
    private final int instructionCount;
    private final long generationTimeMs;

    /**
     * Creates a new code generation result.
     *
     * @param output the generated code output
     * @param errors list of errors (empty if successful)
     * @param targetVM the target VM identifier (e.g., "EP18", "EP18R")
     * @param instructionCount the number of instructions generated
     * @param generationTimeMs the time taken to generate code in milliseconds
     */
    public CodeGenerationResult(
            String output,
            List<String> errors,
            String targetVM,
            int instructionCount,
            long generationTimeMs) {
        this.output = output != null ? output : "";
        this.errors = errors != null ? errors : List.of();
        this.targetVM = targetVM;
        this.instructionCount = instructionCount;
        this.generationTimeMs = generationTimeMs;
    }

    /**
     * Creates a successful result with no errors.
     *
     * @param output the generated code output
     * @param targetVM the target VM identifier
     * @param instructionCount the number of instructions generated
     * @param generationTimeMs the time taken to generate code in milliseconds
     * @return a new successful CodeGenerationResult
     */
    public static CodeGenerationResult success(
            String output,
            String targetVM,
            int instructionCount,
            long generationTimeMs) {
        return new CodeGenerationResult(
                output,
                Collections.emptyList(),
                targetVM,
                instructionCount,
                generationTimeMs);
    }

    /**
     * Creates a failed result with errors.
     *
     * @param errors the list of error messages
     * @param targetVM the target VM identifier
     * @return a new failed CodeGenerationResult
     */
    public static CodeGenerationResult failure(List<String> errors, String targetVM) {
        return new CodeGenerationResult("", errors, targetVM, 0, 0);
    }

    /**
     * Gets the generated code output.
     *
     * @return the generated code string
     */
    public String getOutput() {
        return output;
    }

    /**
     * Gets the list of errors that occurred during generation.
     *
     * @return an unmodifiable list of error messages
     */
    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Gets the target VM identifier.
     *
     * @return the target VM (e.g., "EP18", "EP18R")
     */
    public String getTargetVM() {
        return targetVM;
    }

    /**
     * Gets the number of instructions generated.
     *
     * @return the instruction count
     */
    public int getInstructionCount() {
        return instructionCount;
    }

    /**
     * Gets the time taken for code generation.
     *
     * @return generation time in milliseconds
     */
    public long getGenerationTimeMs() {
        return generationTimeMs;
    }

    /**
     * Checks if code generation was successful.
     *
     * @return true if no errors occurred, false otherwise
     */
    public boolean isSuccess() {
        return errors.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeGenerationResult that = (CodeGenerationResult) o;
        return instructionCount == that.instructionCount
                && generationTimeMs == that.generationTimeMs
                && Objects.equals(output, that.output)
                && Objects.equals(errors, that.errors)
                && Objects.equals(targetVM, that.targetVM);
    }

    @Override
    public int hashCode() {
        return Objects.hash(output, errors, targetVM, instructionCount, generationTimeMs);
    }

    @Override
    public String toString() {
        return "CodeGenerationResult{" +
                "output='" + output + '\'' +
                ", errors=" + errors +
                ", targetVM='" + targetVM + '\'' +
                ", instructionCount=" + instructionCount +
                ", generationTimeMs=" + generationTimeMs +
                '}';
    }
}
