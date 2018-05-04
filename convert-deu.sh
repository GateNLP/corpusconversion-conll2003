#!/bin/bash

## This expects the preparation script to have run
## but lets check first

if [ ! -f ./conll2003-deu/deu.train ]
then 
  echo 'Prepared files not found, did you run ./prepare-deu.sh or provide the conll-format files?'
  exit 1
fi
mkdir -p conll2003-gate-deu/train
mkdir -p conll2003-gate-deu/testa
mkdir -p conll2003-gate-deu/testb


iconv -f iso-8859-1 -t utf-8  conll2003-deu/deu.train | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-deu/train finf
iconv -f iso-8859-1 -t utf-8  conll2003-deu/deu.testa | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-deu/testa finf
iconv -f iso-8859-1 -t utf-8  conll2003-deu/deu.testb | \
  scala -nobootcp -nc -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2003toGate.scala conll2003-gate-deu/testb finf

