/**
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel6;
import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel7;
import com.trivadis.oracle.plsql.plsql.Body;
import com.trivadis.oracle.plsql.plsql.Condition;
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration;
import com.trivadis.oracle.plsql.plsql.ConstructorDeclaration;
import com.trivadis.oracle.plsql.plsql.CreateFunction;
import com.trivadis.oracle.plsql.plsql.CreateProcedure;
import com.trivadis.oracle.plsql.plsql.DeclareSection;
import com.trivadis.oracle.plsql.plsql.DefaultClause;
import com.trivadis.oracle.plsql.plsql.ElementType;
import com.trivadis.oracle.plsql.plsql.ExecuteImmediateStatement;
import com.trivadis.oracle.plsql.plsql.Expression;
import com.trivadis.oracle.plsql.plsql.FuncDeclInType;
import com.trivadis.oracle.plsql.plsql.FunctionDefinition;
import com.trivadis.oracle.plsql.plsql.FunctionOrParenthesisParameter;
import com.trivadis.oracle.plsql.plsql.OpenForStatement;
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration;
import com.trivadis.oracle.plsql.plsql.PlsqlBlock;
import com.trivadis.oracle.plsql.plsql.ProcDeclInType;
import com.trivadis.oracle.plsql.plsql.ProcedureCallOrAssignmentStatement;
import com.trivadis.oracle.plsql.plsql.ProcedureDefinition;
import com.trivadis.oracle.plsql.plsql.ProcedureOrTarget;
import com.trivadis.oracle.plsql.plsql.QualifiedSqlNameExpression;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionStringValue;
import com.trivadis.oracle.plsql.plsql.UserDefinedType;
import com.trivadis.oracle.plsql.plsql.VariableDeclaration;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.Remediation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class SQLInjection extends PLSQLValidator implements PLSQLCopValidator {
  private HashMap<Integer, PLSQLCopGuideline> guidelines;

  private final List<String> ASSERT_PACKAGES = Collections.<String>unmodifiableList(CollectionLiterals.<String>newArrayList("dbms_assert", "ut_utils"));

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
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Never use parameter in string expression of dynamic SQL. Use asserted local variable instead.");
      Remediation _createConstantPerIssue = Remediation.createConstantPerIssue(Integer.valueOf(1));
      PLSQLCopGuideline _pLSQLCopGuideline = new PLSQLCopGuideline(Integer.valueOf(9501), _builder.toString(), PLSQLValidator.CRITICAL, PLSQLValidator.SECURITY_FEATURES, _createConstantPerIssue);
      this.guidelines.put(Integer.valueOf(9501), _pLSQLCopGuideline);
    }
    return this.guidelines;
  }

  public boolean isStringParameter(final ParameterDeclaration param) {
    ElementType etype = param.getType();
    if ((etype instanceof UserDefinedType)) {
      QualifiedSqlNameExpression udf = ((UserDefinedType)etype).getUserDefinedType();
      ICompositeNode node = NodeModelUtils.getNode(udf);
      String nodeText = node.getText();
      boolean _contains = nodeText.toLowerCase().contains("char");
      boolean _not = (!_contains);
      if (_not) {
        return false;
      }
    }
    return true;
  }

  public boolean isParameter(final CreateProcedure proc, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = proc.getParams();
    for (final ParameterDeclaration p : _params) {
      boolean _equalsIgnoreCase = p.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(p);
      }
    }
    return false;
  }

  public boolean isParameter(final CreateFunction func, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = func.getParams();
    for (final ParameterDeclaration f : _params) {
      boolean _equalsIgnoreCase = f.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(f);
      }
    }
    return false;
  }

  public boolean isParameter(final ProcedureDefinition proc, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = proc.getHeading().getParams();
    for (final ParameterDeclaration p : _params) {
      boolean _equalsIgnoreCase = p.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(p);
      }
    }
    return false;
  }

  public boolean isParameter(final FunctionDefinition func, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = func.getHeading().getParams();
    for (final ParameterDeclaration f : _params) {
      boolean _equalsIgnoreCase = f.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(f);
      }
    }
    return false;
  }

  public boolean isParameter(final ProcDeclInType proc, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = proc.getParams();
    for (final ParameterDeclaration p : _params) {
      boolean _equalsIgnoreCase = p.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(p);
      }
    }
    return false;
  }

  public boolean isParameter(final FuncDeclInType func, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = func.getParams();
    for (final ParameterDeclaration f : _params) {
      boolean _equalsIgnoreCase = f.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(f);
      }
    }
    return false;
  }

  public boolean isParameter(final ConstructorDeclaration func, final SimpleExpressionNameValue n) {
    EList<ParameterDeclaration> _params = func.getParams();
    for (final ParameterDeclaration f : _params) {
      boolean _equalsIgnoreCase = f.getParameter().getValue().equalsIgnoreCase(n.getValue());
      if (_equalsIgnoreCase) {
        return this.isStringParameter(f);
      }
    }
    return false;
  }

  public boolean isParameter(final SimpleExpressionNameValue n) {
    final CreateProcedure parentProcedure = EcoreUtil2.<CreateProcedure>getContainerOfType(n, CreateProcedure.class);
    if ((parentProcedure != null)) {
      return this.isParameter(parentProcedure, n);
    } else {
      final CreateFunction parentFunction = EcoreUtil2.<CreateFunction>getContainerOfType(n, CreateFunction.class);
      if ((parentFunction != null)) {
        return this.isParameter(parentFunction, n);
      } else {
        final ProcedureDefinition parentPackageProcedure = EcoreUtil2.<ProcedureDefinition>getContainerOfType(n, ProcedureDefinition.class);
        if ((parentPackageProcedure != null)) {
          return this.isParameter(parentPackageProcedure, n);
        } else {
          final FunctionDefinition parentPackageFunction = EcoreUtil2.<FunctionDefinition>getContainerOfType(n, FunctionDefinition.class);
          if ((parentPackageFunction != null)) {
            return this.isParameter(parentPackageFunction, n);
          } else {
            final ProcDeclInType parentTypeProcedure = EcoreUtil2.<ProcDeclInType>getContainerOfType(n, ProcDeclInType.class);
            if ((parentTypeProcedure != null)) {
              return this.isParameter(parentTypeProcedure, n);
            } else {
              final FuncDeclInType parentTypeFunction = EcoreUtil2.<FuncDeclInType>getContainerOfType(n, FuncDeclInType.class);
              if ((parentTypeFunction != null)) {
                return this.isParameter(parentTypeFunction, n);
              } else {
                final ConstructorDeclaration parentTypeConstructor = EcoreUtil2.<ConstructorDeclaration>getContainerOfType(n, ConstructorDeclaration.class);
                if ((parentTypeConstructor != null)) {
                  return this.isParameter(parentTypeConstructor, n);
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  public String getQualifiedFunctionName(final EObject obj) {
    final BinaryCompoundExpressionLevel7 expr7 = EcoreUtil2.<BinaryCompoundExpressionLevel7>getContainerOfType(obj, BinaryCompoundExpressionLevel7.class);
    if ((expr7 != null)) {
      final Expression left = expr7.getLeft();
      if ((left instanceof SimpleExpressionNameValue)) {
        final String functionName = ((SimpleExpressionNameValue)left).getValue();
        final BinaryCompoundExpressionLevel6 parent = EcoreUtil2.<BinaryCompoundExpressionLevel6>getContainerOfType(expr7, BinaryCompoundExpressionLevel6.class);
        if ((parent != null)) {
          final Expression parentLeft = parent.getLeft();
          if ((parentLeft instanceof BinaryCompoundExpressionLevel6)) {
            final Expression parentLeftLeft = ((BinaryCompoundExpressionLevel6)parentLeft).getLeft();
            if ((parentLeftLeft instanceof SimpleExpressionNameValue)) {
              final String schemaName = ((SimpleExpressionNameValue)parentLeftLeft).getValue();
              final Expression parentLeftRight = ((BinaryCompoundExpressionLevel6)parentLeft).getRight();
              if ((parentLeftRight instanceof SimpleExpressionNameValue)) {
                final String packageName = ((SimpleExpressionNameValue)parentLeftRight).getValue();
                StringConcatenation _builder = new StringConcatenation();
                _builder.append("\'");
                _builder.append(schemaName);
                _builder.append(".");
                _builder.append(packageName);
                _builder.append(".");
                _builder.append(functionName);
                return _builder.toString();
              }
            }
          } else {
            if ((parentLeft instanceof SimpleExpressionNameValue)) {
              final String packageName_1 = ((SimpleExpressionNameValue)parentLeft).getValue();
              StringConcatenation _builder_1 = new StringConcatenation();
              _builder_1.append(packageName_1);
              _builder_1.append(".");
              _builder_1.append(functionName);
              return _builder_1.toString();
            }
          }
        }
      }
    }
    return "";
  }

  public boolean contains(final EObject obj, final String name) {
    final List<SimpleExpressionNameValue> names = EcoreUtil2.<SimpleExpressionNameValue>getAllContentsOfType(obj, SimpleExpressionNameValue.class);
    for (final SimpleExpressionNameValue n : names) {
      boolean _equalsIgnoreCase = n.getValue().equalsIgnoreCase(name);
      if (_equalsIgnoreCase) {
        return true;
      }
    }
    return false;
  }

  public ArrayList<SimpleExpressionNameValue> getItemsWithDefaults(final SimpleExpressionNameValue n) {
    Body body = this.getBody(n);
    DeclareSection decl = null;
    if ((body == null)) {
      decl = EcoreUtil2.<DeclareSection>getContainerOfType(n, DeclareSection.class);
    } else {
      decl = this.getDeclareSection(body);
    }
    final ArrayList<SimpleExpressionNameValue> items = new ArrayList<SimpleExpressionNameValue>();
    final List<VariableDeclaration> variables = EcoreUtil2.<VariableDeclaration>getAllContentsOfType(decl, VariableDeclaration.class);
    for (final VariableDeclaration v : variables) {
      DefaultClause _default = v.getDefault();
      boolean _tripleNotEquals = (_default != null);
      if (_tripleNotEquals) {
        boolean _contains = this.contains(v.getDefault(), n.getValue());
        if (_contains) {
          items.add(v.getVariable());
        }
      }
    }
    final List<ConstantDeclaration> constants = EcoreUtil2.<ConstantDeclaration>getAllContentsOfType(decl, ConstantDeclaration.class);
    for (final ConstantDeclaration c : constants) {
      DefaultClause _default_1 = c.getDefault();
      boolean _tripleNotEquals_1 = (_default_1 != null);
      if (_tripleNotEquals_1) {
        boolean _contains_1 = this.contains(c.getDefault(), n.getValue());
        if (_contains_1) {
          items.add(c.getConstant());
        }
      }
    }
    if ((body != null)) {
      final ArrayList<SimpleExpressionNameValue> bodyItems = new ArrayList<SimpleExpressionNameValue>();
      final List<SimpleExpressionNameValue> names = EcoreUtil2.<SimpleExpressionNameValue>getAllContentsOfType(body, SimpleExpressionNameValue.class);
      for (final SimpleExpressionNameValue name : names) {
        for (final SimpleExpressionNameValue item : items) {
          boolean _equalsIgnoreCase = name.getValue().equalsIgnoreCase(item.getValue());
          if (_equalsIgnoreCase) {
            bodyItems.add(name);
          }
        }
      }
      items.addAll(bodyItems);
    }
    return items;
  }

  public boolean isAsserted(final SimpleExpressionNameValue n) {
    EObject obj = EcoreUtil2.<Body>getContainerOfType(n, Body.class);
    if ((obj == null)) {
      obj = EcoreUtil2.<DeclareSection>getContainerOfType(n, DeclareSection.class);
    }
    final Function1<SimpleExpressionNameValue, Boolean> _function = (SimpleExpressionNameValue it) -> {
      return Boolean.valueOf(it.getValue().equalsIgnoreCase(n.getValue()));
    };
    final Iterable<SimpleExpressionNameValue> usages = IterableExtensions.<SimpleExpressionNameValue>filter(EcoreUtil2.<SimpleExpressionNameValue>getAllContentsOfType(obj, SimpleExpressionNameValue.class), _function);
    for (final SimpleExpressionNameValue usage : usages) {
      {
        final String name = this.getQualifiedFunctionName(usage);
        for (final String assertPackage : this.ASSERT_PACKAGES) {
          String _lowerCase = name.toLowerCase();
          StringConcatenation _builder = new StringConcatenation();
          _builder.append(assertPackage);
          _builder.append(".");
          boolean _contains = _lowerCase.contains(_builder);
          if (_contains) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean isAssertedInParameterOrConstantsOrVariables(final SimpleExpressionNameValue n) {
    boolean _isAsserted = this.isAsserted(n);
    if (_isAsserted) {
      return true;
    }
    ArrayList<SimpleExpressionNameValue> _itemsWithDefaults = this.getItemsWithDefaults(n);
    for (final SimpleExpressionNameValue item : _itemsWithDefaults) {
      boolean _isAsserted_1 = this.isAsserted(item);
      if (_isAsserted_1) {
        return true;
      }
    }
    return false;
  }

  public boolean isParameterName(final SimpleExpressionNameValue n) {
    EObject _eContainer = n.eContainer();
    if ((_eContainer instanceof FunctionOrParenthesisParameter)) {
      EObject _eContainer_1 = n.eContainer();
      final FunctionOrParenthesisParameter param = ((FunctionOrParenthesisParameter) _eContainer_1);
      Expression _parameterName = param.getParameterName();
      boolean _tripleEquals = (_parameterName == n);
      if (_tripleEquals) {
        return true;
      }
    }
    return false;
  }

  public Iterable<SimpleExpressionNameValue> getRelevantSimplExpressionNameValues(final EObject obj) {
    final Function1<SimpleExpressionNameValue, Boolean> _function = (SimpleExpressionNameValue it) -> {
      return Boolean.valueOf(((!(it instanceof SimpleExpressionStringValue)) && (!this.isParameterName(it))));
    };
    return IterableExtensions.<SimpleExpressionNameValue>filter(EcoreUtil2.<SimpleExpressionNameValue>getAllContentsOfType(obj, SimpleExpressionNameValue.class), _function);
  }

  public void check(final SimpleExpressionNameValue n, final HashMap<String, SimpleExpressionNameValue> expressions) {
    boolean _isParameterName = this.isParameterName(n);
    boolean _not = (!_isParameterName);
    if (_not) {
      boolean _isParameter = this.isParameter(n);
      if (_isParameter) {
        boolean _isAssertedInParameterOrConstantsOrVariables = this.isAssertedInParameterOrConstantsOrVariables(n);
        boolean _not_1 = (!_isAssertedInParameterOrConstantsOrVariables);
        if (_not_1) {
          this.warning(Integer.valueOf(9501), n, n);
          return;
        }
      }
      final HashMap<String, SimpleExpressionNameValue> recursiveExpressions = this.getSimpleExpressinNamesFromAssignments(n);
      final HashMap<String, SimpleExpressionNameValue> newExpressions = new HashMap<String, SimpleExpressionNameValue>();
      newExpressions.putAll(expressions);
      newExpressions.putAll(recursiveExpressions);
      Set<String> _keySet = recursiveExpressions.keySet();
      for (final String key : _keySet) {
        SimpleExpressionNameValue _get = expressions.get(key);
        boolean _tripleEquals = (_get == null);
        if (_tripleEquals) {
          this.check(recursiveExpressions.get(key), newExpressions);
        }
      }
    }
  }

  public DeclareSection getDeclareSection(final Body body) {
    final EObject parent = body.eContainer();
    DeclareSection declareSection = null;
    if ((parent instanceof CreateFunction)) {
      declareSection = ((CreateFunction)parent).getDeclareSection();
    } else {
      if ((parent instanceof CreateProcedure)) {
        declareSection = ((CreateProcedure)parent).getDeclareSection();
      } else {
        if ((parent instanceof FuncDeclInType)) {
          declareSection = ((FuncDeclInType)parent).getDeclareSection();
        } else {
          if ((parent instanceof ProcDeclInType)) {
            declareSection = ((ProcDeclInType)parent).getDeclareSection();
          } else {
            if ((parent instanceof ConstructorDeclaration)) {
              declareSection = ((ConstructorDeclaration)parent).getDeclareSection();
            } else {
              if ((parent instanceof PlsqlBlock)) {
                declareSection = ((PlsqlBlock)parent).getDeclareSection();
              } else {
                if ((parent instanceof FunctionDefinition)) {
                  declareSection = ((FunctionDefinition)parent).getDeclareSection();
                } else {
                  if ((parent instanceof ProcedureDefinition)) {
                    declareSection = ((ProcedureDefinition)parent).getDeclareSection();
                  } else {
                    declareSection = null;
                  }
                }
              }
            }
          }
        }
      }
    }
    return declareSection;
  }

  public Body getBody(final EObject obj) {
    final EObject parent = obj.eContainer();
    Body body = null;
    if ((parent instanceof CreateFunction)) {
      body = ((CreateFunction)parent).getBody();
    } else {
      if ((parent instanceof CreateProcedure)) {
        body = ((CreateProcedure)parent).getBody();
      } else {
        if ((parent instanceof FuncDeclInType)) {
          body = ((FuncDeclInType)parent).getBody();
        } else {
          if ((parent instanceof ProcDeclInType)) {
            body = ((ProcDeclInType)parent).getBody();
          } else {
            if ((parent instanceof ConstructorDeclaration)) {
              body = ((ConstructorDeclaration)parent).getBody();
            } else {
              if ((parent instanceof PlsqlBlock)) {
                body = ((PlsqlBlock)parent).getBody();
              } else {
                if ((parent instanceof FunctionDefinition)) {
                  body = ((FunctionDefinition)parent).getBody();
                } else {
                  if ((parent instanceof ProcedureDefinition)) {
                    body = ((ProcedureDefinition)parent).getBody();
                  } else {
                    if ((parent == null)) {
                      body = null;
                    } else {
                      body = this.getBody(parent);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return body;
  }

  public HashMap<String, SimpleExpressionNameValue> getSimpleExpressinNamesFromAssignments(final SimpleExpressionNameValue n) {
    final HashMap<String, SimpleExpressionNameValue> expressions = new HashMap<String, SimpleExpressionNameValue>();
    final Body body = EcoreUtil2.<Body>getContainerOfType(n, Body.class);
    final Function1<ProcedureCallOrAssignmentStatement, Boolean> _function = (ProcedureCallOrAssignmentStatement it) -> {
      Condition _assignment = it.getAssignment();
      return Boolean.valueOf((_assignment != null));
    };
    final List<ProcedureCallOrAssignmentStatement> assignments = IterableExtensions.<ProcedureCallOrAssignmentStatement>toList(IterableExtensions.<ProcedureCallOrAssignmentStatement>filter(EcoreUtil2.<ProcedureCallOrAssignmentStatement>getAllContentsOfType(body, ProcedureCallOrAssignmentStatement.class), _function));
    for (final ProcedureCallOrAssignmentStatement assignment : assignments) {
      {
        ProcedureOrTarget _procedureOrTarget = assignment.getProcedureOrTarget();
        Expression _object = null;
        if (_procedureOrTarget!=null) {
          _object=_procedureOrTarget.getObject();
        }
        final Expression varName = _object;
        if ((varName instanceof SimpleExpressionNameValue)) {
          boolean _equalsIgnoreCase = ((SimpleExpressionNameValue)varName).getValue().equalsIgnoreCase(n.getValue());
          if (_equalsIgnoreCase) {
            Condition a = assignment.getAssignment();
            if ((a instanceof SimpleExpressionNameValue)) {
              expressions.put(((SimpleExpressionNameValue)a).getValue().toLowerCase(), ((SimpleExpressionNameValue)a));
            } else {
              Condition _assignment = null;
              if (assignment!=null) {
                _assignment=assignment.getAssignment();
              }
              Iterable<SimpleExpressionNameValue> _relevantSimplExpressionNameValues = this.getRelevantSimplExpressionNameValues(_assignment);
              for (final SimpleExpressionNameValue name : _relevantSimplExpressionNameValues) {
                expressions.put(name.getValue().toLowerCase(), name);
              }
            }
          }
        }
      }
    }
    final DeclareSection declareSection = this.getDeclareSection(body);
    if ((declareSection != null)) {
      final Function1<VariableDeclaration, Boolean> _function_1 = (VariableDeclaration it) -> {
        return Boolean.valueOf((it.getVariable().getValue().equalsIgnoreCase(n.getValue()) && (it.getDefault() != null)));
      };
      EObject varOrConst = IterableExtensions.<VariableDeclaration>findFirst(EcoreUtil2.<VariableDeclaration>getAllContentsOfType(declareSection, VariableDeclaration.class), _function_1);
      if ((varOrConst != null)) {
        Iterable<SimpleExpressionNameValue> _relevantSimplExpressionNameValues = this.getRelevantSimplExpressionNameValues(((VariableDeclaration) varOrConst).getDefault());
        for (final SimpleExpressionNameValue name : _relevantSimplExpressionNameValues) {
          expressions.put(name.getValue().toLowerCase(), name);
        }
      } else {
        final Function1<ConstantDeclaration, Boolean> _function_2 = (ConstantDeclaration it) -> {
          return Boolean.valueOf((it.getConstant().getValue().equalsIgnoreCase(n.getValue()) && (it.getDefault() != null)));
        };
        varOrConst = IterableExtensions.<ConstantDeclaration>findFirst(EcoreUtil2.<ConstantDeclaration>getAllContentsOfType(declareSection, ConstantDeclaration.class), _function_2);
        if ((varOrConst != null)) {
          Iterable<SimpleExpressionNameValue> _relevantSimplExpressionNameValues_1 = this.getRelevantSimplExpressionNameValues(((ConstantDeclaration) varOrConst).getDefault());
          for (final SimpleExpressionNameValue name_1 : _relevantSimplExpressionNameValues_1) {
            expressions.put(name_1.getValue().toLowerCase(), name_1);
          }
        }
      }
    }
    return expressions;
  }

  public void checkAll(final EObject obj) {
    final HashMap<String, SimpleExpressionNameValue> expressions = new HashMap<String, SimpleExpressionNameValue>();
    if ((obj != null)) {
      if ((obj instanceof SimpleExpressionNameValue)) {
        expressions.putAll(this.getSimpleExpressinNamesFromAssignments(((SimpleExpressionNameValue)obj)));
        int _size = expressions.size();
        boolean _equals = (_size == 0);
        if (_equals) {
          expressions.put(((SimpleExpressionNameValue)obj).getValue().toLowerCase(), ((SimpleExpressionNameValue)obj));
        }
      } else {
        Iterable<SimpleExpressionNameValue> _relevantSimplExpressionNameValues = this.getRelevantSimplExpressionNameValues(obj);
        for (final SimpleExpressionNameValue name : _relevantSimplExpressionNameValues) {
          expressions.put(name.getValue().toLowerCase(), name);
        }
      }
    }
    Collection<SimpleExpressionNameValue> _values = expressions.values();
    for (final SimpleExpressionNameValue name_1 : _values) {
      this.check(name_1, expressions);
    }
  }

  @Check
  public void checkExecuteImmediate(final ExecuteImmediateStatement s) {
    this.checkAll(s.getStatement());
  }

  @Check
  public void checkOpenFor(final OpenForStatement s) {
    Expression _expression = s.getExpression();
    if (_expression!=null) {
      this.checkAll(_expression);
    }
  }
}
