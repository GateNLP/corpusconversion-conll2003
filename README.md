# Tools to convert the CoNLL2003 NER corpora to GATE format

This includes both modified scripts to do the initial merge of text and conll
annotations (the original scripts are meant to be used with a CD mounted 
at some specific mount point) and the conversion from CoNLL format to GATE finf.

This will use the updated 2006 annotations for german

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

* run prepare.sh dir1 dir2 dir3 where dir1 contains the conll2003 archives dir2 contains the LDC94T5 archive and dir3 contains the Reuters zip files
* this creates work/ner/deu.{train,testa,testb} and work/ner/eng.{train,testa,testb}
* NOTE: there will be a message about an incorrect number of lines in data files, but it seems this is just because of an added empty line at the end of eng.testa
