package bda.studio.server.db;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Secure Data Access Object(DAO) 数据库访问对象<br/>
 * DAO提供了通过java对象访问数据库的功能，输入参数和返回值均为Java简单类对象，API使用者不需要构建sql语句，DAO会帮其构建并执行增删改查。
 * 要求数据对象类型名和数据表名一致，数据对象的属性名和数据库的属性名必须保持一致，并且实现了getter和setter方法。
 *
 * @author guotianyou
 * @time 2015年11月10日 下午9:57:48<br/>
 * SecureDao
 */
public class SecureDao extends BaseDao {
  private static Logger logger = Logger.getLogger(SecureDao.class.getName());

  /**
   * 数据库查询函数。<br/>
   * 该函数采用java反射机制自动构建sql语句，并使用查询结果自动构建对象
   *
   * @param o 输入的查询对象，对象中填充的值将会被视作查询条件
   * @return T 返回符合查询条件的第一个对象
   * @throws Exception
   */
  public static <T> T find(T o) throws Exception {
    List<T> list = select(o, "");
    if( list.size() > 0  ) return list.get(0);
    return null;
  }

  public static <T> T find(T o, String append_expr) throws Exception {
    List<T> list = select(o, append_expr);
    if( list.size() > 0  ) return list.get(0);
    return null;
  }

  /**
   * 按照输入对象o所填充的值作为查询条件，查询数据库，并返回o对应类型的对象
   *
   * @param obj         保存查询条件的对象
   * @param append_expr 附加的条件表达式
   * @param <T>         o所对应的泛型
   * @return 查询结果对象列表
   * @throws Exception
   */
  public static <T> List<T> select(T obj, String append_expr)
      throws Exception {
    // 获取类
    Class clazz = obj.getClass();
    // 获取类的所有属性，返回<FieldName,Field> map
    Map<String, Field> fields = getTableFields(clazz);
    StringBuffer sql = new StringBuffer("select ");
    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      String name = entry.getKey();
      sql.append(name + ",");
    }
    sql = new StringBuffer(sql.substring(0, sql.length() - 1));
    sql.append(" from	" + clazz.getSimpleName().toLowerCase());
    sql.append(" where 1=1	");

