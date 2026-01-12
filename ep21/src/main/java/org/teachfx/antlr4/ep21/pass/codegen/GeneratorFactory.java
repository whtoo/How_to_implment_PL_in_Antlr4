package org.teachfx.antlr4.ep21.pass.codegen;

import java.util.Map;

public class GeneratorFactory {

    private final Map<String, Object> config;

    public GeneratorFactory() {
        this.config = Map.of();
    }

    public GeneratorFactory(Map<String, Object> config) {
        this.config = config;
    }

    public ICodeGenerator createGenerator(VMTargetType targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("VM target type cannot be null");
        }

        ICodeGenerator generator = switch (targetType) {
            case STACK_VM -> new StackVMGenerator();
            case REGISTER_VM -> new RegisterVMGenerator();
        };

        if (generator != null && !config.isEmpty()) {
            generator.configure(config);
        }

        return generator;
    }

    public ICodeGenerator createGenerator(VMTargetType targetType, IEmitter emitter) {
        if (targetType == null) {
            throw new IllegalArgumentException("VM target type cannot be null");
        }

        ICodeGenerator generator = switch (targetType) {
            case STACK_VM -> new StackVMGenerator(emitter);
            case REGISTER_VM -> new RegisterVMGenerator();
        };

        if (generator != null && !config.isEmpty()) {
            generator.configure(config);
        }

        return generator;
    }
}
