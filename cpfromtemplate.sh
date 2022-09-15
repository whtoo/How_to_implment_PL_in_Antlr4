#!/bin/sh
echo "input two params representing souce_dir and target_dir"
# skip exclude dir within cp
shopt -s extglob
prefix="src/org/teachfx/antlr4/"
source="${prefix}$1/!(*.md)"
target="${prefix}$2/"

cp -r $source $target

grep --exclude=*jar .-rl "org.teachfx.antlr4.$1" ${target} | xargs sed -ie "s/org.teachfx.antlr4.$1/org.teachfx.antlr4.$2/g"
find ${target} -iname "*.*e" -exec rm -r {} \;