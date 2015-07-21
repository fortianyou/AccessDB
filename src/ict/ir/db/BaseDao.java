package ict.ir.db;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaseDao {
	private static String url ="jdbc:oracle:thin:@localhost:1521:orcl";
	private static String name = "root";
	private static String password = "root";
	protected static Connection conn;
	static{
		conn = getConnection();
	}
	public static Connection getConnection(){
		Connection conn = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			conn = DriverManager.getConnection(url,name,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
	
	/**
	 * 将查询参数填入
	 * @param o
	 * @return
	 * @throws Exception
	 */
	public List find(Object o) throws Exception {
		//获取类
		Class clazz = o.getClass();
		// 获取类的所有属性，返回Field数组
		Field[] fields = clazz.getDeclaredFields();
		
		StringBuffer sql = new StringBuffer("select ");
		for (Field field : fields) {
			sql.append(field.getName()+",");
		}
		sql = new StringBuffer( sql.substring(0,sql.length()-1));
		sql.append(" from	" + clazz.getSimpleName());
		sql.append(" where 1=1	");
		
		//再遍历一边添加where条件
		for (Field field : fields) {
			//如果属性有值
			 PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
			 Method m = pd.getReadMethod();//获得读方法
			 
			 if(m.invoke(o)  != null){
				 if( field.getName().equals("_abstract") )
					 sql.append(" and abstract = " + m.invoke(o));
				 else
					 sql.append(" and " + field.getName()+" = " + m.invoke(o));
			 }
		}
		//System.out.println(sql.toString());
		

		//获得数据库连接
		Connection conn = getConnection();
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		List resultList = new ArrayList();
		pstmt = conn.prepareStatement(sql.toString());
		resultSet = pstmt.executeQuery();
		while (resultSet.next()) {
			Object instance = clazz.newInstance();
			
			for (Field field : fields) {
				if(field.getType().toString().equals("class java.lang.String")){
					 String val = resultSet.getString(field.getName());
					 String name = field.getName();
					 if( name.equals("abstract") ) name = "_"+name;
					 PropertyDescriptor pd = new PropertyDescriptor(name, clazz);
					 Method m = pd.getWriteMethod();//获得写方法
					 m.invoke(instance, val);
				}
			}
			resultList.add(instance);
		}
		return resultList;
	}

}
