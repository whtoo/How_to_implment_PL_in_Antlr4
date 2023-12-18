import os

def is_utf8(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            f.read()
        return True
    except UnicodeDecodeError:
        return False
    except Exception as e:
        print(f"Error reading file: {file_path}")
        print(e)
        return False

def find_non_utf8_files(directory):
    non_utf8_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            file_path = os.path.join(root, file)
            if not is_utf8(file_path):
                non_utf8_files.append(file_path)
    return non_utf8_files

ep20_directory = "../ep20"
non_utf8_files = find_non_utf8_files(ep20_directory)

if len(non_utf8_files) > 0:
    print("以下文件不符合UTF-8规范：")
    for file in non_utf8_files:
        print(file)
else:
    print("所有文件都符合UTF-8规范。")