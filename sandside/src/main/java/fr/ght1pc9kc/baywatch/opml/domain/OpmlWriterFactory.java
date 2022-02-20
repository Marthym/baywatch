package fr.ght1pc9kc.baywatch.opml.domain;

import java.io.OutputStream;
import java.util.function.Function;

public interface OpmlWriterFactory extends Function<OutputStream, OpmlWriter> {
}
