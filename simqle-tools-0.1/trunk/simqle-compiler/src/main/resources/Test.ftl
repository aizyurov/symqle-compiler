/*
* Copyright (c) 2010-2011 Alexander Izyurov
* This file is generated by Simqle code generator
*/

package ${packageName};


public abstract class ${brick.name}AbstractTest {

<#list allMethodNames as methodName>
    public abstract void test_${methodName}() throws Exception;
</#list>
}