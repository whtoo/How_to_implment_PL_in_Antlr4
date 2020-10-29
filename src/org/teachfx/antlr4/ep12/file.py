#!/usr/bin/python3
# -*- coding: UTF-8 -*-
import re;
from string import Template
 
# 打开一个文件
file = open("BuildAstVisitor.java", "r")
try:
    text_lines = file.readlines()
    for line in text_lines:
        m = re.search('public class (\w+)',line)
        if(m is not None and 'Build' not in m.group(0)):
           clzName =  m.group(1)
           print(clzName)
        #    create_file = open(clzName+".java",'x')
        #    template_str = """
        #    package org.teachfx.antlr4.ep12;
        #    public class $clz {

        #    }
        #    """
        #    s = Template(template_str)
        #    create_file.write(s.substitute({'clz':clzName}))
        #    create_file.close()
    
finally:
    file.close


