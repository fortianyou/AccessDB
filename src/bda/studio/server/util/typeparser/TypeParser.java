package bda.studio.server.util.typeparser;

public interface TypeParser<T> {
    T parse(String value);
    String toString(Object obj);
}