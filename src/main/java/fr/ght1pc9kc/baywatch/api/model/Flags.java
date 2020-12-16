package fr.ght1pc9kc.baywatch.api.model;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Flags {
    public static final int READ = 0x01;
    public static final int STAR = 0x02;

    public static final int ALL = READ & STAR;
}
