package io.github.myknowledges.skills;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.util.sequence.Escaping;
import lombok.Builder;

@Builder
record LocalAddress(String path, String suffix) {


    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:.*",
            Pattern.DOTALL);

    private static final Pattern ESCAPES = Pattern.compile(
            "\\\\.|&(?:#[xX][0-9A-Fa-f]+|#[0-9]+|[A-Za-z][A-Za-z0-9]*);");


    static Optional<LocalAddress> parse(String address) {
        String value = Escaping.unescapeString(address);
        if (value.isEmpty() || value.startsWith("#") || value.startsWith("?") ||
                value.startsWith("//") || SCHEME.matcher(value).matches()) {
            return Optional.empty();
        }
        int boundary = value.length();
        for (char separator : new char[] {'?', '#'}) {
            int index = value.indexOf(separator);
            if (index >= 0) {
                boundary = Math.min(boundary, index);
            }
        }
        String path = URLDecoder.decode(value.substring(0, boundary).replace("+", "%2B"),
                StandardCharsets.UTF_8);
        return Optional.of(LocalAddress.builder().path(path)
                .suffix(address.substring(originalOffset(address, boundary))).build());
    }

    private static int originalOffset(String address, int boundary) {
        // Retain the original spelling, including entity-escaped query/fragment delimiters.
        Matcher escapes = ESCAPES.matcher(address);
        int removed = 0;
        while (escapes.find() && escapes.start() - removed < boundary) {
            removed += escapes.group().length() - Escaping.unescapeString(escapes.group()).length();
        }
        return boundary + removed;
    }

    static String encodePath(String path) {
        try {
            return new URI(null, null, path, null).toASCIIString()
                    .replace("?", "%3F").replace("#", "%23")
                    .replace("(", "%28").replace(")", "%29").replace("&", "%26");
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Cannot encode local path: " + path, exception);
        }
    }
}
