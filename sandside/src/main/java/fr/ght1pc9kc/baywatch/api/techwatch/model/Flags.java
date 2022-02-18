package fr.ght1pc9kc.baywatch.api.techwatch.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Flags {
    public static final int NONE = 0;
    public static final int READ = 0x01;
    public static final int SHARED = 0x02;

    public static final int ALL = READ | SHARED;
}
