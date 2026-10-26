package io.github.myknowledges.skills;

final class LinkException extends IllegalArgumentException {

    LinkException(String code, String source, LinkDestination link) {
        super("%s: %s:%d: %s".formatted(code, source, link.line(), link.address()));
    }
}
