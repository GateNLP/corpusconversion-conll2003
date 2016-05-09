import gate._
import java.io._

val patEmpty = "^\\s*$".r
val patNewDoc = "^\\s*-DOCSTART-\\s+-X-\\s+-X-\\s+.*$".r
val patLine = "\\s+"

val in = scala.io.Source.fromInputStream(System.in)

var content = new StringBuilder()

case class Token(from: Int, to: Int, lemma: String, pos: String, chunkBIO: String, neBIO: String) { }

// We use a list to store the tokens and we append at the beginning which is fast
// Order does not matter since we will later convert those into actual GATE annotations
var tokens = List[Token]()

def saveDoc(nr: Int, content: String, tokens: List[Token]) = {
  val docName = "%05d.xml".format(nr)
  System.err.println("Saving document: "+docName)
  val parms = Factory.newFeatureMap()
  parms.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, "UTF-8")
  parms.put(Document.DOCUMENT_STRING_CONTENT_PARAMETER_NAME, content)
  parms.put(Document.DOCUMENT_MIME_TYPE_PARAMETER_NAME, "text/plain")
  val doc = Factory.createResource("gate.corpora.DocumentImpl", parms).asInstanceOf[Document]
  // add the tokens
  val origs = doc.getAnnotations("Original markups")
  tokens.foreach { token =>
    val fm = Factory.newFeatureMap()
    fm.put("lemma",token.lemma)
    fm.put("pos",token.pos)
    fm.put("chunkBIO",token.chunkBIO)
    fm.put("neBIO",token.neBIO)
    gate.Utils.addAnn(origs,token.from, token.to, "Token", fm)
  }
  // actually write the document 
  gate.corpora.DocumentStaxUtils.writeDocument(doc,new File(docName))
}

Gate.init()

var docNr = 0
var linenr = 0
var noSpace = true
in.getLines.foreach { line =>
  linenr += 1
  // first check if it is an empty line. If it is then add a new line character to the document
  if(!patEmpty.findFirstIn(line).isEmpty) {
    content.append("\n");
    noSpace = true
  } else if(!patNewDoc.findFirstIn(line).isEmpty) {
    // if we do have a document, write it
    if(content.size > 0) {
      docNr+=1
      saveDoc(docNr,content.toString,tokens)
    }
    content = new StringBuilder()
    tokens = List[Token]()
    noSpace = true
  } else { 
    // actual line with a token in it
    val fields = line.split(patLine,-1)
    //System.err.println("Got "+fields.size+" fields: "+fields.mkString(", "))
    // if we have 5 fields it must be german and the second field is a lemma, otherwise 
    // it must be english and the lemma is missing
    // if the current string is just punctuation or a closing parenthesis, then just append it otherwise
    // we first insert a separating space, except when the flag noSpace is set
    if(!fields(0).matches("[.,?!}\\])]")) {
      if(!noSpace) {
        content.append(" ")
      } else {
        noSpace = false
      }
    }
    val from = content.size
    content.append(fields(0))
    // if we just added an opening parentheses, do not add a space later
    if(fields(0).matches("[{\\[(]")) {
      noSpace=true
    }
    val to = content.size
    if(fields.size==5) {
      // there is an error in the l\emma: for all forms of the german articles, the lemma is "d"!!
      tokens =  Token(from,to,fields(1),fields(2),fields(3),fields(4)) :: tokens
    } else if (fields.size==4) {
      tokens = Token(from,to,null,fields(1),fields(2),fields(3)) :: tokens
    } else {
      System.err.println("Error in line "+linenr+": number of fields is "+fields.size)
    }
  }
}
// Write any non-written document
if(content.size > 0) {
  docNr+=1
  saveDoc(docNr,content.toString,tokens)
}
