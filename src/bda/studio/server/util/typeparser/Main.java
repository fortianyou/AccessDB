package bda.studio.server.util.typeparser;


public class Main {

	public static void main(String args[]){
		/**
		 * 可以自定义类型解析器，并使用StringToTypeParserBuilder的registerTypeParser来注册解析类型
		StringToTypeParser parser = StringToTypeParser.newBuilder()
				.registerTypeParser(Car.class, new CarTypeParser())
				.registerTypeParser(int.class, new MySpecialIntTypeParser())
				.build();

		Car volvo = parser.parseType("volvo", Car.class);
		*/
		StringToTypeParser parser = StringToTypeParser.newBuilder().build();
//		String  = parser.parse("", String.class);
		System.out.println(  );
	}
}
