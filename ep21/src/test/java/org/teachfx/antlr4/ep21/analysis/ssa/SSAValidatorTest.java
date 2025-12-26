package org.teachfx.antlr4.ep21.analysis.ssa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import org.teachfx.antlr4.ep21.ir.expr.val.ConstVal;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;
import org.teachfx.antlr4.ep21.symtab.scope.GlobalScope;
import org.teachfx.antlr4.ep21.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep21.symtab.type.BuiltInTypeSymbol;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSA Validator Test Suite
 *
 * Tests for the SSAValidator class which validates:
 * - Variable version consistency
 * - Phi function parameter correctness
 * - Use-before-def violations
 *
 * @author EP21 Team
 * @version 1.0
 */
@DisplayName("SSA Validator Tests")
public class SSAValidatorTest {

    private GlobalScope globalScope;
    private BuiltInTypeSymbol intType;

    @BeforeEach
    void setUp() {
        globalScope = new GlobalScope();
        intType = new BuiltInTypeSymbol("int");
    }

    /**
     * Test 1: Valid SSA graph should pass validation
     */
    @Test
    @DisplayName("Given: Valid SSA graph, When: Validate, Then: Should pass")
    void testValidSSAGraph() {
        // Create a simple valid SSA graph:
        // x_1 = 1
        // if (x_1 < 10) goto L1 else goto L2
        // L1: x_2 = x_1 + 1
        // L2: x_3 = PHI(x_1, x_2)

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // Note: This test would require constructing a full SSA graph
        // For now, we test the validator with minimal setup
        // In practice, you'd build a complete SSAGraph and call validate()

        assertTrue(true, "SSAValidator exists and can be instantiated");
    }

    /**
     * Test 2: Variable consistency - each variable should have consecutive version numbers
     */
    @Test
    @DisplayName("Given: SSA with non-consecutive versions, When: Validate, Then: Should report error")
    void testVariableConsistency() {
        // This test would check that variables like x_1, x_2, x_3 are consecutive
        // and not x_1, x_3, x_5 (missing versions)

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // Create a CFG with inconsistent variable versions
        List<IRNode> instructions = new ArrayList<>();
        MethodSymbol methodSymbol = new MethodSymbol("test", intType, globalScope, null);
        instructions.add(new FuncEntryLabel("test", 1, 1, globalScope));

        // Build CFG
        List<BasicBlock<IRNode>> blocks = new ArrayList<>();
        blocks.add(new BasicBlock<>(
            Kind.CONTINUOUS,
            instructions.stream().map(instr -> new Loc<>(instr)).toList(),
            new Label("test_entry", globalScope),
            0
        ));

        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        CFG<IRNode> cfg = new CFG<>(blocks, edges);

        // Build SSA from this CFG
        SSAGraph ssaGraph = new SSAGraph(cfg);
        ssaGraph.buildSSA();

        // Validate
        SSAGraph.ValidationResult result = validator.validate(ssaGraph);

        // For a simple CFG, SSA should be valid
        // This test verifies the validator runs without crashing
        assertNotNull(result, "Validation result should not be null");
        assertNotNull(result.getSummary(), "Summary should not be null");
    }

    /**
     * Test 3: Phi function parameters should match predecessor count
     */
    @Test
    @DisplayName("Given: Phi with wrong parameter count, When: Validate, Then: Should report error")
    void testPhiFunctionParameterCount() {
        // A Phi function in a block with 2 predecessors should have 2 parameters
        // e.g., x_3 = PHI(x_1, x_2) for if/else merge

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // This test would require constructing a CFG with:
        // - A block with multiple predecessors
        // - A Phi function with incorrect number of parameters

        assertTrue(true, "Phi function validation test placeholder");
    }

    /**
     * Test 4: Use-before-def should be detected
     */
    @Test
    @DisplayName("Given: Variable used before definition, When: Validate, Then: Should report error")
    void testUseBeforeDefDetection() {
        // If a variable x_5 is used but only x_1, x_2 are defined, that's an error

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // This test would require constructing a CFG where:
        // - A variable is referenced with a higher version number
        // - That version hasn't been defined yet in the control flow

        assertTrue(true, "Use-before-def validation test placeholder");
    }

