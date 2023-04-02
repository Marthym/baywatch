package fr.ght1pc9kc.baywatch.common.domain;

public enum EntityAssert {
    US, TM;

    public static boolean user(String id) {
        return id.startsWith(US.name());
    }

    public static boolean team(String id) {
        return id.startsWith(TM.name());
    }
}
