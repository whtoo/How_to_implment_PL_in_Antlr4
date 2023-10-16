#!/bin/sh

# skip exclude dir within cp
shopt -s extglob

mkdir "$2"
source="$1/!(*.md)"
target="$2"

cp -r $source $target

grep --exclude-dir="${target}/target" .-rl  "org.teachfx.antlr4.$1" "${target}" | xargs sed -ie "s/org.teachfx.antlr4.$1/org.teachfx.antlr4.$2/g"

find ${target} -iname "*.*e" -exec rm -r {} \;