    List<String> conds = new LinkedList<String>();
    // 再遍历一边添加where条件
    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      String name = entry.getKey();
      Field field = entry.getValue();
      String invoke = toString(obj, field);
      // 如果属性有值
      if (invoke != null) {
        conds.add(invoke.toString());
        sql.append(getConditionExpression(field));
      }
    }
    logger.info(sql.toString() + " " + append_expr);
    // return select(clazz,sql.toString(),conds);
    return select(clazz, sql.toString() + " " + append_expr, conds);
  }

  /**
   * 根据prepare statement以及对应的参数执行查询
   *
   * @param clazz      对应类型
   * @param prepSql    prepare statement
   * @param parameters parameters of prepare statement
   * @param <T>
   * @return 查询结果对象列表
   * @throws Exception
   */
  private static <T> List<T> select(Class<T> clazz, String prepSql,
                                    List<String> parameters) throws Exception {

    ResultSet resultSet;
    PreparedStatement pstmt;
    List<T> resultList = new ArrayList<T>();
    pstmt = getConnection().prepareStatement(prepSql.toString());
    logger.info(prepSql.toString());

    int i = 1;
    for (String param : parameters) {
      pstmt.setString(i++, param);
    }

    resultSet = pstmt.executeQuery();

    Map<String, Field> fields = getTableFields(clazz);
    
    while (resultSet.next()) {
      T instance = clazz.newInstance();
      for (Map.Entry<String, Field> entry : fields.entrySet()) {
        String name = entry.getKey();
        Field field = entry.getValue();

        invoke(instance, resultSet.getString(name), field);
      }

      resultList.add(instance);
    }
    return resultList;
  }

  /**
   * 查找函数，该函数执行了sql语句，并返回clazz类型的结果
   *
   * @param clazz 返回结果的类型
   * @param sql   数据库查询语句
   * @return 返回符合查询的结果
   * @throws Exception
   */
  private static <T> List<T> select(Class<T> clazz, String sql)
      throws Exception {

    ResultSet resultSet;
    PreparedStatement pstmt;
    List<T> resultList = new ArrayList<T>();
    pstmt = getConnection().prepareStatement(sql.toString());
    logger.info(sql.toString());
    resultSet = pstmt.executeQuery();

    Map<String, Field> fields = getTableFields(clazz);
    while (resultSet.next()) {
      T instance = clazz.newInstance();
      for (Map.Entry<String, Field> entry : fields.entrySet()) {
        String name = entry.getKey();
        Field field = entry.getValue();
        invoke(instance, resultSet.getString(name), field);
      }
      
      if( instance != null ){
        resultList.add(instance);
      }
    }
    
    return resultList;
  }

  /**
   * 查询T类型对应的数据库表，返回所有T类型的数据库记录
   *
   * @param object
   * @return 返回封装了的T类型的对象
   * @throws Exception
   */
  public static <T> List<T> findAll(T object) throws Exception {
    
    return select(object,"");
  }

  /**
   * 查询T类型对应的数据库表，返回所有T类型的数据库记录
   *
   * @param object
   * @return 返回封装了的T类型的对象
   * @throws Exception
   */
  public static <T> List<T> findAll(T object, String append_expr)
      throws Exception {
    
    return select(object, append_expr);
  }

  /**
   * 往o运行时类型所对应的表中插入一条记录，记录的值与Object的属性值对应(包括其父类的属性)
   *
   * @param o 要插入到数据库中的对象
   * @throws Exception
   */
  public static void insert(Object o) throws Exception {
    Class clazz = o.getClass();

    StringBuffer sql = new StringBuffer("insert into ");
    sql.append(clazz.getSimpleName().toLowerCase());
    StringBuffer namestr = new StringBuffer(" (");
    StringBuffer valuestr = new StringBuffer(" values(");
    List<String> values = new LinkedList<String>();

    Map<String, Field> fields = getTableFields(clazz);
    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      String name = entry.getKey();
      Field field = entry.getValue();
      String invoke = toString(o, field);
      if (invoke != null) {
        namestr.append(name);
        namestr.append(",");
        valuestr.append(getInsertExpression(field));
        values.add(invoke.toString());
      }
    }
    namestr.setCharAt(namestr.length() - 1, ')');
    valuestr.setCharAt(valuestr.length() - 1, ')');
    sql.append(namestr);
    sql.append(valuestr + "\r\n");
    String sql_str = sql.toString();
    logger.info(sql_str);
    execute(sql_str, values);
    // return execute(sql_str);
  }

  public static void update(Object o, String[] setCols, String[] condCols)
      throws Exception {
    Class clazz = o.getClass();

    StringBuffer sql = new StringBuffer("update ");
    sql.append(clazz.getSimpleName().toLowerCase());
    sql.append(" set ");

    List<String> values = new LinkedList<String>();
    Map<String, Field> fields = getTableFields(clazz);
    for (String colName : setCols) {
      String invoke = toString(o, fields.get(colName));
      if( invoke != null ){
        sql.append(getUpdateExpression(fields.get(colName)));
        values.add(invoke.toString());
      }
      
    }
    sql.setCharAt(sql.length() - 1, ' ');
    sql.append(" where 1=1  ");

    for (String colName : condCols) {
      String invoke = toString(o, fields.get(colName));
      if( invoke != null ){
        sql.append(getConditionExpression(fields.get(colName)));  
        values.add(invoke.toString());
      }

    }

    String sql_str = sql.toString();
    logger.info(sql_str);

    execute(sql_str, values);
  }

  /**
   * 对象o对应的数据表中的对应记录
   * 该函数中，如果对象o的对应属性域值不为null，则将被更新到数据库。查询条件通过condCols指定的列来构建。
   *
   * @param o          输入的对象
   * @param condCols 作为查询条件的列
   * @throws Exception
   */
  public static void update(Object o, String... condCols)
      throws Exception {
    Class clazz = o.getClass();

    StringBuffer sql = new StringBuffer("update ");
    sql.append(clazz.getSimpleName().toLowerCase());
    sql.append(" set ");
    List<String> values = new LinkedList<String>();
    Map<String, Field> fields = getTableFields(clazz);
    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      Field field = entry.getValue();
      String invoke = toString(o, field );
      if( invoke != null ){
        sql.append(getUpdateExpression(field));
        values.add(invoke.toString());
      }
    }
    sql.setCharAt(sql.length() - 1, ' ');
    sql.append(" where 1=1  ");
    for (String colName : condCols) {
      String invoke = toString(o, fields.get(colName));
      if( invoke != null ){
        sql.append(getConditionExpression(fields.get(colName)));  
        values.add(invoke.toString());
      }
    }

    String sql_str = sql.toString();
    logger.info(sql_str);

    execute(sql_str, values);
  }

  /**
   * 对象o对应的数据表中的对应记录
   * 该函数中，如果对象o的对应属性域值不为null，则将被删除的条件
   *
   * @param o          输入的对象
   * @throws Exception
   */
  public static void delete(Object o) throws Exception {
    Class clazz = o.getClass();
    StringBuffer sql = new StringBuffer("delete ");
    sql.append(clazz.getSimpleName().toLowerCase());
    sql.append(" from ");
    sql.append(clazz.getSimpleName().toLowerCase());
    sql.append(" where 1=1 ");

    // 获取类的所有属性，返回<FieldName,Field> map
    Map<String, Field> fields = getTableFields(clazz);
    List<String> values = new LinkedList<String>();
    // 再遍历一边添加where条件
    for (Map.Entry<String, Field> entry : fields.entrySet()) {
      String name = entry.getKey();
      Field field = entry.getValue();
      String invoke = toString(o, field);
      // 如果属性有值
      if (invoke != null) {
        sql.append(getConditionExpression(field));
        values.add( invoke.toString() );
      }
    }

    String sql_str = sql.toString();
    logger.info(sql_str);

    execute(sql_str, values);
  }

  public static void execute(String sql, List<String> values)
      throws Exception {
    PreparedStatement pstat = getConnection().prepareStatement(sql);
    int i = 1;
    for (String val : values) {
      pstat.setString(i++, val);
    }
    logger.info(pstat.toString());
    pstat.execute();
  }

  /**
   * 执行sql语句的函数
   *
   * @param sql 需要执行的sql语句
   * @return 如果执行成功则返回true，否则false
   * @throws Exception
   */
  public static boolean execute(String sql) throws Exception {
    PreparedStatement pstat = getConnection().prepareStatement(sql);
    return pstat.execute();
  }


}
