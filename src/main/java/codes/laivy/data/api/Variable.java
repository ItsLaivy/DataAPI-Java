package codes.laivy.data.api;

import codes.laivy.data.DataAPI;
import codes.laivy.data.api.variables.ActiveVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Arrays;

public class Variable {

    private final String name;
    private final Table table;
    private final Serializable defaultValue;
    private final boolean saveToDatabase;

    public Variable(@NotNull String name, @NotNull Table table, @Nullable Serializable defaultValue) {
        this(name, table, defaultValue, true);
    }
    public Variable(@NotNull String name, @NotNull Table table, @Nullable Serializable defaultValue, boolean saveToDatabase) {
        this.name = name;
        this.table = table;
        this.saveToDatabase = saveToDatabase;
        this.defaultValue = defaultValue;

        if (DataAPI.getVariable(table, name) != null) {
            if (DataAPI.EXISTS_ERROR) throw new IllegalStateException("A variable named '" + name + "' at the table '" + getTable().getName() + "' in the database '" + getTable().getDatabase().getName() + " ('" + getTable().getDatabase().getDatabaseType().getName() + "')' already exists!");
            return;
        }

        getTable().getDatabase().getDatabaseType().variableLoad(this);

        DataAPI.VARIABLES.get(getTable()).add(this);

        for (Receptor receptor : DataAPI.RECEPTORS.get(table)) {
            new ActiveVariable(this, receptor, defaultValue);
        }
    }

    public void delete() {
        for (Receptor receptor : DataAPI.RECEPTORS.get(getTable())) {
            DataAPI.ACTIVE_VARIABLES.get(receptor).removeIf(activeVariable -> activeVariable.getVariable().equals(this));
            DataAPI.INACTIVE_VARIABLES.get(receptor).removeIf(inactiveVariable -> inactiveVariable.getVariable().equals(this.getName()));
        }

        DataAPI.VARIABLES.get(getTable()).remove(this);
        getTable().getDatabase().getDatabaseType().variableDelete(this);
    }

    public boolean isSaveToDatabase() {
        return saveToDatabase;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public Serializable getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public Table getTable() {
        return table;
    }

    private static byte[] getVariableHashedValue(@Nullable Serializable value) {
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

    @NotNull
    private static Serializable getVariableUnhashedValue(byte[] value) {
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
    private static String byteArrayToString(byte[] byteArray) {
        return Arrays.toString(byteArray).replace("[", "").replace("]", "").replace(", ", "/");
    }
    private static byte[] stringToByteArray(@NotNull String byteArray) {
        String[] split = byteArray.split("/");
        byte[] b = new byte[split.length];

        int row = 0;
        for (String e : split) {
            b[row] = Byte.parseByte(e);
            row++;
        }

        return b;
    }

    public static String serialize(Serializable value) {
        return Variable.byteArrayToString(Variable.getVariableHashedValue(value));
    }
    public static Serializable unserialize(String string) {
        return Variable.getVariableUnhashedValue(Variable.stringToByteArray(string));
    }

}
