package io.github.myknowledges.skills;

import com.vladsch.flexmark.util.ast.Node;

/** Supplies metadata without changing the source document. */
public interface DescriptionProvider {

    String describe(Node document);
}
