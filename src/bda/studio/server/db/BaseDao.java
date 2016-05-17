package bda.studio.server.db;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import bda.studio.server.anotation.Password;
import bda.studio.server.anotation.TableField;
import bda.studio.server.constant.Constants;
import bda.studio.server.util.typeparser.StringToTypeParser;


/**
 * Base Data Access Object(DAO)<br/>
 * 这个类主要负责连接数据库等操作
 *
 * @author guotianyou
 * @time 2015年11月10日 下午10:28:46 BaseDao
 */
public class BaseDao {
  private static Logger logger = Logger.getLogger(BaseDao.class.getName());
  private static StringToTypeParser parser = StringToTypeParser.newBuilder().build();
  private static Connection conn = getConnection();

  /**
   * 如果已经有生效的数据连接，则直接返回连接对象。<br/>
   * 如果连接对象为null,或者失效，则会新建一个连接对象并返回。
   */
  public static Connection getConnection() {

    try {
      if (conn != null && conn.isValid(Constants.DB_TIMEOUT))
        return conn;

      Class.forName("com.mysql.jdbc.Driver");

      conn = DriverManager.getConnection(Constants.DB_URL,
          Constants.DB_USER,
          Constants.DB_PASSWORD);
      /*
       * String url = Constants.DB_URL+"?user="+ Constants.DB_USER
       * +"&password="+Constants.DB_PASSWORD+
       * "&useUnicode=true&characterEncoding=utf-8"; conn =
       * DriverManager.getConnection(url);
       */
    } catch (Exception e) {
      e.printStackTrace();
    }
    return conn;

  }

  public static String password(String pwd) {
    // return "PASSWORD(\""+ pwd.replace("\"", "\\\"") + "\")";
    return pwd;
  }

  protected static String getConditionExpression(Field field) {
    String res;
    String name = getTableFieldName(field);
    if ( !field.isAnnotationPresent(Password.class)) {
      res = " and " + name + " = ?";
    } else {
      res = " and " + name + " = password(?)";
    }
    return res;
  }

  protected static String getUpdateExpression(Field field) {
//    System.out.println( field.isAnnotationPresent(Password.class));
    if ( !field.isAnnotationPresent(Password.class)) {
      return " " + getTableFieldName( field ) + " = ?,";
    } else {
      return " " + getTableFieldName( field ) + " = PASSWORD(?),";
    }
  }

  protected static String getInsertExpression(Field field) {
    if ( !field.isAnnotationPresent(Password.class) ) {
      return "?,";
    } else {
      return "PASSWORD(?),";
    }
  }

  /**
   * 根据输入的字符串值，设置对应的域
   *
   * @param instance 被设置的对象
   * @param strVal   输入的值
   * @param field    输入的域
   * @throws IntrospectionException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  public static void invoke(Object instance, String strVal, Field field)
      throws IntrospectionException, InvocationTargetException, IllegalAccessException {

    if (strVal == null) strVal = "";

    Object val = parser.parse(strVal, field.getType());

    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), field.getDeclaringClass());
    Method m = pd.getWriteMethod();// 获得写方法
    m.invoke(instance, val);
  }

  /**
   * 获取对象的所有声明的TableField域
   * 如果子类和父类中都包含同名的域，则只会返回子类的域
   *
   * @param clazz 输入的类型
   * @return clazz以及clazz的所有祖先声明的域
   */
  protected static Map<String, Field> getTableFields(Class clazz) {
    Map<String, Field> fieldMap = new HashMap<String, Field>();

    while (clazz != null) {
      Field fields[] = clazz.getDeclaredFields();
      for (Field f : fields) {
        //过滤不包含TableField注释的域
        if( !f.isAnnotationPresent(TableField.class) ) continue;
        
        String name = getTableFieldName( f );

        if (fieldMap.containsKey(name)) continue;
        else{
          fieldMap.put( name , f);
        }
      }

      clazz = clazz.getSuperclass();
    }
    return fieldMap;
  }

  
  /**
   * 给一个object o 的类和该类的成员Field，得到该成员的值
   * @param o
   * @param field
   * @return
   * @throws Exception
   */
  public static String toString(Object o, Field field)  throws Exception {
    PropertyDescriptor pd = new PropertyDescriptor(field.getName(), o.getClass());
    Method m = pd.getReadMethod();
    Object invoke = m.invoke(o);
    return parser.toString(invoke, field.getType());
  }
  
  private static String getTableFieldName(Field f){
    String name = f.getAnnotation(TableField.class).name();
    if( "".equals(name) ) name = f.getName();
    return name;
  }
}
