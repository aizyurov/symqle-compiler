/*
   Copyright 2011-2014 Alexander Izyurov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.package org.symqle.common;
*/

package org.symqle.model;

import org.symqle.parser.ParseException;
import org.symqle.parser.SimpleNode;
import org.symqle.parser.SymqleParser;
import org.symqle.parser.SyntaxTree;
import org.symqle.processor.GrammarException;
import org.symqle.util.Utils;

import java.util.List;
import java.util.Set;

/**
 * Field declaration.
 */
public class FieldDeclaration {
    private final String accessModifier;
    private final Set<String> otherModifiers;
    private final Type type;
    private final List<VariableDeclarator> variables;
    private final String comment;

    /**
     * Construct from String.
     * The string should contain valid field declaration.
     * @param source the string
     * @return new instance
     */
    public static FieldDeclaration parse(final String source) {
        try {
            final SimpleNode simpleNode = SymqleParser.createParser(source).FieldDeclaration();
            SyntaxTree syntaxTree = new SyntaxTree(simpleNode, source);
            return new FieldDeclaration(syntaxTree);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        } catch (GrammarException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Construct from AST.
     * @param node the syntax tree
     * @throws GrammarException wrong tree
     */
    public FieldDeclaration(final SyntaxTree node) throws GrammarException {
        AssertNodeType.assertOneOf(node,
                "FieldDeclaration");
        List<SyntaxTree> modifierNodes = node.find("FieldModifiers");
        this.accessModifier = Utils.getAccessModifier(modifierNodes);
        this.otherModifiers = Utils.getNonAccessModifiers(modifierNodes);
        // mandatory and only one
        this.type = node.find("Type", Type.CONSTRUCT).get(0);
        this.variables = node.find("VariableDeclarator", VariableDeclarator.CONSTRUCT);
        this.comment = node.getComments();
    }

    @Override
    public final String toString() {
        return comment + accessModifier + " " + Utils.format(otherModifiers, "", " ", " ")
                + type + " " + Utils.format(variables, "", ", ", ";");
    }

}
