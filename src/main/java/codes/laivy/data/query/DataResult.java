package codes.laivy.data.query;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public abstract class DataResult {

    public abstract int columns();
    public abstract @NotNull Set<Map<String, Object>> results();
    public abstract void close();

}