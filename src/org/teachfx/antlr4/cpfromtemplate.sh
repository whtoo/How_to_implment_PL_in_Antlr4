#!/usr/bin/sh

grep -rl 'org.teachfx.antlr4.ep16' ep17 | xargs sed -ie "s/org.teachfx.antlr4.ep16/org.teachfx.antlr4.ep17/g"
find ep17/ -iname "*.javae" -exec rm -r {} \;