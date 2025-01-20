import java.io.StringReader
import java.io.StringWriter
import java.nio.file.Path
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import kotlin.io.path.writeText

fun createModuleReference(moduleImlRelativePath: String): String {
  return $$"""
        <module fileurl="file://$PROJECT_DIR$/$${moduleImlRelativePath}" filepath="$PROJECT_DIR$/$${moduleImlRelativePath}" />
      """.trim()
}

val BUCKETS: List<Int> = listOf(10, 10)

fun bucketSubPath(id: Int): String {
  val bucketCount = BUCKETS.reduce { acc, value -> acc * value }
  var bucketId = (id - 1) % bucketCount
  val result: List<Int> = BUCKETS.reversed().map {
    val result = bucketId % it
    bucketId = bucketId / it
    result
  }.reversed()
  val names: List<String> = result.mapIndexed { index, value ->
    ('a' + value).toString().repeat(index + 1)
  }
  return names.joinToString("/")
}

fun Path.writePrettyPrintedXml(xml: String) {
  this.writeText(xml.toPrettyPrintedXml())
}

private val XSL_TEXT: String = Module::class.java.getResourceAsStream("/pretty-print.xsl")!!.use {
  it.reader().readText()
}

private fun String.toPrettyPrintedXml(): String {
  val transformer = TransformerFactory.newInstance().newTransformer(StreamSource(StringReader(XSL_TEXT)))
  transformer.setOutputProperty(OutputKeys.INDENT, "yes")
  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

  val result = StreamResult(StringWriter())
  val source = StreamSource(StringReader(this.trim()))
  transformer.transform(source, result)
  return result.writer.toString()
}
