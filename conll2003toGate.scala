import gate._
import java.io._

val patEmpty = "^\\s*$".r
// val patNewDoc = "^\\s*-DOCSTART-\\s+-X-\\s+.*$".r
// This docstart pattern should work for conll2003 and wikigold
val patNewDoc = "^\\s*-DOCSTART-\\s+.*$".r
val patLine = "\\s+"

val in = scala.io.Source.fromInputStream(System.in)

var content = new StringBuilder()

case class Token(from: Int, to: Int, lemma: String, pos: String, chunkBIO: String, neBIO: String, lineNr: Int) { }

case class NE(from: Int, to: Int, annType: String, startLineNr: Int) {}

case class Sentence(from: Int, to: Int) {}

// We use a list to store the tokens and we append at the beginning which is fast
// Order does not matter since we will later convert those into actual GATE annotations
var tokens = List[Token]()
var nes = List[NE]()
var sentences = List[Sentence]()

// TODO: count the NE annotations and try to sanity check them against 
// the original data files!!
if(args.size != 2) {
  System.err.println("Need two parameters: the output directory path, xml/finf")
  System.exit(1)
}

val outDir = new File(args(0))
val format = args(1)
if(!format.equals("xml") && !format.equals("finf")) {
  System.err.println("Second parameter must be xml or finf")
  System.exit(1)
}

Gate.init()
gate.Utils.loadPlugin("Format_FastInfoset")
val docExporter = Gate.getCreoleRegister()
                     .get("gate.corpora.FastInfosetExporter")
                     .getInstantiations().iterator().next()
                     .asInstanceOf[DocumentExporter]

var docNr = 0
var linenr = 0
var noSpace = true
var lastNe = "O"
var lastFrom = -1
var lastTo = -1
var lastLineNr = -1
var quoteOpen = true 
var lastSentenceStart = -1
in.getLines.foreach { line =>
  linenr += 1
  // first check if it is an empty line. If it is then add a new line character to the document
  // this is also the end of the sentence!
  if(!patEmpty.findFirstIn(line).isEmpty) {
    // we want to ignore the empty line after a doc start
    if(lastSentenceStart == -1) {
      lastSentenceStart = 0
    } else {
      // this is an empty line after something other than a docstart
      // add the sentence annotation
      sentences = Sentence(lastSentenceStart,content.size) :: sentences 
      // the new line is not part of the sentence and will get a split annotation
      content.append("\n");
      noSpace = true
      lastNe = "O"   
      lastSentenceStart = content.size
    }
  } else if(!patNewDoc.findFirstIn(line).isEmpty) {
    // if we do have a document, write it
    if(content.size > 0) {
      docNr+=1
      saveDoc(docNr,content.toString,tokens,nes,sentences,format)
    }
    content = new StringBuilder()
    tokens = List[Token]()
    nes = List[NE]()
    sentences = List[Sentence]()
    noSpace = true
    lastNe = "O"
    quoteOpen = true
    lastSentenceStart = -1
  } else { 
    // actual line with a token in it
    val fields = line.split(patLine,-1)
    //System.err.println("Got "+fields.size+" fields: "+fields.mkString(", "))
    // if we have 5 fields it must be german and the second field is a lemma, otherwise 
    // it must be english and the lemma is missing
    // if the current string is just punctuation or a closing parenthesis, then just append it otherwise
    // we first insert a separating space, except when the flag noSpace is set
    if(fields(0) != "\"" && !fields(0).matches("[.,:;\\-?!}\\])]") && !fields(0).matches("'s")) {
      if(!noSpace) {
        content.append(" ")
      } else {
        noSpace = false
      }
    } else if (fields(0) == "\"") {
      if(quoteOpen) {
        content.append(" ")
        noSpace = true
      } 
      quoteOpen = !quoteOpen
    }
    val from = content.size
    content.append(fields(0))
    // if we just added an opening parentheses, do not add a space later
    if(fields(0).matches("[\\-{\\[(]")) {
      noSpace=true
    }
    val to = content.size
    val thisNe =
      // Token(from: Int, to: Int, lemma: String, pos: String, chunkBIO: String, neBIO: String, lineNr: Int)
      if(fields.size==5) {
        // there is an error in the l\emma: for all forms of the german articles, the lemma is "d"!!
        tokens =  Token(from,to,fields(1),fields(2),fields(3),fields(4),linenr) :: tokens
        fields(4)
      } else if (fields.size==4) {
        tokens = Token(from,to,null,fields(1),fields(2),fields(3),linenr) :: tokens
        fields(3)
      } else if (fields.size==2) {
        tokens = Token(from,to,null,null,null,fields(1),linenr) :: tokens
        fields(1)
      } else {
        System.err.println("Error in line "+linenr+": number of fields is "+fields.size)
        ""
      }
    // now analyze the ne-bio: we expect either O or something starting with I-, B- or U-
    // followed by the actual type code.
    // If it is O and our lastNe  is not O, then we create an annotation from the lastFrom
    // to the lastTo for the respective type
    if(thisNe == "O" && lastNe != "O") {
      nes = NE(lastFrom,lastTo,lastNe.substring(2),lastLineNr) :: nes
      lastNe = "O"
      lastFrom = -1
      lastTo = -1
      lastLineNr = -1
    } else if(thisNe.startsWith("U-")) {
      // if thisNe starts with U- than this token by itself is an NE and we 
      // need to complete any previous one we may have
      if(lastNe != "O") {
        nes = NE(lastFrom,lastTo,lastNe.substring(2),lastLineNr) :: nes
        lastNe = "O"
        lastFrom = -1
        lastTo = -1
        lastLineNr = -1
      }
      nes = NE(from,to,thisNe.substring(2),linenr) :: nes
    } else if(thisNe.startsWith("I-") || thisNe.startsWith("B-")) {
      // if the last one was identical, only change lastTo
      // otherwise if the last one was not O, complete it
      if(lastNe == thisNe) {
        lastTo = to
      } else if(lastNe != "O") {
        nes = NE(lastFrom,lastTo,lastNe.substring(2),lastLineNr) :: nes
        lastNe = "O"
        lastFrom = -1
        lastTo = -1        
        lastLineNr = -1
      } else {
        // last one was O, so start a new one here
        lastNe = thisNe
        lastFrom = from
        lastTo = to
        lastLineNr = linenr
      }
    }
  }
}
// Write any non-written document
if(content.size > 0) {
  docNr+=1
  saveDoc(docNr,content.toString,tokens,nes,sentences,format)
}



