#!/bin/sh
# Compile and start PolarLog on Linux / macOS (needs JDK 8 or above)
mkdir -p out
javac -d out $(find src -name "*.java") || exit 1
java -cp out com.polarlog.app.Main "$@"
