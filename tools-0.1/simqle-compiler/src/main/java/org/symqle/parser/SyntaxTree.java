package org.symqle.parser;

import org.symqle.model.F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is a simplified presentation of SimpleNode, containing exactly what is needed for Simqle
 * User: lvovich
 * Date: 13.10.11
 * Time: 14:56
 * To change this template use File | Settings | File Templates.
 */
public class SyntaxTree {

    private final SimpleNode node;
    private final String fileName;

    public SyntaxTree(SimpleNode node, String fileName) {
        this.node = node;
        this.fileName = fileName;
    }

    private static void collectImage(Token token, StringBuilder builder) {
        // break at the top of special tokens or first shell-style comment
      if (token!=null && !token.image.startsWith("#")) {
          Token specialToken = token.specialToken;
          collectImage(specialToken, builder);
          builder.append(token.image);
      }
    }

    public SyntaxTree getParent() {
        final Node parent = node.parent;
        return parent == null ? null : new SyntaxTree((SimpleNode) parent, fileName);
    }

    @Override
    public String toString() {
        return getType() + ":" + getValue();
    }

    public String getType() {
        return node.toString();
    }

    public String getValue() {
        final Token lastToken = node.jjtGetLastToken();
        final Token firstToken = node.jjtGetFirstToken();
        // for empty non-terminals jjTree sets lastToken = preceding token
        // and firstToken = the first token of following non-terminal
        // in this case we set value to null
        return (lastToken !=null && lastToken.next == firstToken) ? "" : firstToken.image;
    }

    public int getLine() {
        return node.jjtGetFirstToken().beginLine;
    }

    public int getColumn() {
        return node.jjtGetFirstToken().beginColumn;
    }

    public String getComments() {
        StringBuilder builder = new StringBuilder();
        collectImage(node.jjtGetFirstToken().specialToken, builder);
        return builder.toString();
    }

    public String getImage() {
        return getComments() + getBody();
    }

    public String getBody() {
        final Token first = node.jjtGetFirstToken();
        final Token last = node.jjtGetLastToken();
        if (last!=null && last.next==first) {
            return "";
        }
        Token current = first;
        final StringBuilder bodyBuilder = new StringBuilder();
        while(true) {
            if (current==first) {
                // skip comments for the first token
                bodyBuilder.append(current.image);
            } else {
                collectImage(current, bodyBuilder);
            }
            if (current==null || current==last) {
                break;
            }
            current = current.next;
        }
        return bodyBuilder.toString();
    }

    public List<SyntaxTree> getChildren() {
        final int numChildren = node.jjtGetNumChildren();
        final List<SyntaxTree> result = new ArrayList<SyntaxTree>(numChildren);
        for (int i=0; i< numChildren; i++) {
            result.add(new SyntaxTree((SimpleNode) node.jjtGetChild(i), fileName));
        }
        return result;
    }

    /**
     * Returns all descendants by a given path
     * By convention, null or empty path refers to <code>this</code>
     * @param path dot-separated types of descendants down the hierarchy
     * @return
     */
    public List<SyntaxTree> find(String path) {
        final List<String> nameList = (path==null || path.equals("")) ?
                Collections.<String>emptyList() :
                Arrays.asList(path.split("\\."));
        return find(nameList);
    }

    public <T, Ex extends Exception> List<T> find(String path, F<SyntaxTree, T, Ex> transformer) throws Ex {
        List<SyntaxTree> nodes = find(path);
        List<T> result = new ArrayList<T>(nodes.size());
        for (SyntaxTree node: nodes) {
            result.add(transformer.apply(node));
        }
        return result;
    }

    private List<SyntaxTree> find(List<String> nameList) {
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

    public String getFileName() {
        return fileName;
    }

    public final static F<SyntaxTree, String, RuntimeException> VALUE =
            new F<SyntaxTree, String, RuntimeException>() {
                @Override
                public String apply(SyntaxTree syntaxTree) throws RuntimeException {
                    return syntaxTree.getValue();
                }
            };

    public final static F<SyntaxTree, String, RuntimeException> BODY =
            new F<SyntaxTree, String, RuntimeException>() {
                @Override
                public String apply(SyntaxTree syntaxTree) throws RuntimeException {
                    return syntaxTree.getBody();
                }
            };

}
