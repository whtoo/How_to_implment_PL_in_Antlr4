package org.teachfx.antlr4.ep14.test;

import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep14.Compiler;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EP14 tests: Classic optimizations (DCE, const prop, CSE, copy prop).
 */
public class OptimizerTest {

    @Test
    void testDCERemovesUnusedVariables() throws Exception {
        Method dce = Compiler.class.getDeclaredMethod("dce", List.class);
        dce.setAccessible(true);
        var prog = List.of("x = 10", "y = 20", "z = x + 5", "return z");
        @SuppressWarnings("unchecked")
        var result = (List<String>) dce.invoke(null, prog);
        assertEquals(3, result.size(), "Should remove dead 'y = 20'");
    }

    @Test
    void testConstantPropagationFoldsExpressions() throws Exception {
        Method cp = Compiler.class.getDeclaredMethod("constantProp", List.class);
        cp.setAccessible(true);
        var prog = List.of("a = 10", "b = 5", "c = a + b", "return c");
        @SuppressWarnings("unchecked")
        var result = (List<String>) cp.invoke(null, prog);
        assertTrue(result.get(2).contains("15"), "c should be folded to 15");
    }

    @Test
    void testCSEEliminatesRedundantComputations() throws Exception {
        Method cse = Compiler.class.getDeclaredMethod("cse", List.class);
        cse.setAccessible(true);
        var prog = List.of("t1 = a + b", "t2 = a + b", "return t1");
        @SuppressWarnings("unchecked")
        var result = (List<String>) cse.invoke(null, prog);
        assertTrue(result.get(1).contains("CSE"), "Should mark CSE elimination");
    }

    @Test
    void testCopyPropagationReplacesVariables() throws Exception {
        Method copyProp = Compiler.class.getDeclaredMethod("copyProp", List.class);
        copyProp.setAccessible(true);
        var prog = List.of("x = y", "z = x + 1");
        @SuppressWarnings("unchecked")
        var result = (List<String>) copyProp.invoke(null, prog);
        assertTrue(result.get(1).contains("y + 1"), "x should be replaced with y");
    }

    @Test
    void testCombinedPipelineOptimizesAggressively() throws Exception {
        Method cp = Compiler.class.getDeclaredMethod("copyProp", List.class);
        Method cse = Compiler.class.getDeclaredMethod("cse", List.class);
        Method constProp = Compiler.class.getDeclaredMethod("constantProp", List.class);
        Method dce = Compiler.class.getDeclaredMethod("dce", List.class);
        cp.setAccessible(true); cse.setAccessible(true); constProp.setAccessible(true); dce.setAccessible(true);

        var prog = List.of("a = 5", "b = 10", "c = a", "d = c + b", "e = a + b", "f = d * 2", "g = e * 2", "h = 99", "return f");
        var step1 = (List<String>) cp.invoke(null, prog);
        var step2 = (List<String>) cse.invoke(null, step1);
        var step3 = (List<String>) constProp.invoke(null, step2);
        var step4 = (List<String>) dce.invoke(null, step3);

        assertTrue(step4.size() <= 2, "Pipeline should reduce to ~1-2 instructions");
    }
}
