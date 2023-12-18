import os
import shutil
import fileinput

# 源目录
src_dir = "../ep20/src"
# 目标目录
dest_dir = "../ep21/src"

# 拷贝源目录到目标目录，并将目录名从ep20改为ep21
shutil.copytree(src_dir, dest_dir, ignore=shutil.ignore_patterns("*.DS_Store", "*.class"))

# 遍历目标目录，将子目录中包含ep20的目录名改为ep21
for root, dirs, files in os.walk(dest_dir):
    for dir in dirs:
        if "ep20" in dir:
            old_dir_path = os.path.join(root, dir)
            new_dir_path = os.path.join(root, dir.replace("ep20", "ep21"))
            os.rename(old_dir_path, new_dir_path)

# 替换文件中的字符串
for root, dirs, files in os.walk(dest_dir):
    for file in files:
        file_path = os.path.join(root, file)
        with fileinput.FileInput(file_path, inplace=True) as f:
            for line in f:
                print(line.replace("org.teachfx.antlr4.ep20", "org.teachfx.antlr4.ep21"), end='')