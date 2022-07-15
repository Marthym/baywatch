package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableDocument;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableVisitor;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.infra.config.IndexerProperties;
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

import java.io.IOException;
import java.nio.file.Path;

@Component
public class LuceneDataAdapter implements IndexBuilderPort {

    private final Path directoryFile;

    public LuceneDataAdapter(IndexerProperties properties) {
        this.directoryFile = Path.of(properties.directory());
    }

    @Override
    public Mono<Void> write(Flux<IndexableDocument> documents) {
        try {
            Directory memoryIndex = FSDirectory.open(directoryFile);
            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(memoryIndex, indexWriterConfig);

            return documents.doOnNext(id -> {
                Document doc = id.accept(new IndexableVisitor<>() {
                    @Override
                    public Document feed(IndexableFeed idxFeed) {
                        try {
                            Document doc = new Document();
                            doc.add(new StringField("id", idxFeed.id(), Field.Store.NO));
                            doc.add(new TextField("title", idxFeed.title(), Field.Store.NO));
                            writer.addDocument(doc);
                            return doc;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Document entry(IndexableFeedEntry idxEntry) {
                        Document doc = new Document();
                        return doc;
                    }
                });
            }).then();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
