# labeled Expr

## Labels

- MUL : '*'
- ADD : '+'
- SUB : '-'
- DIV : '/'

```java
        String inputFile = null;
        if (args.length > 0) inputFile = args[0];
        InputStream is = System.in;
        if (inputFile != null) is = new FileInputStream(inputFile);
        ANTLRInputStream input = new ANTLRInputStream(is);
```
