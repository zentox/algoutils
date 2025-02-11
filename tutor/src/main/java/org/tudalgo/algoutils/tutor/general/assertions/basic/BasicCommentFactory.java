package org.tudalgo.algoutils.tutor.general.assertions.basic;

import org.tudalgo.algoutils.student.CrashException;
import org.tudalgo.algoutils.tutor.general.Environment;
import org.tudalgo.algoutils.tutor.general.assertions.CommentFactory;
import org.tudalgo.algoutils.tutor.general.assertions.Context;
import org.tudalgo.algoutils.tutor.general.assertions.PreCommentSupplier;
import org.tudalgo.algoutils.tutor.general.assertions.Result;

import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.stream;

/**
 * <p>A basic implementation of a comment factory.</p>
 *
 * @author Dustin Glaser
 */
@SuppressWarnings("ClassCanBeRecord")
public final class BasicCommentFactory implements CommentFactory<Result<?, ?, ?, ?>> {

    private static final int TRACE_LINES = 3;

    private final Environment environment;

    /**
     * <p>Constructs a new comment factory with the given environment.</p>
     *
     * @param environment the environment
     */
    public BasicCommentFactory(Environment environment) {
        this.environment = environment;
    }

    @Override
    public <TS extends Result<?, ?, ?, ?>> String comment(TS result, Context context, PreCommentSupplier<? super TS> commentSupplier) {
        if (result.cause() instanceof CrashException) {
            return "not implemented";
        }
        var stringifier = environment.getStringifier();
        var builder = new StringBuilder();
        var trace = (String) null;
        if (result.cause() != null) {
            trace = "\\<div class=\"alert alert-danger\" role=\"alert\"\\>" +
                result.cause().toString() +
                "\n" +
                stream(result.cause().getStackTrace()).limit(TRACE_LINES).map(e -> "\t" + e).collect(Collectors.joining("\n")) +
                "\n...\n" +
                "\\</div\\>";
        }
        // comment
        var comment = commentSupplier != null ? commentSupplier.getPreComment(result) : null;
        if (comment != null) {
            builder.append(e(comment));
        }
        // subject
        var subject = context != null && context.subject() != null ? stringifier.stringify(context.subject()) : null;
        if (subject != null) {
            builder.append(" @ ");
            builder.append(subject);
        }
        // properties
        var properties = context != null ? properties(context, 0) : "";


        if (!properties.isEmpty()) {
            builder.append(" ");
            builder.append(properties);
        }
        if ((comment != null || subject != null || !properties.isEmpty()) && (result.expected().display() || result.actual().display())) {
            builder.append("\\<br\\>");
        }
        // expected
        if (result.expected().display()) {
            builder.append(e("expected")).append(" ").append(result.expected().string(stringifier));
            builder.append("\\<br\\>");
        }
        // actual
        if (result.actual().display()) {
            builder.append(e("actual")).append(" ").append(result.actual().string(stringifier));
            builder.append("\\<br\\>");
        }
        // trace
        if (trace != null) {
            builder.append("\\<br\\>\\<br\\>");
            builder.append(trace);
        }
        return builder.toString().trim();
    }

    private String properties(Context context, int depth) {
        var stringifier = environment.getStringifier();
        var string = context.properties().stream().map(p -> {
            var key = stringifier.stringify(p.key());
            var value = p.value() instanceof Context c ? properties(c, depth + 1) : stringifier.stringify(p.value());
            return format("%s = %s", key, value);
        }).collect(Collectors.joining(",\\<br\\>"));
        if (string.isEmpty()) {
            return "";
        }
        return format("{\\<div class=\"pl-4\"\\>%s\\</div\\>}", string);
    }

    private static String e(String string) {
        return format("\\<b\\>%s\\</b\\>", string);
    }
}



