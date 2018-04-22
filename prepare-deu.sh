#!/bin/bash

echo 'ERROR: this script does not work at the moment!'
exit 1

c="$1"
l="$2"

if [ "$l" == "" ]
then
  echo need two parameters
  echo first is directory where all the conll2003 tgz/tar files are stored
  echo second is directory where the LDC94T5.tgz is stored
  exit 1
fi

## make the paths to the directories absolute
c=`cd "$c"; pwd -P`
l=`cd "$l"; pwd -P`

echo CoNLL 2003 directory is $c
echo LDC94T5 directory is $l

mkdir work 
pushd work
tar xvf "$c"/ner.tgz
tar xvf "$l"/LDC94T5.tgz eci_multilang_txt/data/eci1/ger03/ger03b05.eci
popd

## Get location of German corpus
deu=`cd work/eci_multilang_txt/data/eci1/ger03/; pwd -P`
deu=$deu/ger03b05.eci

echo German corpus file is $deu

# fix the conll2003 script to use the correct german corpus
sed -i work/ner/bin/make.deu -e "s|/mnt/cdrom/data/eci1/ger03/ger03b05.eci|$deu|g"

## We want the better 2006 annotation for German, so we copy the 2006 annotations over
pushd work/ner
cp ./etc.2006/tags.deu ./etc/tags.deu

chmod 755 bin/make.deu

echo Creating German files
./bin/make.deu
popd



