package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Flags {
    public static final int NONE = 0;
    public static final int READ = 0x01;
    public static final int SHARED = 0x02;
    public static final int KEEP = 0x04;

    public static final int ALL = READ | SHARED | KEEP;
}
