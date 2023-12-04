/*
 * Copyright 2017 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0
 * Unported License (the "License"); you may not use file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *     https://creativecommons.org/licenses/by-nc-nd/3.0/
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trivadis.tvdcc.validators;

import com.trivadis.oracle.plsql.plsql.CreatePackage;
import com.trivadis.oracle.plsql.plsql.CreatePackageBody;
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration;
import com.trivadis.oracle.plsql.plsql.VariableDeclaration;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.Remediation;
import java.util.HashMap;
import java.util.List;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

public class GLP extends PLSQLValidator implements PLSQLCopValidator {
    private HashMap<Integer, PLSQLCopGuideline> guidelines;

    // must be overridden to avoid duplicate issues when used via ComposedChecks
    @Override
    public void register(EValidatorRegistrar registrar) {
        final List<EPackage> ePackages = getEPackages();
        if (registrar.getRegistry().get(ePackages.get(0)) == null) {
            // standalone validator, default registration required
            super.register(registrar);
        }
    }

    @Override
    public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
        if ((guidelines == null)) {
            guidelines = new HashMap<>();
            // register parent guidelines
            for (final Integer k : super.getGuidelines().keySet()) {
                guidelines.put(k, super.getGuidelines().get(k));
            }
            // register guidelines
            guidelines.put(9001, new PLSQLCopGuideline(9001, "Always prefix global variables with 'g_'.", MAJOR,
                    UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(9002, new PLSQLCopGuideline(9002, "Always prefix local variables with 'l_'.", MAJOR,
                    UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(9003, new PLSQLCopGuideline(9003, "Always prefix parameters with 'p_'.", MAJOR,
                    UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
        }
        return guidelines;
    }

    @Check
    public void checkVariableName(VariableDeclaration v) {
        final EObject parent = v.eContainer().eContainer();
        final String name = v.getVariable().getValue().toLowerCase();
        if (parent instanceof CreatePackage || parent instanceof CreatePackageBody) {
            if (!name.startsWith("g_")) {
                warning(9001, v.getVariable(), v);
            }
        } else {
            if (!name.startsWith("l_")) {
                warning(9002, v.getVariable(), v);
            }
        }
    }

    @Check
    public void checkParameterName(ParameterDeclaration p) {
        if (!p.getParameter().getValue().toLowerCase().startsWith("p_")) {
            warning(9003, p.getParameter(), p);
        }
    }
}
