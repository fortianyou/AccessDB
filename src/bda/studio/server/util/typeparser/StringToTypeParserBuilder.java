package bda.studio.server.util.typeparser;

import java.util.Map;

/**
 * 构建{@link StringToTypeParser}的类
 */
public final class StringToTypeParserBuilder {
    private Map<Class<?>, TypeParser<?>> typeParsers;

    StringToTypeParserBuilder() {
        // Initialize with the default typeParsers
        typeParsers = DefaultTypeParsers.copy();
    }

    public StringToTypeParserBuilder unregisterTypeParser(Class<?> type){
        if(type == null) {
            throw new NullPointerException(StringToTypeParser.nullArgumentErrorMsg("type"));
        }
        typeParsers.remove(type);
        return this;
    }

    public <T> StringToTypeParserBuilder registerTypeParser(Class<T> type, TypeParser<? extends T> typeParser){
        if(typeParser == null) {
            throw new NullPointerException(StringToTypeParser.nullArgumentErrorMsg("typeParser"));
        }
        if(type == null) {
            throw new NullPointerException(StringToTypeParser.nullArgumentErrorMsg("type"));
        }
        typeParsers.put(type, typeParser);
        return this;
    }

    public StringToTypeParser build(){
        return new StringToTypeParser(typeParsers);
    }
}