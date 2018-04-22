#!/bin/bash

c="$1"
r="$2"

if [ "$r" == "" ]
then
  echo need two parameters
  echo 'first is directory where all the conll2003 tgz/tar files are stored (as downloaded)'
  echo second is directory where the Reuters corpus is stored as a collection of zip files
  exit 1
fi

## make the paths to the directories absolute
c=`cd "$c"; pwd -P`
r=`cd "$r"; pwd -P`

echo CoNLL 2003 directory is $c
echo Reuters directory is $r

mkdir work 
pushd work
tar xvf "$c"/ner.tgz

# fix the script to use the correct english corpus
sed -i ner/bin/make.eng -e "s|/mnt/cdrom|$r|g"
chmod 755 ner/bin/make.eng
popd

pushd work/ner
echo Creating English files
./bin/make.eng
popd

# copying the files to the conll2003-eng directory
mkdir conll2003-eng
cp work/ner/eng.train conll2003-eng
cp work/ner/eng.testb conll2003-eng
# for some reason, this one contains one line too many so we remove it
head -55044 < work/ner/eng.testa > conll2003-eng/eng.testa

echo 'Finished, the files should now be in ./conll2003-eng'
echo 'If everything worked OK, you can remove the temporary file ./work now'


