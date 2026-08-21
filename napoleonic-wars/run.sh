#!/bin/bash
cd "$(dirname "$0")"

# Компиляция всех исходных файлов
echo "Compiling..."
mkdir -p out
javac -d out src/config/*.java src/map/*.java src/army/*.java src/battle/*.java src/game/*.java src/Main.java 2>&1

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Компиляция UI
javac -cp out -d out src/ui/*.java 2>&1

if [ $? -ne 0 ]; then
    echo "UI compilation failed!"
    exit 1
fi

echo "Compilation successful!"
echo ""
echo "To run console version: java -cp out Main"
echo "To run GUI version: java -cp out ui.GameWindow"
