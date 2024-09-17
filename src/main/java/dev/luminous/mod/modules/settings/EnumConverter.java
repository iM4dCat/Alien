package dev.luminous.mod.modules.settings;

public class EnumConverter {

    public static int currentEnum(Enum<?> clazz) {
        for (int i = 0; i < clazz.getDeclaringClass().getEnumConstants().length; ++i) {
            Enum<?> e = clazz.getDeclaringClass().getEnumConstants()[i];
            if (!e.name().equalsIgnoreCase(clazz.name())) continue;
            return i;
        }
        return -1;
    }

    public static Enum<?> increaseEnum(Enum<?> clazz) {
        int index = currentEnum(clazz);
        for (int i = 0; i < clazz.getDeclaringClass().getEnumConstants().length; ++i) {
            if (i != index + 1) continue;
            return clazz.getDeclaringClass().getEnumConstants()[i];
        }
        return clazz.getDeclaringClass().getEnumConstants()[0];
    }

    public Enum<?> get(Enum<?> clazz, String string) {
        try {
            for (int i = 0; i < clazz.getDeclaringClass().getEnumConstants().length; ++i) {
                Enum<?> e = clazz.getDeclaringClass().getEnumConstants()[i];
                if (!e.name().equalsIgnoreCase(string)) continue;
                return e;
            }
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}

