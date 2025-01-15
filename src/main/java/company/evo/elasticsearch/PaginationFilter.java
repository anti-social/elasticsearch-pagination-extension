package company.evo.elasticsearch;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.ActionFilter;
import org.elasticsearch.action.support.ActionFilterChain;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.InternalAggregations;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.internal.InternalSearchResponse;
import org.elasticsearch.search.profile.SearchProfileShardResults;
import org.elasticsearch.tasks.Task;

import java.util.Arrays;

public class PaginationFilter implements ActionFilter {
    private static final int DEFAULT_ORDER = 5;
    public static final Setting<Integer> PAGINATION_FILTER_ORDER = Setting.intSetting(
        "pagination.filter.order", DEFAULT_ORDER, Setting.Property.NodeScope
    );

    private final int order;

    public PaginationFilter(Settings settings) {
        order = PAGINATION_FILTER_ORDER.get(settings);
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void apply(
        Task task,
        String action,
        Request request,
        ActionListener<Response> listener,
        ActionFilterChain<Request, Response> chain
    ) {
        if (!SearchAction.INSTANCE.name().equals(action)) {
            chain.proceed(task, action, request, listener);
            return;
        }

        final var searchRequest = (SearchRequest) request;
        var source = searchRequest.source();
        if (source == null) {
            source = SearchSourceBuilder.searchSource();
        }

        final var from = Math.max(source.from(), 0);
        final var size = source.size() < 0 ? 10 : source.size();
        final var searchExt = source.ext().stream()
            .filter(ext -> ext.getWriteableName().equals(PaginationExtBuilder.NAME))
            .findFirst();
        if (searchExt.isEmpty()) {
            chain.proceed(task, action, request, listener);
            return;
        }

        // To work without other plugins
        source.from(0);
        source.size(from + size);

        @SuppressWarnings("unchecked")
        final ActionListener<Response> rescoreListener = ActionListener.map(listener, (response) -> {
            final var resp = (SearchResponse) response;
            final var searchHits = resp.getHits();
            final var hits = searchHits.getHits();
            if (hits.length == 0) {
                return response;
            }

            final var pageHits = hits.length > from ? Arrays.copyOfRange(
                hits,
                from,
                Math.min(from + size, hits.length)
            ) : new SearchHit[0];

            final var rescoredResponse = new InternalSearchResponse(
                new SearchHits(pageHits, searchHits.getTotalHits(), searchHits.getMaxScore()),
                (InternalAggregations) resp.getAggregations(),
                resp.getSuggest(),
                new SearchProfileShardResults(resp.getProfileResults()),
                resp.isTimedOut(),
                resp.isTerminatedEarly(),
                resp.getNumReducePhases()
            );
            return (Response) new SearchResponse(
                rescoredResponse,
                resp.getScrollId(),
                resp.getTotalShards(),
                resp.getSuccessfulShards(),
                resp.getSkippedShards(),
                resp.getTook().millis(),
                resp.getShardFailures(),
                resp.getClusters()
            );
        });

        chain.proceed(task, action, request, rescoreListener);
    }
}
