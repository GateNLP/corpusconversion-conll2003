# Tools to convert the CoNLL2003 NER corpora to GATE format

This includes both modified scripts to do the initial merge of text and conll
annotations (the original scripts are meant to be used with a CD mounted 
at some specific mount point) and the conversion from CoNLL format to GATE finf.

This will use the updated 2006 annotations for German! 

The script prepare.sh expects three parameters for the  locations of the following files
First parameter for directory of
* ner.tgz from CoNLL 2003
* eng.raw.tar from CoNLL 2003
* deu.raw.tar from CoNLL 2003
Second parameter for directory of
* LDC94T5.tgz for the actual texts
Third parameter for directory of
* Reuters files <date>.zip

The tools will extract from these original locations into ./work 
and then processing will use the files in there, so there must be
enough disk space to temporarily hold all the files in ./work.
After the conversion is completed, ./work can be removed (but it is
not removed automatically).

If the script prepare.sh is run and a directory work already exists it is assumed 
to already contain the correctly expanded files and it will be used as is.

## Steps 

* run ./prepare.sh dir1 dir2 dir3 where dir1 contains the conll2003 archives dir2 contains the LDC94T5 archive and dir3 contains the Reuters zip files
* this creates work/ner/deu.{train,testa,testb} and work/ner/eng.{train,testa,testb}
* NOTE: there will be a message about an incorrect number of lines in data files, but it seems this is just because of an added empty line at the end of eng.testa
* Run ./convert.sh : this will create a directory deu and a directory eng with subdirectories train, test and testb each and populate the directories with the GATE files.

Each of the result directories contains one GATE document in GATE XML format for each document identified in the corresponding input file. 

The following annotations are placed into the annotation set "Original markups":
* LOC, MISC, ORG, PER: for the entity annotations from the input. These annotations have the single feature startLineNr which identifies the (1-based) number of the original CoNLL input file where this entity started
* Token: for each input token one annotation is created. It contains the following features:
  * chunkBIO: the original BIO value of the column for chunks
  * lemma: the original value of the lemma columns (German only)
  * lineNr: the line number (1-based) of that token in the original CoNLL input file as generated by the script described above
  * neBIO: the original BIO value of the NE column
  * pos: the original value of the POS column

The conversion algorithm separates all tokens by spaces except there are no spaces before punctuation caracters !,-.:;? and 
there are no spaces after opening parentheses ({[ and before closing parentheses )}]
There is no space inserted before token 's but there is space inserted before a single ' because we cannot know if it is
the genetive of a plural or just used for quoting or something other.
For quote characters " a space is inserted before but not after all odd occurrences in a document and after but not before
all even occurrences.

All other tokens, including quote-like characters like '`, or characters like $£#~% if separate tokens in the input file are separated by 
spaces.
No space is added at the beginning or end of a sentence or beginning or end of a document.
