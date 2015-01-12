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

import org.symqle.util.Utils;

import java.util.Arrays;

/**
 * Descirbes implicit conversion from one interface to another.
 * @author lvovich
 */
public class ImplicitConversion {

    private final TypeParameters typeParameters;
    private final Type from;
    private final Type to;
    private final MethodDefinition conversionMethod;

    /**
     * Construct an implicit conversion.
     * @param typeParameters type parameters in the current context
     * @param from starting point
     * @param to target interface
     * @param conversionMethod method, which converts {@code from} to {@code to} - a static method of Symqle class.
     * When constructed, this method returns a stub of not fully implemented anonymous class.
     */
    public ImplicitConversion(final TypeParameters typeParameters,
                              final Type from,
                              final Type to,
                              final MethodDefinition conversionMethod) {
        this.typeParameters = typeParameters;
        this.from = from;
        this.to = to;
        this.conversionMethod = conversionMethod;
    }

    /**
     * Type parameters.
     * @return type parameters
     */
    public final TypeParameters getTypeParameters() {
        return typeParameters;
    }

    /**
     * Starting point.
     * @return interface, which should be converted
     */
    public final Type getFrom() {
        return from;
    }

    /**
     * Target interface.
     * @return interface, which we should get as the result of conversion
     */
    public final Type getTo() {
        return to;
    }

    /**
     * Static method of Symqle class, which does the conversion.
     * @return the method
     */
    public final MethodDefinition getConversionMethod() {
        return conversionMethod;
    }

    /**
     * Constructs a helper *SqlBuilder class for this conversion and adds it to model.
     * @param basicInterface target interface
     * @param model the collection of classes and interfaces
     * @return constructed ClassDefinition
     * @throws ModelException wrong model
     */
    public static ClassDefinition getSqlBuilder(final Type basicInterface, final Model model) throws ModelException {
        final InterfaceDefinition anInterface = model.getInterface(basicInterface.getSimpleName());
        final MethodDefinition archetypeMethod = anInterface.getArchetypeMethod();
        String myArchetypeMethodSource = "    public abstract " + archetypeMethod.getResultType()
                + " " + archetypeMethod.getName() + "(final SqlContext context);";
        final String sqlBuilderName = basicInterface.getSimpleName() + "SqlBuilder";
        ClassDefinition builder = model.getClassDef(sqlBuilderName);
        if (builder == null) {
            builder = ClassDefinition.parse("abstract class " + sqlBuilderName
                    + " " + anInterface.getTypeParameters() + " {" + Utils.LINE_BREAK
                    + myArchetypeMethodSource + Utils.LINE_BREAK
                    + "}");
            builder.addImportLines(Arrays.asList(
                    "import org.symqle.common.*;"));
            model.addClass(builder);
        }
        return builder;
    }

    /**
     * Derive helper type from target interface type.
     * @param resultType target type
     * @param model collection of known classes and interfaces
     * @return helper type
     * @throws ModelException wrong model
     */
    public static Type getImplementationType(final Type resultType, final Model model) throws ModelException {
        final ClassDefinition sqlBuilder = getSqlBuilder(resultType, model);
        return new Type(sqlBuilder.getName(), resultType.getTypeArguments(), resultType.getArrayDimensions());
    }
}
