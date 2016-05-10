#!/bin/bash

## This expects the preparation script to have run
## but lets check first

if [ ! -f work/ner/deu.train ]
then 
  echo 'Prepared files not found, did your run ./prepare.sh?'
  exit 1
fi
mkdir -p conll2003-gate/deu/train
mkdir -p conll2003-gate/deu/testa
mkdir -p conll2003-gate/deu/testb
mkdir -p conll2003-gate/eng/train
mkdir -p conll2003-gate/eng/testa
mkdir -p conll2003-gate/eng/testb


iconv -f iso-8859-1 -t utf-8  work/ner/deu.train | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/deu/train
iconv -f iso-8859-1 -t utf-8  work/ner/deu.testa | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/deu/testa
iconv -f iso-8859-1 -t utf-8  work/ner/deu.testb | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/deu/testb

iconv -f iso-8859-1 -t utf-8  work/ner/eng.train | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/eng/train
iconv -f iso-8859-1 -t utf-8  work/ner/eng.testa | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/eng/testa
iconv -f iso-8859-1 -t utf-8  work/ner/eng.testb | \
  scala -cp ${GATE_HOME}/bin/gate.jar:${GATE_HOME}/lib/'*' conll2203toGate.scala conll2003-gate/eng/testb