def saveDoc(nr: Int, content: String, tokens: List[Token], nes: List[NE], sentences: List[Sentence], format: String) = {
  //System.err.println("NES: "+nes)
  val parms = Factory.newFeatureMap()
  parms.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8")
  parms.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, content)
  parms.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, "text/plain")
  val doc = Factory.createResource("gate.corpora.DocumentImpl", parms).asInstanceOf[Document]
  // add the tokens
  val origs = doc.getAnnotations("Original markups")
  sentences.foreach { sentence => 
    var fm = Factory.newFeatureMap()
    gate.Utils.addAnn(origs,sentence.from,sentence.to,"Sentence",fm)
    fm = Factory.newFeatureMap()
    fm.put("kind","external")
    gate.Utils.addAnn(origs,sentence.to,sentence.to+1,"Split",fm)
    // TODO: if the last character covered within the Sentence annotation is
    // punctuation, we probably should also add a Split.kind="internal" annotation there.
  }
  tokens.foreach { token =>
    val fm = Factory.newFeatureMap()
    fm.put("lemma",token.lemma)
    fm.put("pos",token.pos)
    fm.put("chunkBIO",token.chunkBIO)
    fm.put("neBIO",token.neBIO)
    fm.put("lineNr",token.lineNr: java.lang.Integer)
    gate.Utils.addAnn(origs,token.from, token.to, "Token", fm)
  }
  nes.foreach { ne =>
    val fm = Factory.newFeatureMap()
    fm.put("startLineNr",ne.startLineNr: java.lang.Integer)
    // Sanity check
    if(ne.startLineNr == -1) {
      System.err.println("Got a startLineNr of -1 for ne: "+ne)
    }
    gate.Utils.addAnn(origs,ne.from, ne.to, ne.annType, fm)
  }
  // actually write the document 
  val docName = if(format.equals("finf")) "%05d.finf".format(nr) else "%05d.xml".format(nr)
  System.err.println("Saving document: "+docName+ " nrSentences="+sentences.size)
  if(format.equals("xml")) {
    gate.corpora.DocumentStaxUtils.writeDocument(doc,new File(outDir,docName))
  } else {
    docExporter.export(doc,new File(outDir, docName), Factory.newFeatureMap());
  }
}

