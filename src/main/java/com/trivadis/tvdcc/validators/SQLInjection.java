/*
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
import com.trivadis.oracle.plsql.plsql.QualifiedSqlNameExpression;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue;
import com.trivadis.oracle.plsql.plsql.UserDefinedType;
import com.trivadis.oracle.plsql.plsql.VariableDeclaration;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.Remediation;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;

public class SQLInjection extends PLSQLValidator implements PLSQLCopValidator {
    private HashMap<Integer, PLSQLCopGuideline> guidelines;
    private final List<String> ASSERT_PACKAGES = Collections
            .unmodifiableList(CollectionLiterals.newArrayList("dbms_assert", "ut_utils"));

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
        if (guidelines == null) {
            guidelines = new HashMap<>();
            guidelines.put(9501, new PLSQLCopGuideline(9501,
                    "Never use parameter in string expression of dynamic SQL. Use asserted local variable instead.",
                    CRITICAL, SECURITY_FEATURES, Remediation.createConstantPerIssue(1)));
        }
        return guidelines;
    }

    public boolean isStringParameter(ParameterDeclaration param) {
        ElementType etype = param.getType();
        if (etype instanceof UserDefinedType) {
            QualifiedSqlNameExpression udf = ((UserDefinedType) etype).getUserDefinedType();
            ICompositeNode node = NodeModelUtils.getNode(udf);
            String nodeText = node.getText();
            return nodeText.toLowerCase().contains("char");
        }
        return false;
    }

    public boolean isParameter(CreateProcedure proc, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration p : proc.getParams()) {
            if (p.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(p);
            }
        }
        return false;
    }

    public boolean isParameter(CreateFunction func, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration f : func.getParams()) {
            if (f.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(f);
            }
        }
        return false;
    }

    public boolean isParameter(ProcedureDefinition proc, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration p : proc.getHeading().getParams()) {
            if (p.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(p);
            }
        }
        return false;
    }

    public boolean isParameter(FunctionDefinition func, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration f : func.getHeading().getParams()) {
            if (f.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(f);
            }
        }
        return false;
    }

    public boolean isParameter(ProcDeclInType proc, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration p : proc.getParams()) {
            if (p.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(p);
            }
        }
        return false;
    }

    public boolean isParameter(FuncDeclInType func, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration f : func.getParams()) {
            if (f.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(f);
            }
        }
        return false;
    }

    public boolean isParameter(ConstructorDeclaration func, SimpleExpressionNameValue n) {
        for (final ParameterDeclaration f : func.getParams()) {
            if (f.getParameter().getValue().equalsIgnoreCase(n.getValue())) {
                return isStringParameter(f);
            }
        }
        return false;
    }

    public boolean isParameter(SimpleExpressionNameValue n) {
        final CreateProcedure parentProcedure = EcoreUtil2.getContainerOfType(n, CreateProcedure.class);
        if (parentProcedure != null) {
            return isParameter(parentProcedure, n);
        } else {
            final CreateFunction parentFunction = EcoreUtil2.getContainerOfType(n, CreateFunction.class);
            if (parentFunction != null) {
                return isParameter(parentFunction, n);
            } else {
                final ProcedureDefinition parentPackageProcedure = EcoreUtil2.getContainerOfType(n,
                        ProcedureDefinition.class);
                if (parentPackageProcedure != null) {
                    return isParameter(parentPackageProcedure, n);
                } else {
                    final FunctionDefinition parentPackageFunction = EcoreUtil2.getContainerOfType(n,
                            FunctionDefinition.class);
                    if (parentPackageFunction != null) {
                        return isParameter(parentPackageFunction, n);
                    } else {
                        final ProcDeclInType parentTypeProcedure = EcoreUtil2.getContainerOfType(n,
                                ProcDeclInType.class);
                        if (parentTypeProcedure != null) {
                            return isParameter(parentTypeProcedure, n);
                        } else {
                            final FuncDeclInType parentTypeFunction = EcoreUtil2.getContainerOfType(n,
                                    FuncDeclInType.class);
                            if (parentTypeFunction != null) {
                                return isParameter(parentTypeFunction, n);
                            } else {
                                final ConstructorDeclaration parentTypeConstructor = EcoreUtil2.getContainerOfType(n,
                                        ConstructorDeclaration.class);
                                if (parentTypeConstructor != null) {
                                    return isParameter(parentTypeConstructor, n);
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public String getQualifiedFunctionName(EObject obj) {
        final BinaryCompoundExpressionLevel7 expr7 = EcoreUtil2.getContainerOfType(obj,
                BinaryCompoundExpressionLevel7.class);
        if (expr7 != null) {
            final Expression left = expr7.getLeft();
            if (left instanceof SimpleExpressionNameValue) {
                final String functionName = ((SimpleExpressionNameValue) left).getValue();
                final BinaryCompoundExpressionLevel6 parent = EcoreUtil2.getContainerOfType(expr7,
                        BinaryCompoundExpressionLevel6.class);
                if (parent != null) {
                    final Expression parentLeft = parent.getLeft();
                    if (parentLeft instanceof BinaryCompoundExpressionLevel6) {
                        final Expression parentLeftLeft = ((BinaryCompoundExpressionLevel6) parentLeft).getLeft();
                        if (parentLeftLeft instanceof SimpleExpressionNameValue) {
                            final String schemaName = ((SimpleExpressionNameValue) parentLeftLeft).getValue();
                            final Expression parentLeftRight = ((BinaryCompoundExpressionLevel6) parentLeft).getRight();
                            if (parentLeftRight instanceof SimpleExpressionNameValue) {
                                final String packageName = ((SimpleExpressionNameValue) parentLeftRight).getValue();
                                return schemaName + "." + packageName + "." + functionName;
                            }
                        }
                    } else if (parentLeft instanceof SimpleExpressionNameValue) {
                        final String packageName = ((SimpleExpressionNameValue) parentLeft).getValue();
                        return packageName + "." + functionName;
                    }
                }
            }
        }
        return "";
    }

    public boolean contains(EObject obj, final String name) {
        final List<SimpleExpressionNameValue> names = EcoreUtil2.getAllContentsOfType(obj,
                SimpleExpressionNameValue.class);
        for (final SimpleExpressionNameValue n : names) {
            if (n.getValue().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<SimpleExpressionNameValue> getItemsWithDefaults(SimpleExpressionNameValue n) {
        Body body = getBody(n);
        DeclareSection decl;
        if (body == null) {
            decl = EcoreUtil2.getContainerOfType(n, DeclareSection.class);
        } else {
            decl = getDeclareSection(body);
        }
        final ArrayList<SimpleExpressionNameValue> items = new ArrayList<>();
        final List<VariableDeclaration> variables = EcoreUtil2.getAllContentsOfType(decl, VariableDeclaration.class);
        for (final VariableDeclaration v : variables) {
            if (v.getDefault() != null) {
                if (contains(v.getDefault(), n.getValue())) {
                    items.add(v.getVariable());
                }
            }
        }
        final List<ConstantDeclaration> constants = EcoreUtil2.getAllContentsOfType(decl, ConstantDeclaration.class);
        for (final ConstantDeclaration c : constants) {
            if (c.getDefault() != null) {
                if (contains(c.getDefault(), n.getValue())) {
                    items.add(c.getConstant());
                }
            }
        }
        if (body != null) {
            final ArrayList<SimpleExpressionNameValue> bodyItems = new ArrayList<>();
            final List<SimpleExpressionNameValue> names = EcoreUtil2.getAllContentsOfType(body,
                    SimpleExpressionNameValue.class);
            for (final SimpleExpressionNameValue name : names) {
                for (final SimpleExpressionNameValue item : items) {
                    if (name.getValue().equalsIgnoreCase(item.getValue())) {
                        bodyItems.add(name);
                    }
                }
            }
            items.addAll(bodyItems);
        }
        return items;
    }

    public boolean isAsserted(SimpleExpressionNameValue n) {
        EObject obj = EcoreUtil2.getContainerOfType(n, Body.class);
        if (obj == null) {
            obj = EcoreUtil2.getContainerOfType(n, DeclareSection.class);
        }
        final Iterable<SimpleExpressionNameValue> usages = EcoreUtil2
                .getAllContentsOfType(obj, SimpleExpressionNameValue.class).stream()
                .filter(it -> it.getValue().equalsIgnoreCase(n.getValue())).collect(Collectors.toList());
        for (final SimpleExpressionNameValue usage : usages) {
            final String name = getQualifiedFunctionName(usage);
            for (final String assertPackage : ASSERT_PACKAGES) {
                if (name.toLowerCase().contains(assertPackage + ".")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAssertedInParameterOrConstantsOrVariables(SimpleExpressionNameValue n) {
        // check parameter fist
        if (isAsserted(n)) {
            return true;
        }
        // check constants and variables with an assignment of the parameter
        for (final SimpleExpressionNameValue item : getItemsWithDefaults(n)) {
            if (isAsserted(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean isParameterName(SimpleExpressionNameValue n) {
        if (n.eContainer() instanceof FunctionOrParenthesisParameter) {
            final FunctionOrParenthesisParameter param = (FunctionOrParenthesisParameter) n.eContainer();
            return param.getParameterName() == n;
        }
        return false;
    }

    public List<SimpleExpressionNameValue> getRelevantSimpleExpressionNameValues(EObject obj) {
        return EcoreUtil2.getAllContentsOfType(obj, SimpleExpressionNameValue.class).stream()
                .filter(it -> it != null && !isParameterName(it)).collect(Collectors.toList());
    }

    public void check(SimpleExpressionNameValue n, HashMap<String, SimpleExpressionNameValue> expressions) {
        if (!isParameterName(n)) {
            if (isParameter(n)) {
                if (!isAssertedInParameterOrConstantsOrVariables(n)) {
                    warning(9501, n, n);
                    return;
                }
            }
            final HashMap<String, SimpleExpressionNameValue> recursiveExpr = getSimpleExpressinNamesFromAssignments(n);
            final HashMap<String, SimpleExpressionNameValue> newExpressions = new HashMap<>();
            newExpressions.putAll(expressions);
            newExpressions.putAll(recursiveExpr);
            for (final String key : recursiveExpr.keySet()) {
                if (expressions.get(key) == null) {
                    check(recursiveExpr.get(key), newExpressions);
                }
            }
        }
    }

    public DeclareSection getDeclareSection(Body body) {
        final EObject parent = body.eContainer();
        DeclareSection declareSection;
        if (parent instanceof CreateFunction) {
            declareSection = ((CreateFunction) parent).getDeclareSection();
        } else if (parent instanceof CreateProcedure) {
            declareSection = ((CreateProcedure) parent).getDeclareSection();
        } else if (parent instanceof FuncDeclInType) {
            declareSection = ((FuncDeclInType) parent).getDeclareSection();
        } else if (parent instanceof ProcDeclInType) {
            declareSection = ((ProcDeclInType) parent).getDeclareSection();
        } else if (parent instanceof ConstructorDeclaration) {
            declareSection = ((ConstructorDeclaration) parent).getDeclareSection();
        } else if (parent instanceof PlsqlBlock) {
            declareSection = ((PlsqlBlock) parent).getDeclareSection();
        } else if (parent instanceof FunctionDefinition) {
            declareSection = ((FunctionDefinition) parent).getDeclareSection();
        } else if (parent instanceof ProcedureDefinition) {
            declareSection = ((ProcedureDefinition) parent).getDeclareSection();
        } else {
            declareSection = null;
        }

        return declareSection;
    }

    public Body getBody(EObject obj) {
        final EObject parent = obj.eContainer();
        Body body;
        if (parent instanceof CreateFunction) {
            body = ((CreateFunction) parent).getBody();
        } else if (parent instanceof CreateProcedure) {
            body = ((CreateProcedure) parent).getBody();
        } else if (parent instanceof FuncDeclInType) {
            body = ((FuncDeclInType) parent).getBody();
        } else if (parent instanceof ProcDeclInType) {
            body = ((ProcDeclInType) parent).getBody();
        } else if (parent instanceof ConstructorDeclaration) {
            body = ((ConstructorDeclaration) parent).getBody();
        } else if (parent instanceof PlsqlBlock) {
            body = ((PlsqlBlock) parent).getBody();
        } else if (parent instanceof FunctionDefinition) {
            body = ((FunctionDefinition) parent).getBody();
        } else if (parent instanceof ProcedureDefinition) {
            body = ((ProcedureDefinition) parent).getBody();
        } else if (parent == null) {
            body = null;
        } else {
            body = getBody(parent);
        }
        return body;
    }

    public HashMap<String, SimpleExpressionNameValue> getSimpleExpressinNamesFromAssignments(
            final SimpleExpressionNameValue n) {
        final HashMap<String, SimpleExpressionNameValue> expressions = new HashMap<>();
        final Body body = EcoreUtil2.getContainerOfType(n, Body.class);
        List<ProcedureCallOrAssignmentStatement> assignments = EcoreUtil2
                .getAllContentsOfType(body, ProcedureCallOrAssignmentStatement.class).stream()
                .filter(it -> it.getAssignment() != null).collect(Collectors.toList());
        for (final ProcedureCallOrAssignmentStatement assignment : assignments) {
            Expression varName = null;
            if (assignment.getProcedureOrTarget() != null) {
                varName = assignment.getProcedureOrTarget().getObject();
            }
            if (varName instanceof SimpleExpressionNameValue) {
                if (((SimpleExpressionNameValue) varName).getValue().equalsIgnoreCase(n.getValue())) {
                    Condition a = assignment.getAssignment();
                    if (a instanceof SimpleExpressionNameValue) {
                        expressions.put(((SimpleExpressionNameValue) a).getValue().toLowerCase(),
                                (SimpleExpressionNameValue) a);
                    } else {
                        Condition c;
                        c = assignment.getAssignment();
                        for (final SimpleExpressionNameValue name : getRelevantSimpleExpressionNameValues(c)) {
                            expressions.put(name.getValue().toLowerCase(), name);
                        }
                    }
                }
            }
        }
        final DeclareSection declareSection = getDeclareSection(body);
        if (declareSection != null) {
            final Optional<VariableDeclaration> variable = EcoreUtil2
                    .getAllContentsOfType(declareSection, VariableDeclaration.class).stream()
                    .filter(it -> it.getVariable().getValue().equalsIgnoreCase(n.getValue()) && it.getDefault() != null)
                    .findFirst();
            if (variable.isPresent()) {
                Iterable<SimpleExpressionNameValue> _relevantSimplExpressionNameValues = getRelevantSimpleExpressionNameValues(
                        variable.get().getDefault());
                for (final SimpleExpressionNameValue name : _relevantSimplExpressionNameValues) {
                    expressions.put(name.getValue().toLowerCase(), name);
                }
            } else {
                final Optional<ConstantDeclaration> constant = EcoreUtil2
                        .getAllContentsOfType(declareSection, ConstantDeclaration.class).stream()
                        .filter(it -> it.getConstant().getValue().equalsIgnoreCase(n.getValue())
                                && it.getDefault() != null)
                        .findFirst();
                if (constant.isPresent()) {
                    for (final SimpleExpressionNameValue name : getRelevantSimpleExpressionNameValues(
                            constant.get().getDefault())) {
                        expressions.put(name.getValue().toLowerCase(), name);
                    }
                }
            }
        }
        return expressions;
    }

    public void checkAll(EObject obj) {
        final HashMap<String, SimpleExpressionNameValue> expressions = new HashMap<>();
        if (obj != null) {
            if (obj instanceof SimpleExpressionNameValue) {
                expressions.putAll(getSimpleExpressinNamesFromAssignments(((SimpleExpressionNameValue) obj)));
                if (expressions.size() == 0) {
                    expressions.put(((SimpleExpressionNameValue) obj).getValue().toLowerCase(),
                            ((SimpleExpressionNameValue) obj));
                }
            } else {
                for (final SimpleExpressionNameValue name : getRelevantSimpleExpressionNameValues(obj)) {
                    expressions.put(name.getValue().toLowerCase(), name);
                }
            }
        }
        for (final SimpleExpressionNameValue name : expressions.values()) {
            check(name, expressions);
        }
    }

    @Check
    public void checkExecuteImmediate(ExecuteImmediateStatement s) {
        checkAll(s.getStatement());
    }

    @Check
    public void checkOpenFor(OpenForStatement s) {
        if (s.getExpression() != null) {
            checkAll(s.getExpression());
        }
    }
}
