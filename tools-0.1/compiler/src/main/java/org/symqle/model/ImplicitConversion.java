package org.symqle.model;

import org.symqle.util.Utils;

import java.util.Arrays;

/**
 * @author lvovich
 */
public class ImplicitConversion {

    private final TypeParameters typeParameters;
    private final Type from;
    private final Type to;
    private final MethodDefinition conversionMethod;

    public ImplicitConversion(final TypeParameters typeParameters, final Type from, final Type to, final MethodDefinition conversionMethod) {
        this.typeParameters = typeParameters;
        this.from = from;
        this.to = to;
        this.conversionMethod = conversionMethod;
    }

    public TypeParameters getTypeParameters() {
        return typeParameters;
    }

    public Type getFrom() {
        return from;
    }

    public Type getTo() {
        return to;
    }

    public MethodDefinition getConversionMethod() {
        return conversionMethod;
    }

    public static ClassDefinition getSqlBuilder(final Type basicInterface, final Model model) throws ModelException {
        final InterfaceDefinition anInterface = model.getInterface(basicInterface.getSimpleName());
        final MethodDefinition archetypeMethod = anInterface.getArchetypeMethod();
        String myArchetypeMethodSource = "    public abstract " + archetypeMethod.getResultType() + " " + archetypeMethod.getName() + "(final SqlContext context);";
        final String sqlBuilderName = basicInterface.getSimpleName()+"SqlBuilder";
        ClassDefinition builder = model.getClassDef(sqlBuilderName);
        if (builder == null) {
            builder = ClassDefinition.parse("abstract class " + sqlBuilderName + " " + anInterface.getTypeParameters() + " {" + Utils.LINE_BREAK
                    + myArchetypeMethodSource + Utils.LINE_BREAK +
                    "}");
            builder.addImportLines(Arrays.asList("import org.symqle.common.*;"));
            model.addClass(builder);
        }
        return builder;
    }

    public static Type getImplementationType(final Type resultType, final Model model) throws ModelException {
        final ClassDefinition sqlBuilder = getSqlBuilder(resultType, model);
        return new Type(sqlBuilder.getName(), resultType.getTypeArguments(), resultType.getArrayDimensions());
    }
}
