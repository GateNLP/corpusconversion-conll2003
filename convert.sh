#!/bin/bash

## will have to be something like
iconv -f iso-8859-1 -t utf-8  work/ner/deu.testb | scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala
