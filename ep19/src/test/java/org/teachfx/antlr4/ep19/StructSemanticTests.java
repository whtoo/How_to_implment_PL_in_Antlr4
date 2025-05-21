package org.teachfx.antlr4.ep19;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.teachfx.antlr4.ep19.misc.CompilerLogger;
import org.teachfx.antlr4.ep19.Compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class StructSemanticTests {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private Locale defaultLocale;


    @BeforeEach
    public void setUpStreamsAndLogger() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        CompilerLogger.INSTANCE.clear(); // Clear any previous errors
        // Fix locale for consistent float printing
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    public void restoreStreamsAndLogger() {
        System.setOut(originalOut);
        System.setErr(originalErr);
        CompilerLogger.INSTANCE.clear(); // Clear errors after test
        Locale.setDefault(defaultLocale);
    }

    private Path createTempSourceFile(String baseName, String content) throws IOException {
        Path tempFile = Files.createTempFile(baseName + "_", ".sm");
        Files.writeString(tempFile, content);
        return tempFile;
    }
    
    private boolean compileAndTypeCheck(String sourceCode, String testName) throws Exception {
        Path tempFile = createTempSourceFile(testName, sourceCode);
        boolean success = Compiler.doCompile(tempFile.toString(), false, false, false);
        Files.deleteIfExists(tempFile); 
        return success;
    }
    
    private boolean compileAndExecute(String sourceCode, String testName) throws Exception {
        Path tempFile = createTempSourceFile(testName, sourceCode);
        boolean success = Compiler.doCompile(tempFile.toString(), false, true, false);
        Files.deleteIfExists(tempFile); 
        return success;
    }

    private String getOutput() {
        return outContent.toString().replace("\r\n", "\n");
    }

    // --- Test Cases ---

    // 1. Struct Declaration and Instantiation
    @Test
    void testBasicStructInstantiation_TypeCheck() throws Exception {
        String code = """
                struct Point { int x; int y; }
                void main() {
                    Point p;
                    p = new Point;
                }
                """;
        assertTrue(compileAndTypeCheck(code, "basicStructTC"), "Basic struct instantiation should type check.");
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount(), "Should be no type errors.");
    }

    @Test
    void testBasicStructInstantiation_Runtime() throws Exception {
        String code = """
                struct Point { int x; int y; }
                void main() {
                    Point p;
                    p = new Point;
                    p.x = 10;
                    p.y = 20;
                    print(p.x);
                    print(p.y);
                }
                """;
        assertTrue(compileAndExecute(code, "basicStructRT"), "Basic struct instantiation and field access should execute.");
        assertEquals("10\n20\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount(), "Should be no runtime errors logged by CompilerLogger.");
    }
    
    @Test
    void testTypedefStructInstantiation_TypeCheck() throws Exception {
        String code = """
                struct Vector2D { float x; float y; }
                typedef Vector2D Vec2;
                void main() {
                    Vec2 v;
                    v = new Vec2; // Instantiation using typedef name
                }
                """;
        assertTrue(compileAndTypeCheck(code, "typedefStructTC"), "Struct instantiation via typedef should type check.");
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount(), "Should be no type errors.");
    }

    @Test
    void testTypedefStructInstantiation_Runtime() throws Exception {
        String code = """
                struct Vector2D { float x; float y; }
                typedef Vector2D Vec2;
                void main() {
                    Vec2 v;
                    v = new Vec2;
                    v.x = 1.5;
                    v.y = 2.5;
                    print(v.x);
                    print(v.y);
                }
                """;
        assertTrue(compileAndExecute(code, "typedefStructRT"), "Struct instantiation via typedef and field access should execute.");
        assertEquals("1.5\n2.5\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    // 2. Struct Field Access
    @Test
    void testValidFieldAccess_TypeCheck() throws Exception {
        String code = """
                struct Test { int a; float b; }
                void main() {
                    Test t;
                    t = new Test;
                    int val_a;
                    float val_b;
                    val_a = t.a;
                    val_b = t.b;
                }
                """;
        assertTrue(compileAndTypeCheck(code, "validFieldAccessTC"), "Valid field access should type check.");
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }
    
    @Test
    void testNonExistentFieldAccess_TypeCheck() throws Exception {
        String code = """
                struct Test { int a; }
                void main() {
                    Test t;
                    t = new Test;
                    int val_c;
                    val_c = t.c; // 'c' does not exist
                }
                """;
        assertFalse(compileAndTypeCheck(code, "nonExistentFieldTC"), "Accessing non-existent field should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0, "Error should be logged for non-existent field.");
    }

    @Test
    void testFieldAccessOnTypedefStruct_Runtime() throws Exception {
        String code = """
                struct Data { char c; }
                typedef Data MyData;
                void main() {
                    MyData md;
                    md = new MyData;
                    md.c = 'X';
                    print(md.c);
                }
                """;
        assertTrue(compileAndExecute(code, "fieldAccessTypedefRT"));
        assertEquals("X\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testFieldAssignmentAndUpdate_Runtime() throws Exception {
        String code = """
                struct Box { int value; }
                void main() {
                    Box b;
                    b = new Box;
                    b.value = 100;
                    print(b.value);
                    b.value = 200;
                    print(b.value);
                }
                """;
        assertTrue(compileAndExecute(code, "fieldAssignUpdateRT"));
        assertEquals("100\n200\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testIncompatibleTypeFieldAssignment_TypeCheck() throws Exception {
        String code = """
                struct Item { int id; string name; }
                void main() {
                    Item item;
                    item = new Item;
                    item.id = "wrong_type"; // Assigning string to int field
                }
                """;
        assertFalse(compileAndTypeCheck(code, "incompatFieldAssignTC"), "Assigning incompatible type to field should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }

    // 3. Struct Method Calls
    @Test
    void testMethodCallNoParams_Runtime() throws Exception {
        String code = """
                struct Greeter {
                    string message;
                    void init() {
                        this.message = "Hello";
                    }
                    void sayHi() {
                        print(this.message);
                    }
                }
                void main() {
                    Greeter g;
                    g = new Greeter;
                    g.init();
                    g.sayHi();
                }
                """;
        assertTrue(compileAndExecute(code, "methodNoParamRT"));
        assertEquals("Hello\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testMethodCallWithParams_Runtime() throws Exception {
        String code = """
                struct Adder {
                    int base;
                    void setBase(int b) {
                        this.base = b;
                    }
                    int add(int x) {
                        return this.base + x;
                    }
                }
                void main() {
                    Adder calc;
                    calc = new Adder;
                    calc.setBase(10);
                    int result;
                    result = calc.add(5);
                    print(result);
                }
                """;
        assertTrue(compileAndExecute(code, "methodWithParamRT"));
        assertEquals("15\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testMethodCallIncorrectParamType_TypeCheck() throws Exception {
        String code = """
                struct Multiplier {
                    void multiply(int a, int b) { print(a*b); }
                }
                void main() {
                    Multiplier m;
                    m = new Multiplier;
                    m.multiply(5, "not_an_int");
                }
                """;
        assertFalse(compileAndTypeCheck(code, "methodIncorrectParamTypeTC"), "Method call with incorrect param type should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }
    
    @Test
    void testMethodCallIncorrectArgCount_TooFew_TypeCheck() throws Exception {
        String code = """
                struct Calculator {
                    int add(int a, int b) { return a + b; }
                }
                void main() {
                    Calculator calc;
                    calc = new Calculator;
                    calc.add(5); // Too few arguments
                }
                """;
        assertFalse(compileAndTypeCheck(code, "methodTooFewArgsTC"), "Method call with too few arguments should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }

    @Test
    void testMethodCallIncorrectArgCount_TooMany_TypeCheck() throws Exception {
        String code = """
                struct Simple { void doIt() { print("done"); } }
                void main() {
                    Simple s;
                    s = new Simple;
                    s.doIt(123); // Too many arguments
                }
                """;
        assertFalse(compileAndTypeCheck(code, "methodTooManyArgsTC"), "Method call with too many arguments should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }


    @Test
    void testMethodCallReturnValue_Runtime() throws Exception {
        String code = """
                struct ValueProvider {
                    int getValue() { return 42; }
                }
                void main() {
                    ValueProvider vp;
                    vp = new ValueProvider;
                    int v;
                    v = vp.getValue();
                    print(v);
                }
                """;
        assertTrue(compileAndExecute(code, "methodReturnRT"));
        assertEquals("42\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testMethodAccessModifyFields_Runtime() throws Exception {
        String code = """
                struct Counter {
                    int count;
                    void init() { this.count = 0; }
                    void increment() { this.count = this.count + 1; }
                    int get() { return this.count; }
                }
                void main() {
                    Counter c;
                    c = new Counter;
                    c.init();
                    c.increment();
                    c.increment();
                    print(c.get());
                }
                """;
        assertTrue(compileAndExecute(code, "methodModifyFieldRT"));
        assertEquals("2\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testNonExistentMethodCall_TypeCheck() throws Exception {
        String code = """
                struct Test { int a; }
                void main() {
                    Test t;
                    t = new Test;
                    t.nonExistentMethod();
                }
                """;
        assertFalse(compileAndTypeCheck(code, "nonExistentMethodTC"), "Calling non-existent method should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }

    @Test
    void testMethodCallOnTypedefStruct_Runtime() throws Exception {
        String code = """
                struct Power {
                    int raise(int base, int exp) {
                        int res; res = 1;
                        int i; i = 0;
                        while(i < exp) {
                           res = res * base;
                           i = i + 1;
                        }
                        return res;
                    }
                }
                typedef Power MyPower;
                void main() {
                    MyPower mp;
                    mp = new MyPower;
                    print(mp.raise(2,3));
                }
                """;
        assertTrue(compileAndExecute(code, "methodTypedefRT"));
        assertEquals("8\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    // 4. Error Scenarios (Type Checker)

    @Test
    void testFieldAccessOnNonStruct_TypeCheck() throws Exception {
        String code = """
                void main() {
                    int i;
                    i = 10;
                    print(i.field); // 'i' is not a struct
                }
                """;
        assertFalse(compileAndTypeCheck(code, "fieldAccessNonStructTC"), "Field access on non-struct type should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }

    @Test
    void testMethodCallOnNonStruct_TypeCheck() throws Exception {
        String code = """
                void main() {
                    float f;
                    f = 3.14;
                    f.someMethod(); // 'f' is not a struct
                }
                """;
        assertFalse(compileAndTypeCheck(code, "methodCallNonStructTC"), "Method call on non-struct type should fail type check.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0);
    }

    // 5. Nested Structs (Type Checking and Runtime)
    @Test
    void testNestedStructDeclarationAndAccess_TypeCheck() throws Exception {
        String code = """
                struct Outer {
                    int id;
                    struct Inner {
                        string name;
                    }
                    Inner inner_instance; 
                }
                void main() {
                    Outer o;
                    o = new Outer;
                    o.id = 1;
                    // Direct instantiation of nested struct type might be tricky depending on language design
                    // Assuming 'Inner' type is accessible if Outer is in scope, or through Outer.Inner
                    // For now, let's test access to a field of that type.
                    // o.inner_instance = new Outer.Inner; // If nested type name is Outer.Inner
                    // o.inner_instance = new Inner; // If Inner is globally visible or resolved within Outer's scope
                    // The provided grammar does not seem to support `new Outer.Inner`.
                    // Let's assume the field `inner_instance` is of a separately defined struct for this test,
                    // or the grammar allows `struct Inner` to be globally visible.
                    // For this test, we will assume `Inner` is defined elsewhere or implicitly available.
                    // This test will focus on whether Outer.inner_instance.name is type-checked correctly
                    // if `inner_instance`'s type `Inner` is known.
                    // The grammar seems to declare structs at global level only.
                    // So, true nested struct types (Inner only visible via Outer) are not supported.
                    // We'll simulate by having two distinct structs.
                    struct InnerSim { string name; }
                    struct OuterSim { int id; InnerSim inner_field; }
                    
                    OuterSim os;
                    os = new OuterSim;
                    os.id = 100;
                    os.inner_field = new InnerSim; // Must instantiate the inner field
                    string temp;
                    temp = os.inner_field.name; 
                }
                """;
        assertTrue(compileAndTypeCheck(code, "nestedStructAccessTC"), "Accessing fields of a struct within another struct should type check.");
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testNestedStruct_Runtime() throws Exception {
        String code = """
                struct Inner { string data; }
                struct Outer { Inner n; int val; }
                void main() {
                    Outer o;
                    o = new Outer;
                    o.n = new Inner; // Important: inner struct must be instantiated
                    o.n.data = "nested hello";
                    o.val = 123;
                    print(o.n.data);
                    print(o.val);
                }
                """;
        assertTrue(compileAndExecute(code, "nestedStructRT"));
        assertEquals("nested hello\n123\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    // 6. Structs as Parameters and Return Types
    @Test
    void testStructAsParameter_Runtime() throws Exception {
        String code = """
                struct Point { int x; int y; }
                void printPoint(Point p_param) {
                    print(p_param.x);
                    print(p_param.y);
                }
                void main() {
                    Point myPt;
                    myPt = new Point;
                    myPt.x = 3;
                    myPt.y = 4;
                    printPoint(myPt);
                }
                """;
        assertTrue(compileAndExecute(code, "structAsParamRT"));
        assertEquals("3\n4\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }

    @Test
    void testStructAsReturnValue_Runtime() throws Exception {
        String code = """
                struct Dimensions { int w; int h; }
                Dimensions createDims(int width, int height) {
                    Dimensions d;
                    d = new Dimensions;
                    d.w = width;
                    d.h = height;
                    return d;
                }
                void main() {
                    Dimensions box_dims;
                    box_dims = createDims(10, 20);
                    print(box_dims.w);
                    print(box_dims.h);
                }
                """;
        assertTrue(compileAndExecute(code, "structAsReturnRT"));
        assertEquals("10\n20\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }
    
    @Test
    void testStructMethodReturningStruct_Runtime() throws Exception {
        String code = """
            struct Coord { int x; int y; }
            struct Mover {
                Coord current_pos;
                void init(int x, int y) {
                    this.current_pos = new Coord;
                    this.current_pos.x = x;
                    this.current_pos.y = y;
                }
                Coord getPos() {
                    return this.current_pos;
                }
                void move(int dx, int dy) {
                    this.current_pos.x = this.current_pos.x + dx;
                    this.current_pos.y = this.current_pos.y + dy;
                }
            }
            void main() {
                Mover m;
                m = new Mover;
                m.init(1,2);
                m.move(5,10);
                Coord final_pos;
                final_pos = m.getPos();
                print(final_pos.x);
                print(final_pos.y);
            }
            """;
        assertTrue(compileAndExecute(code, "structMethodReturnStructRT"));
        assertEquals("6\n12\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }


    @Test
    void testPassStructToMethodOfAnotherStruct_Runtime() throws Exception {
        String code = """
            struct Vector { float x; float y; }
            struct Shape {
                Vector center;
                void init() { this.center = new Vector; }
                void setCenter(Vector c_vec) {
                    this.center.x = c_vec.x;
                    this.center.y = c_vec.y;
                }
                void printCenter() {
                    print(this.center.x);
                    print(this.center.y);
                }
            }
            void main() {
                Shape s;
                s = new Shape;
                s.init();
                
                Vector new_c;
                new_c = new Vector;
                new_c.x = 7.7;
                new_c.y = 8.8;
                
                s.setCenter(new_c);
                s.printCenter();
            }
            """;
        assertTrue(compileAndExecute(code, "passStructToMethodRT"));
        assertEquals("7.7\n8.8\n", getOutput());
        assertEquals(0, CompilerLogger.INSTANCE.getErrorCount());
    }
    
    @Test
    void testAssignNullToStructVarThenAccessField_RuntimeError() throws Exception {
        // This test depends on whether 'null' is a concept for struct variables
        // and if the runtime checks for null pointers before field access.
        // Cymbol grammar may not have explicit 'null'.
        // If uninitialized structs are not new'd, they might be implicitly null or cause error on access.
        // Let's assume a variable is declared but not initialized with 'new'.
        String code = """
                struct Data { int val; }
                void main() {
                    Data d; 
                    // d is not initialized with 'new Data'
                    // Behavior depends on default initialization or runtime checks.
                    // If default is null-like and no check -> crash (hard to test specific error)
                    // If runtime check -> specific error
                    // If auto-initialized to default values -> no error
                    // Let's assume for now it would lead to an error if fields are accessed.
                    // The current interpreter seems to initialize fields to default (0, null for string etc.)
                    // if not explicitly assigned, but the StructInstance itself needs `new`.
                    // Accessing `d.val` without `d = new Data` should cause an error if `d` is null.
                    // The TypeChecker might not catch this if `d` is considered validly declared.
                    // The Interpreter should catch it.
                    // Update: The Interpreter's visitPrimaryID for a variable not in MemorySpace
                    // (or default initialized to Java null) might cause NullPointerException
                    // or a custom error. Let's see what CompilerLogger catches.
                    print(d.val); 
                }
                """;
        // This test might be tricky. If `d` is not initialized, `visitPrimaryID` might return `null` for `d`.
        // Then `visitExprStructFieldAccess` would receive `null` as `structInstanceObj`.
        // The check `if (!(structInstanceObj instanceof StructInstance))` should catch this.
        assertFalse(compileAndExecute(code, "assignNullAccessFieldRT"), "Accessing field on uninitialized struct should fail at runtime.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0, "Error should be logged for accessing field on uninitialized struct.");
        // Example error message: "表达式 'd' (类型: null) 不是一个结构体实例。"
    }

     @Test
    void testAssignNullToStructVarThenCallMethod_RuntimeError() throws Exception {
        String code = """
                struct Worker { void doWork() { print("working"); } }
                void main() {
                    Worker w;
                    // w is not initialized with 'new Worker'
                    w.doWork();
                }
                """;
        assertFalse(compileAndExecute(code, "assignNullCallMethodRT"), "Calling method on uninitialized struct should fail at runtime.");
        assertTrue(CompilerLogger.INSTANCE.getErrorCount() > 0, "Error should be logged for calling method on uninitialized struct.");
    }
}
