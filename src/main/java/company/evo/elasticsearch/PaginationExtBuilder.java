package company.evo.elasticsearch;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.SearchExtBuilder;

import java.io.IOException;
import java.util.Objects;

public class PaginationExtBuilder extends SearchExtBuilder {
    public static final String NAME = "pagination";

    private static final ObjectParser<PaginationExtBuilder, Void> PARSER =
            new ObjectParser<>(NAME, PaginationExtBuilder::new);

    public PaginationExtBuilder() {}

    public PaginationExtBuilder(StreamInput in) {}

    @Override
    public void writeTo(StreamOutput out) {}

    public static PaginationExtBuilder fromXContent(XContentParser parser) {
        return PARSER.apply(parser, null);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.endObject();
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PaginationExtBuilder;
    }
}
