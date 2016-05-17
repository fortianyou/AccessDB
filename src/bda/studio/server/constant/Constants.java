package bda.studio.server.constant;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bda.studio.server.util.XMLUtil;
import bda.studio.server.util.typeparser.StringToTypeParser;

/**
 * 从WEB-INF/classes/server-config.xml资源文件中读取服务的配置。
 * 
 * @author guotianyou
 * @time 2015年11月10日 下午10:34:04 Constants
 */
public class Constants {

  // public static final String OOZIE_CLIENT = "http://bda05:11000/oozie";
  public static String OOZIE_CLIENT;
  // public static final String NAME_NODE = "hdfs://bda00:8020";
  public static String NAME_NODE;
  // public static final String JOB_TRACKER = "bda04:8050";
  public static String JOB_TRACKER;
  public static String QUEUE_NAME;
  public static String APP_WORKSPACE;
  public static String DRAFT_PATH;
  public static String DATASET_PATH;
  public static String MODULE_PATH;
  // public static final String DB_URL ="jdbc:mysql://bda04:3306/dfgendb";
  public static String DB_URL;
  public static String DB_USER;
  // this is the password
  public static String DB_PASSWORD;
  public static Integer DB_TIMEOUT;
  /**********************************************
   *********** MAIL *****************
   **********************************************/
  public static String MAIL_HOST;
  public static String MAIL_USERNAME;
  public static String MAIL_PASSWORD;
  static Logger logger = Logger.getLogger(Constants.class.getName());

  static {
    Document dom = null;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      // 获取配置文件，并构建xml dom对象。
      //String pt = "/home/xh/bda/studio/src/server-config.xml";
      dom = XMLUtil.read(classLoader.getResourceAsStream("server-config.xml"));
      //dom = XMLUtil.read(classLoader.getResourceAsStream(pt));
      //FileInputStream in = new FileInputStream(pt);
      //dom = XMLUtil.read(in);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    /**
     * 使用java反射机制设置对应的属性
     */
    Class<Constants> clazz = Constants.class;
    StringToTypeParser parser = StringToTypeParser.newBuilder().build();

    Element root = dom.getDocumentElement();
    NodeList nlist = root.getChildNodes();
    int len = nlist.getLength();
    // 遍历dom树根节点的所有子节点
    for (int i = 0; i < len; ++i) {
      Node tagNode = nlist.item(i);
      Node valNode = tagNode.getFirstChild();
      if (valNode == null)
        continue;

      String name = tagNode.getNodeName();

      try {
        Field f = clazz.getDeclaredField(name);
        String val = valNode.getNodeValue();
        // 使用parser自动识别类型，并作类型转换，parse( String, Class )
        f.set(null, parser.parse(val, f.getType()));
        logger.info(name + " " + val);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

}
