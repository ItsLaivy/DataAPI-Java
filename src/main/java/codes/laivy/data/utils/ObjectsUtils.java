package codes.laivy.data.utils;

import org.jetbrains.annotations.Nullable;

public class ObjectsUtils {

    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

}
