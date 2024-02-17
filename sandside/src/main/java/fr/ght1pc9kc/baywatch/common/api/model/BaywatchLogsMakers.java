package fr.ght1pc9kc.baywatch.common.api.model;

import lombok.experimental.UtilityClass;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@UtilityClass
public class BaywatchLogsMakers {
    public static final Marker PERFORMANCE = MarkerFactory.getMarker("performance");
}
