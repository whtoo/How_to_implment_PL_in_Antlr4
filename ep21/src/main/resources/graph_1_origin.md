```mermaid
graph TD
subgraph L9
Q0["t0 =  7 ;"]
Q1["jmp L3;"]
end
subgraph L8
Q2["t0 =  'break' ;"]
Q3["call print(args:1);"]
Q4["t0 = @0;"]
Q5["call dec1(args:1);"]
Q6["@0 = t0;"]
Q7["jmp L4;"]
end
subgraph L7
Q8["t0 = @0;"]
Q9["call print(args:1);"]
Q10["t0 = @0;"]
Q11["t1 =  7 ;"]
Q12["t0 EQ t1;"]
Q13["jmpIf t0,L9,L8;"]
end
subgraph L6
Q14["t0 =  0 ;"]
Q15["jmp L3;"]
end
subgraph L5
Q16["t0 = @0;"]
Q17["t1 =  5 ;"]
Q18["t0 GT t1;"]
Q19["jmpIf t0,L7,L8;"]
end
subgraph L4
Q20["t0 = @0;"]
Q21["t1 =  0 ;"]
Q22["t0 GT t1;"]
Q23["jmpIf t0,L5,L6;"]
end
subgraph L3
Q24["halt;"]
end
subgraph L2
Q25[".def main: args=0 ,locals=1;"]
Q26["t0 =  10 ;"]
Q27["@0 = t0;"]
Q28["jmp L4;"]
end
L2 --> L4
L2 --> L4
L4 --> L6
L4 --> L5
L5 --> L8
L5 --> L7
L7 --> L8
L7 --> L9
L9 --> L3
L9 --> L8
L8 --> L4
L8 --> L6
L6 --> L3
L6 --> L3

```
