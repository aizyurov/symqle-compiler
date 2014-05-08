package org.symqle.parser;

import org.symqle.model.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract syntax tree. This is a simplified presentation of SimpleNode, containing exactly what is needed for Symqle.
 */
public class SyntaxTree {

    private final SimpleNode node;
    private final String fileName;

    /**
     * Construct from SimpleNode.
     * @param node source node
     * @param fileName source file name. Used for diagnostics.
     */
    public SyntaxTree(final SimpleNode node, final String fileName) {
        this.node = node;
        this.fileName = fileName;
    }

    private static void collectImage(final Token token, final StringBuilder builder) {
        // break at the top of special tokens or first shell-style comment
      if (token != null && !token.image.startsWith("#")) {
          Token specialToken = token.specialToken;
          collectImage(specialToken, builder);
          builder.append(token.image);
      }
    }

    /**
     * Parent tree.
     * @return parent, null if this is top node.
     */
    public final SyntaxTree getParent() {
        final Node parent = node.parent;
        return parent == null ? null : new SyntaxTree((SimpleNode) parent, fileName);
    }

    @Override
    public final String toString() {
        return getType() + ":" + getValue();
    }

    /**
     * Node type (name of syntax symbol).
     * @return node type.
     */
    public final String getType() {
        return node.toString();
    }

    /**
     * First token.
     * @return the text; null of node is non-terminal symbol with no children.
     */
    public final String getValue() {
        final Token lastToken = node.jjtGetLastToken();
        final Token firstToken = node.jjtGetFirstToken();
        // for empty non-terminals jjTree sets lastToken = preceding token
        // and firstToken = the first token of following non-terminal
        // in this case we set value to null
        return lastToken != null && lastToken.next == firstToken
                ? ""
                : firstToken.image;
    }

    /**
     * Source line number.
     * @return line number
     */
    public final int getLine() {
        return node.jjtGetFirstToken().beginLine;
    }

    /**
     * Source column number.
     * @return column number
     */
    public final int getColumn() {
        return node.jjtGetFirstToken().beginColumn;
    }

    /**
     * All comments preceding {@code this}.
     * Symqle counts only java-style comments end empty lines.
     * Everything from the nearest preceding code or shell-style comment up to {@code this}
     * is included.
     * @return comments
     */
    public final String getComments() {
        StringBuilder builder = new StringBuilder();
        collectImage(node.jjtGetFirstToken().specialToken, builder);
        return builder.toString();
    }

    /**
     * Comments + body.
     * @return comments + body.
     */
    public final String getImage() {
        return getComments() + getBody();
    }

    /**
     * Full text of {@code this}.
     * Preceding comments are excluded.
     * Comments betwee tokens are included.
     * @return text of {@code this}. Empty string for non-terminals with no text and no children.
     */
    public final String getBody() {
        final Token first = node.jjtGetFirstToken();
        final Token last = node.jjtGetLastToken();
        if (last != null && last.next == first) {
            return "";
        }
        Token current = first;
        final StringBuilder bodyBuilder = new StringBuilder();
        while (true) {
            if (current == first) {
                // skip comments for the first token
                bodyBuilder.append(current.image);
            } else {
                collectImage(current, bodyBuilder);
            }
            if (current == null || current == last) {
                break;
            }
            current = current.next;
        }
        return bodyBuilder.toString();
    }

    /**
     * Child nodes.
     * @return child nodes
     */
    public final List<SyntaxTree> getChildren() {
        final int numChildren = node.jjtGetNumChildren();
        final List<SyntaxTree> result = new ArrayList<SyntaxTree>(numChildren);
        for (int i = 0; i < numChildren; i++) {
            result.add(new SyntaxTree((SimpleNode) node.jjtGetChild(i), fileName));
        }
        return result;
    }

    /**
     * Returns all descendants by a given path.
     * By convention, null or empty path refers to <code>this</code>.
     * Path may also include "^", which points to parent.
     * E.g find("^.VariableDeclarator") will find all sibling variable declarators
     * (including {@code this} if it is variable declarator too).
     * @param path dot-separated types of descendants down the hierarchy
     * @return list of found nodes
     */
    public final List<SyntaxTree> find(final String path) {
        final List<String> nameList = path == null || path.equals("")
                ? Collections.<String>emptyList()
                : Arrays.asList(path.split("\\."));
        return find(nameList);
    }

    /**
     * Find and convert to desired type.
     * @param path see {@link #find(String)}
     * @param transformer converting function
     * @param <T> desired type
     * @param <Ex> exception thrown by transformer
     * @return list of objects of required type
     * @throws Ex transformer failed
     */
    public final <T, Ex extends Exception> List<T> find(final String path,
                                                        final F<SyntaxTree, T, Ex> transformer) throws Ex {
        List<SyntaxTree> nodes = find(path);
        List<T> result = new ArrayList<T>(nodes.size());
        for (SyntaxTree currentNode: nodes) {
            result.add(transformer.apply(currentNode));
        }
        return result;
    }

    private List<SyntaxTree> find(final List<String> nameList) {
        if (nameList.isEmpty()) {
            return Collections.singletonList(this);
        } else {
            final List<SyntaxTree> result = new ArrayList<SyntaxTree>();
            final String firstName = nameList.get(0);
            final List<String> otherNames = nameList.subList(1, nameList.size());
            if (firstName.equals("^")) {
                return getParent() == null ? Collections.<SyntaxTree>emptyList() : getParent().find(otherNames);
            } else {
                for (SyntaxTree child: getChildren()) {
                    if (child.getType().equals(firstName)) {
                        result.addAll(child.find(otherNames));
                    }
                }
            }
            return result;
        }
    }

    /**
     * Source file name.
     * @return file name
     */
    public final String getFileName() {
        return fileName;
    }

    /**
     * Function, which converts SyntaxTree to the text of its first token.
     */
    public static final F<SyntaxTree, String, RuntimeException> VALUE =
            new F<SyntaxTree, String, RuntimeException>() {
                @Override
                public String apply(final SyntaxTree syntaxTree) {
                    return syntaxTree.getValue();
                }
            };

    /**
     * Function, which converts SyntaxTree to the text of its full text excluding preceding comment.
     */
    public static final F<SyntaxTree, String, RuntimeException> BODY =
            new F<SyntaxTree, String, RuntimeException>() {
                @Override
                public String apply(final SyntaxTree syntaxTree) {
                    return syntaxTree.getBody();
                }
            };

}
