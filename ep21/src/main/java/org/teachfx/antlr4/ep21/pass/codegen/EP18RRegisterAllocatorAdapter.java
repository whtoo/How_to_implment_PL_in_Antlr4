package org.teachfx.antlr4.ep21.pass.codegen;

import org.teachfx.antlr4.ep18r.stackvm.codegen.IRegisterAllocator;
import org.teachfx.antlr4.ep18r.stackvm.codegen.LinearScanAllocator;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EP18RRegisterAllocatorAdapter implements org.teachfx.antlr4.ep21.pass.codegen.IRegisterAllocator {

    private final IRegisterAllocator ep18rAllocator;
private final Map<VariableSymbol, String> variableToName;
    private final Map<String, VariableSymbol> nameToVariable;
    private int nextVariableId;

    public EP18RRegisterAllocatorAdapter(IRegisterAllocator ep18rAllocator) {
        if (ep18rAllocator == null) {
            throw new IllegalArgumentException("EP18R register allocator cannot be null");
        }
        this.ep18rAllocator = ep18rAllocator;
        this.variableToName = new ConcurrentHashMap<>();
        this.nameToVariable = new ConcurrentHashMap<>();
        this.nextVariableId = 1;
    }

    @Override
    public int allocateRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        return ep18rAllocator.allocate(varName);
    }

    @Override
    public int getStackOffset(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        return ep18rAllocator.getSpillSlot(varName);
    }

    @Override
    public void reset() {
        ep18rAllocator.reset();
        variableToName.clear();
        nameToVariable.clear();
        nextVariableId = 1;
    }

    @Override
    public int getAllocatedRegisterCount() {
        return ep18rAllocator.getAllocatedRegisterCount();
    }

    @Override
    public int getRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        return ep18rAllocator.getRegister(varName);
    }

    @Override
    public boolean isSpilled(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        return ep18rAllocator.isSpilled(varName);
    }

    @Override
    public void freeRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        String varName = getVariableName(variable);
        ep18rAllocator.free(varName);
    }

    public IRegisterAllocator getEp18rAllocator() {
        return ep18rAllocator;
    }

    private String getVariableName(VariableSymbol variable) {
        if (variableToName.containsKey(variable)) {
            return variableToName.get(variable);
        }

        String name;
        if (variable.getName() != null) {
            name = variable.getName();
        } else {
            name = "var" + nextVariableId++;
        }

        variableToName.put(variable, name);
        nameToVariable.put(name, variable);
        return name;
    }

    public VariableSymbol getVariableSymbol(String name) {
        return nameToVariable.get(name);
    }

    public java.util.Set<VariableSymbol> getManagedVariables() {
        return java.util.Collections.unmodifiableSet(variableToName.keySet());
    }

    public String generateAllocationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== EP18R Register Allocator Adapter Report ===\n");
        report.append("Managed variables: ").append(variableToName.size()).append("\n");
        report.append("Allocated registers: ").append(getAllocatedRegisterCount()).append("\n");
        report.append("\n");

        if (ep18rAllocator instanceof LinearScanAllocator linearAllocator) {
            report.append("EP18R Allocation Details:\n");
            report.append(linearAllocator.generateAllocationReport());
        }

        return report.toString();
    }

    @Override
    public String toString() {
        return "EP18RRegisterAllocatorAdapter{" +
                "managedVariables=" + variableToName.size() +
                ", allocatedRegisters=" + getAllocatedRegisterCount() +
                '}';
    }
}