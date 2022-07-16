package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.indexer.domain.model.Indexable;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.infra.config.IndexerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Component
public class LuceneDataAdapter implements IndexBuilderPort {

    private final Path directoryFile;

    public LuceneDataAdapter(IndexerProperties properties) {
        this.directoryFile = Path.of(properties.directory());
    }

    @Override
    @SuppressWarnings("java:S2095")
    public Mono<Void> write(Flux<Indexable> documents) {
        try {
            Directory memoryIndex = FSDirectory.open(directoryFile);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);
            writer.deleteAll();

            return documents.doOnNext(i -> {
                        try {
                            Document doc = new Document();
                            doc.add(new StringField("id", i.id(), Field.Store.NO));
                            doc.add(new TextField("title", i.title(), Field.Store.NO));
                            if (!Objects.isNull(i.description())) {
                                doc.add(new TextField("description", i.description(), Field.Store.NO));
                            }
                            doc.add(new TextField("link", i.link(), Field.Store.NO));
                            doc.add(new TextField("contentTitles", i.contentTitles(), Field.Store.NO));
                            doc.add(new TextField("contentSummaries", i.contentSummaries(), Field.Store.NO));
                            writer.addDocument(doc);
                        } catch (Exception e) {
                            if (log.isWarnEnabled()) {
                                log.warn("Unable to index document with id {}", i.id().substring(0, 10));
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("STACKTRACE", e);
                            }
                        }
                    }).doFinally(signal -> Exceptions.wrap().run(writer::close))
                    .then();

        } catch (Exception e) {
            log.error("Error while writing Feed index !");
            log.debug("STACKTRACE", e);
            return Mono.empty().then();
        }
    }
}
