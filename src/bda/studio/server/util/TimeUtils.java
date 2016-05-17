package bda.studio.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

  private static SimpleDateFormat ft = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");

  public static Date getTime() {
    Date date = new Date();
    return date;
  }

  public static long timeDiff(String time) {
    try {
      Date old = ft.parse(time);
      Date now = new Date();

      return now.getTime() - old.getTime();
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  public static String format(Date date) {
    if( date == null ) return null;
    return ft.format(date);
  }
  
  public static Date parse(String string){
    
    if( string == null ) return null;
    string = string.trim();
    if( string.equals("unknown") || string.equals("")) return null;
    
    try {
        if( !"".equals( string )){
          SimpleDateFormat ft = new SimpleDateFormat(
              "yyyy-MM-dd HH:mm:ss");
          return ft.parse(string);
        }else{
          System.out.println( string );
        }
    } catch (ParseException e) {
        e.printStackTrace();
    } catch (IllegalArgumentException e){
       System.out.println( "Exception string:" + string );
        e.printStackTrace();
    }
    return null;
  }

  public static void main(String args[]) {
    System.out.println(parse("2016-01-25 15:47:48"));
  }
}
