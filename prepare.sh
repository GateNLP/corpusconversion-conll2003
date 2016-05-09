#!/bin/bash

c="$1"
l="$2"
r="$3"

if [ "$r" == "" ]
then
  echo need three parameters
  echo first is directory where all the conll2003 tgz/tar files are stored
  echo second is directory where the LDC94T5.tgz is stored
  echo third is directory where the Reuters corpus is stored as a collection of zip files
  exit 1
fi

## make the paths to the directories absolute
c=`cd "$c"; pwd -P`
l=`cd "$l"; pwd -P`
r=`cd "$r"; pwd -P`

echo CoNLL 2003 directory is $c
echo LDC94T5 directory is $l
echo Reuters directory is $r

## if work already exists we assume we already have extracted everything correctly
if [ ! -d work ] 
then
  mkdir work 
  pushd work
  tar xvf "$c"/ner.tgz
  tar xvf "$c"/eng.raw.tar
  tar xvf "$c"/deu.raw.tar
  tar xvf "$l"/LDC94T5.tgz
  popd
fi

## Get location of German corpus
deu=`cd work/eci_multilang_txt/data/eci1/ger03/; pwd -P`
deu=$deu/ger03b05.eci

echo German corpus file is $deu

# fix the conll2003 script to use the correct german corpus
sed -i work/ner/bin/make.deu -e "s|/mnt/cdrom/data/eci1/ger03/ger03b05.eci|$deu|g"

# fix the script to use the correct english corpus
sed -i work/ner/bin/make.eng -e "s|/mnt/cdrom|$r|g"

## We want the better 2006 annotation for German, so we copy the 2006 annotations over
pushd work/ner
cp ./etc.2006/tags.deu ./etc/tags.deu

chmod 755 bin/make.deu
chmod 755 bin/make.eng

echo Creating German files
./bin/make.deu
popd
pushd work/ner
echo Creating English files
./bin/make.eng

popd



