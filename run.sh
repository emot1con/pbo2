#!/bin/bash
if [ ! -d out ]; then
    echo "Output directory 'out' not found. Please compile first using ./compile.sh"
    exit 1
fi

echo "Starting employee attendance system..."
java -cp "out:lib/*" Main
