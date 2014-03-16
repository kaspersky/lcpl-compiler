#!/bin/sh

# Fisierele sursa trebuie sa fie intr-un director lcpl
# cu 2 subdirectoare simple si advanced
# Fisierele ast rezultat trebuie sa fie in directorul
# ast cu aceleasi subdirectoare.
# De ex., se poate dezarhiva testele publice chiar in
# directorul curent.

# Pentru fiecare test, rezultatele se compara cu comanda
# "diff -b" (brief)

for f in `ls lcpl/simple/`
do
    dir=$(dirname "$f")
    filename=$(basename "$f")
    name="${filename%.*}"
    echo "Processing $f"

    java -cp "lib/*:./bin" LCPLParser lcpl/simple/$f out
    diff -b "ast/simple/$name.ast" out
    echo "=================================================================================================="
done

for f in `ls lcpl/advanced/`
do
    dir=$(dirname "$f")
    filename=$(basename "$f")
    name="${filename%.*}"
    echo "Processing $f"

    java -cp "lib/*:./bin" LCPLParser lcpl/advanced/$f out
    diff -b "ast/advanced/$name.ast" out
    echo "=================================================================================================="
done

rm -rf out

exit 0
