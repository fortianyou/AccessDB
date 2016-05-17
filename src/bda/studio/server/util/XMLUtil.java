package bda.studio.server.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * 用于读取XML类型文件
 *
 * @author guotianyou
 */
public class XMLUtil {

  private static Logger logger = Logger.getLogger(XMLUtil.class.getName());

  /**
   * 输入本地文件名，返回一个Document类型的xml描述
   *
   * @param file 本地文件名
   * @return document
   * @throws Exception
   */
  public static Document read(String file) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document dom = builder.parse(new File(file));
    return dom;
  }

  /**
   * 输入本地文件名，返回一个Document类型的xml描述
   *
   * @param file 本地文件名
   * @return document
   * @throws Exception
   */
  public static Document read(InputStream in) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = null;
    builder = factory.newDocumentBuilder();
    Document dom = builder.parse(in);
    return dom;
  }

  /** Parse a XML document from string */
  public static Document parse(String xml) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xml));
    return builder.parse(is);
  }

  /** Create a empty XML docmuent */
  public static Document createDocument() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = factory.newDocumentBuilder();
    Document doc = docBuilder.newDocument();
    return doc;
  }

  /** Transform a XML Document to String */
  public static String toString(Document doc) {
    try {
      DOMSource domSource = new DOMSource(doc);
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  /**
   * Format generated xml doc by indentation
   *
   * @param node   For java, rather than GWT
   * @param indent
   * @return
   */
  public static String format(Node node, String indent) {
    StringBuilder formatted = new StringBuilder();

    if (node.getNodeType() == Node.ELEMENT_NODE) {
      StringBuilder attributes = new StringBuilder();
      for (int k = 0; k < node.getAttributes().getLength(); k++) {
        attributes.append(" ");
        attributes.append(node.getAttributes().item(k).getNodeName());
        attributes.append("=\"");
        attributes.append(node.getAttributes().item(k).getNodeValue());
        attributes.append("\"");
      }

      formatted.append(indent);
      formatted.append("<");
      formatted.append(node.getNodeName());
      formatted.append(attributes.toString());
      if (!node.hasChildNodes()) {
        formatted.append("/>\n");
        return formatted.toString();
      }
      if ((node.hasChildNodes() && node.getFirstChild().getNodeType() == Node.TEXT_NODE)) {
        formatted.append(">");
      } else {
        formatted.append(">\n");
      }

      for (int i = 0; i < node.getChildNodes().getLength(); i++) {
        formatted.append(format(node.getChildNodes().item(i), indent + "   "));
      }

      if (node.hasChildNodes()
          && node.getFirstChild().getNodeType() != Node.TEXT_NODE) {
        formatted.append(indent);
      }
      formatted.append("</");
      formatted.append(node.getNodeName());
      formatted.append(">\n");
    } else {
      String value = node.getTextContent().trim();
      if (value.length() > 0) {
        formatted.append(value);
      }
    }
    return formatted.toString();
  }

}
