/**
 * Copyright 2017 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0
 * Unported License (the "License"); you may not use this file except
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
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

@SuppressWarnings("all")
public class GLP extends PLSQLValidator implements PLSQLCopValidator {
  private HashMap<Integer, PLSQLCopGuideline> guidelines;

  @Override
  public void register(final EValidatorRegistrar registrar) {
    final List<EPackage> ePackages = this.getEPackages();
    Object _get = registrar.getRegistry().get(ePackages.get(0));
    boolean _tripleEquals = (_get == null);
    if (_tripleEquals) {
      super.register(registrar);
    }
  }

  @Override
  public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
    if ((this.guidelines == null)) {
      HashMap<Integer, PLSQLCopGuideline> _hashMap = new HashMap<Integer, PLSQLCopGuideline>();
      this.guidelines = _hashMap;
      Set<Integer> _keySet = super.getGuidelines().keySet();
      for (final Integer k : _keySet) {
        this.guidelines.put(k, super.getGuidelines().get(k));
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Always prefix global variables with \'g_\'.");
      Remediation _createConstantPerIssue = Remediation.createConstantPerIssue(Integer.valueOf(1));
      PLSQLCopGuideline _pLSQLCopGuideline = new PLSQLCopGuideline(Integer.valueOf(9001), _builder.toString(), PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue);
      this.guidelines.put(Integer.valueOf(9001), _pLSQLCopGuideline);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Always prefix local variables with \'l_\'.");
      Remediation _createConstantPerIssue_1 = Remediation.createConstantPerIssue(Integer.valueOf(1));
      PLSQLCopGuideline _pLSQLCopGuideline_1 = new PLSQLCopGuideline(Integer.valueOf(9002), _builder_1.toString(), PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_1);
      this.guidelines.put(Integer.valueOf(9002), _pLSQLCopGuideline_1);
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Always prefix parameters with \'p_\'.");
      Remediation _createConstantPerIssue_2 = Remediation.createConstantPerIssue(Integer.valueOf(1));
      PLSQLCopGuideline _pLSQLCopGuideline_2 = new PLSQLCopGuideline(Integer.valueOf(9003), _builder_2.toString(), PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_2);
      this.guidelines.put(Integer.valueOf(9003), _pLSQLCopGuideline_2);
    }
    return this.guidelines;
  }

  @Check
  public void checkVariableName(final VariableDeclaration v) {
    final EObject parent = v.eContainer().eContainer();
    final String name = v.getVariable().getValue().toLowerCase();
    if (((parent instanceof CreatePackage) || (parent instanceof CreatePackageBody))) {
      boolean _startsWith = name.startsWith("g_");
      boolean _not = (!_startsWith);
      if (_not) {
        this.warning(Integer.valueOf(9001), v.getVariable(), v);
      }
    } else {
      boolean _startsWith_1 = name.startsWith("l_");
      boolean _not_1 = (!_startsWith_1);
      if (_not_1) {
        this.warning(Integer.valueOf(9002), v.getVariable(), v);
      }
    }
  }

  @Check
  public void checkParameterName(final ParameterDeclaration p) {
    boolean _startsWith = p.getParameter().getValue().toLowerCase().startsWith("p_");
    boolean _not = (!_startsWith);
    if (_not) {
      this.warning(Integer.valueOf(9003), p.getParameter(), p);
    }
  }
}
