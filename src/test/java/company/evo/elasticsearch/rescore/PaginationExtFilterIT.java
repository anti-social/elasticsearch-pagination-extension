package company.evo.elasticsearch.rescore;

import company.evo.elasticsearch.PaginationExtBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.elasticsearch.cluster.metadata.IndexMetadata.SETTING_NUMBER_OF_SHARDS;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;

@ESIntegTestCase.ClusterScope(scope = ESIntegTestCase.Scope.SUITE)
public class PaginationExtFilterIT extends ESIntegTestCase {
    private final static int NUMBER_OF_SHARDS = 2;


    @Test
    public void testEmpty() throws IOException {
        createIndex(NUMBER_OF_SHARDS);

        var resp = client().prepareSearch()
            .setSource(
                SearchSourceBuilder.searchSource()
                    .query(QueryBuilders.matchAllQuery())
                    .ext(List.of(
                        new PaginationExtBuilder()
                    ))
            )
            .get();
        assertHitCount(resp, 0);
    }

    @Test
    public void testPaginationDefaults() throws IOException {
        createIndex(NUMBER_OF_SHARDS);
        populateIndex();

        // Just test double pagination
        var resp = client().prepareSearch()
            .setSource(
                SearchSourceBuilder.searchSource()
                    .query(
                        QueryBuilders.functionScoreQuery(
                            ScoreFunctionBuilders.scriptFunction(
                                new Script("return doc['rank'].value;")
                            )
                        )
                    )
                    .ext(List.of(new PaginationExtBuilder()))
            )
            .get();
        assertHitCount(resp, 6);
        assertOrderedSearchHits(resp, "1", "2", "3", "4", "5", "6");
    }

    @Test
    public void testPagination() throws IOException {
        createIndex(NUMBER_OF_SHARDS);
        populateIndex();

        // Just test double pagination
        var resp = client().prepareSearch()
            .setSource(
                SearchSourceBuilder.searchSource()
                    .query(
                        QueryBuilders.functionScoreQuery(
                            ScoreFunctionBuilders.scriptFunction(
                                new Script("return doc['rank'].value;")
                            )
                        )
                    )
                    .from(2)
                    .size(4)
                    .ext(List.of(new PaginationExtBuilder()))
            )
            .get();
        assertHitCount(resp, 6);
        assertOrderedSearchHits(resp, "3", "4", "5", "6");
    }

    public void testPaginationFromMoreThenTotalHintsSize() throws IOException {
        createIndex(NUMBER_OF_SHARDS);
        populateIndex();

        // Just test double pagination
        var resp = client().prepareSearch()
            .setSource(
                 SearchSourceBuilder.searchSource()
                    .query(
                        QueryBuilders.functionScoreQuery(
                            ScoreFunctionBuilders.scriptFunction(
                                new Script("return doc['rank'].value;")
                            )
                        )
                    )
                    .from(100)
                    .size(4)
                )
                .get();
        assertHitCount(resp, 6);
        assertOrderedSearchHits(resp);
    }

    public void testPaginationFromMoreThenTotalHintsSizeWithPlugin() throws IOException {
        createIndex(NUMBER_OF_SHARDS);
        populateIndex();

        // Just test double pagination
        var resp = client().prepareSearch()
                .setSource(
                    SearchSourceBuilder.searchSource()
                       .query(
                           QueryBuilders.functionScoreQuery(
                               ScoreFunctionBuilders.scriptFunction(
                                   new Script("return doc['rank'].value;")
                               )
                           )
                       )
                       .from(100)
                       .size(4)
                       .ext(List.of(new PaginationExtBuilder()))
                )
                .get();
        assertHitCount(resp, 6);
        assertOrderedSearchHits(resp);
    }

    private void createIndex(int numShards) throws IOException {
        assertAcked(
            prepareCreate("test")
                .setSettings(Settings.builder().put(SETTING_NUMBER_OF_SHARDS, numShards))
                .addMapping("product",
                    jsonBuilder()
                        .startObject()
                          .startObject("product")
                            .startObject("properties")
                              .startObject("rank")
                                .field("type", "float")
                              .endObject()
                            .endObject()
                          .endObject()
                        .endObject())
        );
        ensureYellow();
    }

    private void populateIndex() throws IOException {
        client().prepareIndex("test", "product", "1")
            .setSource(
                "rank", 2.1
            )
            .get();
        client().prepareIndex("test", "product", "2")
            .setSource(
                "rank", 1.9
            )
            .get();
        client().prepareIndex("test", "product", "3")
            .setSource(
                "rank", 1.6
            )
            .get();
        client().prepareIndex("test", "product", "4")
            .setSource(
                "rank", 1.0
            )
            .get();
        client().prepareIndex("test", "product", "5")
            .setSource(
                "rank", 0.8
            )
            .get();
        client().prepareIndex("test", "product", "6")
            .setSource(
                "rank", 0.6
            )
            .get();

        refresh();
        ensureYellow();
    }
}
