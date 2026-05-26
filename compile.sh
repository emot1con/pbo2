#!/bin/bash
# Download JDBC driver if not exists
if [ ! -f lib/postgresql-42.7.4.jar ]; then
    mkdir -p lib
    wget -O lib/postgresql-42.7.4.jar https://jdbc.postgresql.org/download/postgresql-42.7.4.jar
fi

# Clean previous output directory
rm -rf out
mkdir -p out

# Compile all Java files
echo "Compiling Java files..."
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d out @sources.txt
rm sources.txt

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
    exit 1
fi
