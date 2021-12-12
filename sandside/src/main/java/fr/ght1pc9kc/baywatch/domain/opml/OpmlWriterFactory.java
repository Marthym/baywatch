package fr.ght1pc9kc.baywatch.domain.opml;

import java.io.OutputStream;
import java.util.function.Function;

public interface OpmlWriterFactory extends Function<OutputStream, OpmlWriter> {
}
