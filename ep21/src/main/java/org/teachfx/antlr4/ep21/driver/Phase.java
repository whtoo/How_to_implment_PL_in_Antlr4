package org.teachfx.antlr4.ep21.driver;

import java.util.Optional;

public abstract class Phase<Input,Output> implements Task<Input,Output>,ErrorIssuer {

    public final String name;

    public Phase(String name) {
        this.name = name;
    }

    public abstract Output transform(Input input);

    public void onSucceed(Output output) {}

    @Override
    public Optional<Output> apply(Input input) {
        var out = transform(input);
        if (hasError()) {

            printErrors(System.out);

            return Optional.empty();
        }

        return Optional.of(out);
    }
}
