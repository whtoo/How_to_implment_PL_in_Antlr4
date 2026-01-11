package org.teachfx.antlr4.ep18r.stackvm.interpreter;

import java.io.InputStream;

public interface IVirtualMachine {

    void exec() throws Exception;

    int getRegister(int regNum);

    void setRegister(int regNum, int value);

    void setTrace(boolean trace);

    VMConfig getConfig();

    byte[] getCode();
}
