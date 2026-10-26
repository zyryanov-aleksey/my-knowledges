package io.github.myknowledges.skills;

import lombok.Builder;

@Builder
record LinkDestination(int start, int end, int line, String address) {
}
