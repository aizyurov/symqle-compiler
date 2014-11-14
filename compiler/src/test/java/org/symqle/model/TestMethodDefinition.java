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

import junit.framework.TestCase;
import org.symqle.util.ModelUtils;

/**
 * @author lvovich
 */
public class TestMethodDefinition extends TestCase {

    public void testEllipsis() throws Exception {
        final Model model = ModelUtils.prepareModel();
        ClassDefinition symqle = model.getClassDef("Symqle");
        MethodDefinition.parse("public abstract RoutineInvocation<T> apply(ValueExpression<?>... arg);", symqle);
    }
}
