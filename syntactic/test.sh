#!/bin/bash

for file in `ls ref/error/*.ast`
do
    echo "================================================================"
    name=${file%.*}
    echo $file
    java -jar ../LCPLSemant.jar $file in.run
    java -jar LCPLSemant.jar $file out.run
    #java -jar LCPLSemant.jar in.run --run >in.res
    #java -jar LCPLSemant.jar out.run --run >out.res
    diff -b in.run out.run
    echo $? 1>&2
done

for file in `ls ref/simple/*.ast`
do
    echo "================================================================"
    name=${file%.*}
    echo $file
    java -jar ../LCPLSemant.jar $file in.run
    java -jar LCPLSemant.jar $file out.run
    #java -jar LCPLSemant.jar in.run --run >in.res
    #java -jar LCPLSemant.jar out.run --run >out.res
    diff -b in.run out.run
    echo $? 1>&2
done

for file in `ls ref/advanced/*.ast`
do
    echo "================================================================"
    name=${file%.*}
    echo $file
    java -jar ../LCPLSemant.jar $file in.run
    java -jar LCPLSemant.jar $file out.run
    #java -jar LCPLSemant.jar in.run --run >in.res
    #java -jar LCPLSemant.jar out.run --run >out.res
    diff -b in.run out.run
    echo $? 1>&2
done

for file in `ls ref/complex/*.ast`
do
    echo "================================================================"
    name=${file%.*}
    echo $file
    java -jar ../LCPLSemant.jar $file in.run
    java -jar LCPLSemant.jar $file out.run
    #java -jar LCPLSemant.jar in.run --run >in.res
    #java -jar LCPLSemant.jar out.run --run >out.res
    diff -b in.run out.run
    echo $? 1>&2
done
