#!/bin/bash

## This expects the preparation script to have run
## but lets check first

if [ ! -f ./conll2003-eng/eng.train ]
then 
  echo 'Prepared files not found, did you run ./prepare-eng.sh or provide the conll-format files?'
  exit 1
fi
mkdir -p conll2003-gate-eng/train
mkdir -p conll2003-gate-eng/testa
mkdir -p conll2003-gate-eng/testb


iconv -f iso-8859-1 -t utf-8  conll2003-eng/eng.train | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-eng/train
iconv -f iso-8859-1 -t utf-8  conll2003-eng/eng.testa | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-eng/testa
iconv -f iso-8859-1 -t utf-8  conll2003-eng/eng.testb | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-eng/testb

