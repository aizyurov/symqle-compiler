/*
* Copyright (c) 2010-2011 Alexander Izyurov
* This file is generated by Simqle code generator
*/

package ${packageName};

<#list imports as import>${import}
</#list>
<#list implementationImports as import>${import}
</#list>

import java.sql.SQLException;

import org.symqle.simqle.*;
import static org.symqle.simqle.SqlTerminal.*;


/**
* This class defines methods, implementing Sql language productions.
* Method names are self-descriptive.
*/

public class GenericSqlFactory extends SqlFactory {

<#list factoryMethods as factoryMethod>
    public ${factoryMethod.methodDeclaration.declarationWithoutModifiers} ${factoryMethod.methodDeclaration.methodBody}
</#list>

}
