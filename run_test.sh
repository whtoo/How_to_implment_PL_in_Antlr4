#!/bin/bash
cd D:/How_to_implment_PL_in_Antlr4
mvn -q clean compile test-compile
mvn -q exec:java -pl ep18r -Dexec.mainClass="org.teachfx.antlr4.ep18r.SubTest"
