/*
* Copyright Alexander Izyurov 2010
*/
package org.simqle.model;

import org.simqle.parser.ParseException;
import org.simqle.parser.SimpleNode;
import org.simqle.parser.SyntaxTree;
import org.simqle.processor.GrammarException;
import org.simqle.util.Utils;

import java.util.*;

/**
 * <br/>13.11.2011
 *
 * @author Alexander Izyurov
 */
public class Model {

    public Model() {
        try {
            addClass(createSimqleClass(SIMQLE_SOURCE));
            addClass(createSimqleClass(SIMQLE_GENERIC_SOURCE));
        } catch (ModelException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

    private final Map<String, AbstractTypeDefinition> classMap = new LinkedHashMap<String, AbstractTypeDefinition>();
    private final Map<String, FactoryMethodModel> factoryMethods = new HashMap<String, FactoryMethodModel>();

    private final Set<String> caseInsensitiveClassNames = new HashSet<String>();

    private final List<MethodDefinition> implicitSimqleMethods = new ArrayList<MethodDefinition>();
    private final List<MethodDefinition> explicitSimqleMethods = new ArrayList<MethodDefinition>();

    public void addImplicitMethod(MethodDefinition method) {
        implicitSimqleMethods.add(method);
    }

    public void addExplicitMethod(MethodDefinition method) {
        explicitSimqleMethods.add(method);
    }

    public List<MethodDefinition> getImplicitSimqleMethods() {
        return implicitSimqleMethods;
    }

    public List<MethodDefinition> getExplicitSimqleMethods() {
        return explicitSimqleMethods;
    }

    public void addInterface(InterfaceDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    private void addClassOrInterface(AbstractTypeDefinition def) throws ModelException {
        final String name = def.getName();
        if (classMap.containsKey(name)) {
            throw new ModelException("Duplicate class name: "+name);
        } else if (caseInsensitiveClassNames.contains(name.toUpperCase())) {
            throw new ModelException("Name duplicate under Windows: "+name);
        }
        caseInsensitiveClassNames.add(name.toUpperCase());
        classMap.put(name, def);
    }

    public InterfaceDefinition getInterface(String name) throws ModelException {
        try {
            return (InterfaceDefinition) classMap.get(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    public AbstractTypeDefinition getAbstractType(String name) throws ModelException {
        final AbstractTypeDefinition def = classMap.get(name);
        if (def == null) {
            throw new ModelException("Type not found: "+name);
        }
        return def;
    }

    public List<InterfaceDefinition> getAllInterfaces() {
        List<InterfaceDefinition> result = new LinkedList<InterfaceDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate instanceof InterfaceDefinition) {
                result.add((InterfaceDefinition) candidate);
            }
        }
        return result;
    }

    public Collection<AbstractTypeDefinition> getAllTypes() {
        return classMap.values();
    }

    public void addClass(ClassDefinition def) throws ModelException {
        addClassOrInterface(def);
    }

    public ClassDefinition getClassDef(String name) throws ModelException {
        try {
            return (ClassDefinition) classMap.get(name);
        } catch (ClassCastException e) {
            throw new ModelException("Not interface: " + name);
        }
    }

    public List<ClassDefinition> getAllClasses() {
        List<ClassDefinition> result = new LinkedList<ClassDefinition>();
        for (AbstractTypeDefinition candidate: classMap.values()) {
            if (candidate instanceof ClassDefinition) {
                result.add((ClassDefinition) candidate);
            }
        }
        return result;
    }

    public List<FactoryMethodModel> getAllFactoryMethods() {
        return new ArrayList<FactoryMethodModel>(factoryMethods.values());
    }

    private static class ClassOrInterface {
        private final ClassDefinition classDefinition;
        private final InterfaceDefinition interfaceDefinition;
        private boolean isInterface;

        private ClassOrInterface(InterfaceDefinition interfaceDefinition) {
            this.interfaceDefinition = interfaceDefinition;
            this.classDefinition = null;
            this.isInterface = true;
        }

        private ClassOrInterface(ClassDefinition classDefinition) {
            this.classDefinition = classDefinition;
            this.interfaceDefinition = null;
            this.isInterface = false;
        }

        private AbstractTypeDefinition getAbstract() {
            return isInterface ? interfaceDefinition : classDefinition;
        }
    }

    public InterfaceDefinition getInterface(Type t) throws ModelException {
        return getInterface(t.getSimpleName());
    }

    public ClassDefinition getClassDef(Type t) throws ModelException {
        return getClassDef(t.getSimpleName());
    }

    private final static String SIMQLE_SOURCE = "public abstract class Simqle {" + Utils.LINE_BREAK +
            "    public static Simqle get() { " + Utils.LINE_BREAK +
            "        return new SimqleGeneric(); " + Utils.LINE_BREAK +
            "    }" + Utils.LINE_BREAK +
            "}";

    private final static String SIMQLE_GENERIC_SOURCE = "import org.simqle.*;" + Utils.LINE_BREAK +
            "import static org.simqle.SqlTerm.*;" + Utils.LINE_BREAK +
            "public class SimqleGeneric extends Simqle {}";

    private static ClassDefinition createSimqleClass(String source) {
        final SimpleNode node;
        try {
            node = Utils.createParser(
                    source
            ).SimqleUnit();
        } catch (ParseException e) {
            throw new RuntimeException("Internal error", e);
        }
        SyntaxTree root = new SyntaxTree(node, "source code");

        final SyntaxTree simqleTree = root.find("SimqleDeclarationBlock.SimqleDeclaration.NormalClassDeclaration").get(0);
        try {
            return new ClassDefinition(simqleTree);
        } catch (GrammarException e) {
            throw new RuntimeException("Internal error", e);
        }
    }

}


