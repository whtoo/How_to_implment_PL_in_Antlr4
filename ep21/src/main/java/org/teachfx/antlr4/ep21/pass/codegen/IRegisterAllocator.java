package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

/**
 * Register allocator interface for EP21 code generation.
 * 
 * <p>This interface defines the contract for register allocation in EP21,
 * providing abstraction over different register allocation strategies.
 * It maps EP21 VariableSymbol objects to physical registers and handles
 * stack spilling when registers are insufficient.</p>
 * 
 * <p>EP18R Register Convention (16 registers):</p>
 * <ul>
 *   <li>r0: Zero register (always 0, not allocatable)</li>
 *   <li>r1: Return address (ra, caller-saved)</li>
 *   <li>r2: Parameter 0 / Return value (a0, caller-saved)</li>
 *   <li>r3: Parameter 1 (a1, caller-saved)</li>
 *   <li>r4: Parameter 2 (a2, caller-saved)</li>
 *   <li>r5: Parameter 3 (a3, caller-saved)</li>
 *   <li>r6: Parameter 4 (a4, caller-saved)</li>
 *   <li>r7: Parameter 5 (a5, caller-saved)</li>
 *   <li>r8-r12: Callee-saved registers (s0-s4)</li>
 *   <li>r13: Stack pointer (sp, not allocatable)</li>
 *   <li>r14: Frame pointer (fp, not allocatable)</li>
 *   <li>r15: Link register (lr, caller-saved)</li>
 * </ul>
 * 
 * <p>Allocatable registers: r1-r12, r15 (13 total)</p>
 * 
 * @author EP21 Compiler Team
 * @version 1.0
 */
public interface IRegisterAllocator {

    /**
     * Allocate a physical register for the given variable.
     *
     * @param variable the variable symbol to allocate a register for
     * @return the allocated physical register number (0-15)
     * @throws IllegalStateException if no registers are available and spilling fails
     * @throws IllegalArgumentException if variable is null
     */
    int allocateRegister(VariableSymbol variable);

    /**
     * Get the stack offset for a spilled variable.
     *
     * @param variable the variable symbol
     * @return the stack offset from frame pointer (fp), or -1 if variable is not spilled
     * @throws IllegalArgumentException if variable is null
     */
    int getStackOffset(VariableSymbol variable);

    /**
     * Reset the allocator state, clearing all allocations.
     * 
     * <p>This method should release all allocated registers and clear spill slots,
     * returning the allocator to a clean state suitable for allocating a new function.</p>
     */
    void reset();

    /**
     * Get the count of currently allocated registers.
     *
     * @return the number of registers currently allocated
     */
    int getAllocatedRegisterCount();

    /**
     * Get the physical register number assigned to a variable.
     *
     * @param variable the variable symbol
     * @return the register number (0-15), or -1 if no register is allocated
     * @throws IllegalArgumentException if variable is null
     */
    int getRegister(VariableSymbol variable);

    /**
     * Check if a variable is spilled to the stack.
     *
     * @param variable the variable symbol
     * @return true if the variable is spilled, false otherwise
     * @throws IllegalArgumentException if variable is null
     */
    boolean isSpilled(VariableSymbol variable);

    /**
     * Release the register allocated to a variable.
     *
     * @param variable the variable symbol to release
     * @throws IllegalArgumentException if variable is null
     */
    void freeRegister(VariableSymbol variable);
}