```mermaid
graph TD
subgraph L1
Q0["ret;"]
end
subgraph L0
Q1[".def dec1: args=1 ,locals=1;"]
Q2["t0 = @0;"]
Q3["t1 =  1 ;"]
Q4["t0 SUB t1;"]
Q5["jmp L1;"]
end
L0 --> L1
L0 --> L1

```
