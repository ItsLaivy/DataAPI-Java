package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;

public abstract class Variable {

    public static final @NotNull Set<@NotNull Variable> TEMPORARY_VARIABLES = new LinkedHashSet<>();

    protected final @NotNull String name;
    protected final @NotNull Database database;
    protected final @Nullable Object defaultValue;
    protected final boolean serialize;
    protected final boolean saveToDatabase;

    public abstract void delete();

    public Variable(@NotNull String name, @NotNull Database database, @Nullable Object defaultValue) {
        this(name, database, defaultValue, true, true);
    }
    public Variable(@NotNull String name, @NotNull Database database, @Nullable Object defaultValue, boolean serialize, boolean saveToDatabase) {
        this.name = name;
        this.database = database;
        this.saveToDatabase = saveToDatabase;
        this.defaultValue = defaultValue;
        this.serialize = serialize;

        if (DataAPI.getVariable(database, name) != null) {
            return;
        }

        if (defaultValue != null && isSerialize() && !(defaultValue instanceof Serializable)) {
            throw new IllegalArgumentException("The serialization option are enabled, but the value isn't a instance of Serializable!");
        }

        DataAPI.VARIABLES.putIfAbsent(database, new HashSet<>());
        DataAPI.VARIABLES.get(database).add(this);

        if (!saveToDatabase) {
            TEMPORARY_VARIABLES.add(this);
        }
    }

    public @NotNull Database getDatabase() {
        return database;
    }

    public boolean isSaveToDatabase() {
        return saveToDatabase;
    }

    public boolean isSerialize() {
        return serialize;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Object getDefaultValue() {
        return defaultValue;
    }

    protected static byte[] getVariableHashedValue(@Nullable Serializable value) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(b);
            stream.writeObject(value);
            return b.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Nullable
    protected static Serializable getVariableUnhashedValue(byte[] value) {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(value);
            ObjectInputStream stream = new ObjectInputStream(b);
            return (Serializable) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @NotNull
    protected static String byteArrayToString(byte[] byteArray) {
        return Arrays.toString(byteArray).replace("[", "").replace("]", "").replace(", ", "/");
    }
    protected static byte[] stringToByteArray(@NotNull String byteArray) {
        String[] split = byteArray.split("/");
        byte[] b = new byte[split.length];

        int row = 0;
        for (String e : split) {
            b[row] = Byte.parseByte(e);
            row++;
        }

        return b;
    }

    public static @NotNull String serialize(@Nullable Serializable value) {
        return Variable.byteArrayToString(Variable.getVariableHashedValue(value));
    }
    public static @Nullable Serializable unserialize(@NotNull String string) {
        return Variable.getVariableUnhashedValue(Variable.stringToByteArray(string));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;
        Variable variable = (Variable) o;
        return Objects.equals(getName(), variable.getName()) && Objects.equals(getDatabase(), variable.getDatabase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDatabase());
    }
}
