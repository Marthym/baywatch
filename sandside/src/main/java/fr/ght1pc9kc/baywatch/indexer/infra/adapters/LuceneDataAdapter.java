package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.indexer.domain.model.Indexable;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexSearcherPort;
import fr.ght1pc9kc.baywatch.indexer.infra.config.IndexerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.DESCRIPTION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LINK;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TITLE;

@Slf4j
@Component
public class LuceneDataAdapter implements IndexBuilderPort, IndexSearcherPort {

    private static final int MAX_SEARCH_RESULT = 10;
    private static final String[] FIELDS = new String[]{TITLE, DESCRIPTION, LINK, "contentTitles", "contentSummaries"};
    private static final Map<String, Float> BOOSTERS = Map.of(
            FIELDS[0], 10f,
            FIELDS[1], 8f,
            FIELDS[2], 10f,
            FIELDS[3], 6f,
            FIELDS[4], 4f
    );

    private final Directory indexDirectory;
    private final StandardAnalyzer analyzer;
    private final AtomicReference<IndexSearcher> indexSearcher;

    private final Executor searchExecutor = Executors.newFixedThreadPool(5, new CustomizableThreadFactory("lucene-search-"));

    public LuceneDataAdapter(IndexerProperties properties) {
        this.indexDirectory = Exceptions.log(log).get(Exceptions.sneak().supplier(() ->
                FSDirectory.open(Path.of(properties.directory())))
        ).orElseThrow();
        this.analyzer = new StandardAnalyzer();
        DirectoryReader reader = Exceptions.log(log).get(Exceptions.sneak().supplier(() ->
                DirectoryReader.open(indexDirectory))
        ).orElseThrow();
        this.indexSearcher = new AtomicReference<>(new IndexSearcher(reader, searchExecutor));

    }

    @Override
    @SuppressWarnings("java:S2095")
    public Mono<Void> write(Flux<Indexable> documents) {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDirectory, indexWriterConfig);
            writer.deleteAll();

            return documents.doOnNext(i -> {
                try {
                    Document doc = new Document();
                    doc.add(new StringField(ID, i.id(), Field.Store.YES));
                    doc.add(new TextField(TITLE, i.title(), Field.Store.NO));
                    if (!Objects.isNull(i.description())) {
                        doc.add(new TextField(DESCRIPTION, i.description(), Field.Store.NO));
                    }
                    doc.add(new TextField(LINK, i.link(), Field.Store.NO));
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
            }).doFinally(signal -> {
                Exceptions.log(log).run(Exceptions.sneak().runnable(() -> {
                    writer.commit();
                    writer.close();
                }));
                Exceptions.log(log).run(Exceptions.sneak().runnable(() -> {
                    DirectoryReader reader = DirectoryReader.open(indexDirectory);
                    IndexSearcher searcher = new IndexSearcher(reader, searchExecutor);
                    this.indexSearcher.set(searcher);
                }));
            }).then();

        } catch (Exception e) {
            log.error("Error while writing Feed index !");
            log.debug("STACKTRACE", e);
            return Mono.empty().then();
        }
    }

    @Override
    public String escapeQuery(String terms) {
        return QueryParserBase.escape(terms);
    }

    @Override
    public Flux<String> search(String terms) {
        IndexSearcher searcher = indexSearcher.get();
        if (searcher == null) {
            log.debug("Index searcher not yet available !");
            return Flux.empty();
        }
        return performFuzzySearch(searcher, terms)
                .map(Exceptions.sneak().function(d -> searcher.getIndexReader().document(d.doc)))
                .map(d -> d.get(ID));
    }

    private Flux<ScoreDoc> performFuzzySearch(IndexSearcher searcher, String terms) {
        try {
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            for (String field : FIELDS) {
                bq.add(new BoostQuery(new FuzzyQuery(new Term(field, terms)), BOOSTERS.get(field)), BooleanClause.Occur.SHOULD);
            }
            TopDocs topDocs = searcher.search(bq.build(), MAX_SEARCH_RESULT);
            explainOnTrace(searcher, bq.build(), topDocs);
            return Flux.just(topDocs.scoreDocs);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }

    private void explainOnTrace(IndexSearcher searcher, Query query, TopDocs tops) {
        if (log.isDebugEnabled()) {
            log.debug("Lucene query {} found {} document(s)", query.toString(), tops.totalHits);
            if (log.isTraceEnabled()) {
                for (ScoreDoc score : tops.scoreDocs) {
                    try {
                        Document document = searcher.getIndexReader().document(score.doc);
                        log.trace("-- {}: {} --------", document.get(ID).substring(0, 10), score.score);
                        Explanation explanation = searcher.explain(query, score.doc);
                        log.trace(explanation.toString());
                        log.trace("------------------------------");
                    } catch (IOException e) {
                        log.trace("{}: {}", e.getClass(), e.getLocalizedMessage());
                    }
                }
                log.trace("------------------------------");
            }
        }
    }
}
