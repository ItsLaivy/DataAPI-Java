package codes.laivy.data.query;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class DataResult {

    public abstract int columns();
    public abstract @NotNull Map<String, String> results();
    public abstract void close();

}