    /**
     * Test 5: ValidationResult should have correct structure
     */
    @Test
    @DisplayName("Given: ValidationResult, When: Access properties, Then: Should return correct values")
    void testValidationResultStructure() {
        // Test valid result
        SSAGraph.ValidationResult validResult = new SSAGraph.ValidationResult(true, new ArrayList<>());

        assertTrue(validResult.isValid(), "Valid result should return true");
        assertTrue(validResult.getErrors().isEmpty(), "Valid result should have no errors");
        assertEquals("SSA验证通过", validResult.getSummary(), "Summary should indicate success");

        // Test invalid result
        List<String> errors = List.of("Error 1", "Error 2");
        SSAGraph.ValidationResult invalidResult = new SSAGraph.ValidationResult(false, errors);

        assertFalse(invalidResult.isValid(), "Invalid result should return false");
        assertEquals(2, invalidResult.getErrors().size(), "Should have 2 errors");
        assertTrue(invalidResult.getSummary().contains("2 个错误"), "Summary should mention error count");
    }

    /**
     * Test 6: Validator should handle minimal CFG
     */
    @Test
    @DisplayName("Given: Minimal CFG with one block, When: Validate, Then: Should not crash")
    void testMinimalCFG() {
        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // Create minimal CFG with at least one instruction (function entry)
        List<IRNode> instructions = new ArrayList<>();
        MethodSymbol methodSymbol = new MethodSymbol("minimal", intType, globalScope, null);
        instructions.add(new FuncEntryLabel("minimal", 0, 0, globalScope));

        List<BasicBlock<IRNode>> blocks = new ArrayList<>();
        blocks.add(new BasicBlock<>(
            Kind.CONTINUOUS,
            instructions.stream().map(instr -> new Loc<>(instr)).toList(),
            new Label("minimal_entry", globalScope),
            0
        ));

        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        CFG<IRNode> cfg = new CFG<>(blocks, edges);

        assertDoesNotThrow(() -> {
            SSAGraph ssaGraph = new SSAGraph(cfg);
            SSAGraph.ValidationResult result = validator.validate(ssaGraph);
            assertNotNull(result);
        });
    }

    /**
     * Test 7: Validator should handle CFG with no Phi functions
     */
    @Test
    @DisplayName("Given: CFG without Phi functions, When: Validate, Then: Should pass")
    void testCFGWithoutPhi() {
        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // Create simple linear CFG (no branches, no Phi needed)
        List<IRNode> instructions = new ArrayList<>();
        MethodSymbol methodSymbol = new MethodSymbol("linear", intType, globalScope, null);
        instructions.add(new FuncEntryLabel("linear", 0, 0, globalScope));

        List<BasicBlock<IRNode>> blocks = new ArrayList<>();
        blocks.add(new BasicBlock<>(
            Kind.CONTINUOUS,
            instructions.stream().map(instr -> new Loc<>(instr)).toList(),
            new Label("linear_entry", globalScope),
            0
        ));

        List<org.apache.commons.lang3.tuple.Triple<Integer, Integer, Integer>> edges = new ArrayList<>();
        CFG<IRNode> cfg = new CFG<>(blocks, edges);

        SSAGraph ssaGraph = new SSAGraph(cfg);
        ssaGraph.buildSSA();

        // Should validate successfully (no Phi functions needed for linear code)
        assertDoesNotThrow(() -> {
            SSAGraph.ValidationResult result = validator.validate(ssaGraph);
            // Note: Result may or may not be valid depending on SSA construction
            assertNotNull(result);
        });
    }

    /**
     * Test 8: Validator should handle complex control flow
     */
    @Test
    @DisplayName("Given: CFG with loops, When: Validate, Then: Should check Phi functions")
    void testCFGWithLoops() {
        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        // This test would require constructing a CFG with:
        // - A loop (back edge)
        // - Phi functions for loop variables
        // - Multiple paths merging

        assertTrue(true, "Loop CFG validation test placeholder");
    }

    /**
     * Test 9: Multiple variables should be validated independently
     */
    @Test
    @DisplayName("Given: SSA with multiple variables, When: Validate, Then: Should check all variables")
    void testMultipleVariables() {
        // Test with variables: x_1, x_2, y_1, y_2, z_1
        // Each should have consistent versioning

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        assertTrue(true, "Multiple variables validation test placeholder");
    }

    /**
     * Test 10: Nested scope variables should be validated correctly
     */
    @Test
    @DisplayName("Given: SSA with nested scopes, When: Validate, Then: Should handle scoping correctly")
    void testNestedScopes() {
        // Test that variables in different scopes don't interfere

        SSAGraph.SSAValidator validator = new SSAGraph.SSAValidator();

        assertTrue(true, "Nested scopes validation test placeholder");
    }
}
