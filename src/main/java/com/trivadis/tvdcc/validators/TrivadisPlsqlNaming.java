/*
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

import com.trivadis.oracle.plsql.plsql.CollectionTypeDefinition;
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration;
import com.trivadis.oracle.plsql.plsql.CreatePackage;
import com.trivadis.oracle.plsql.plsql.CreatePackageBody;
import com.trivadis.oracle.plsql.plsql.CreateType;
import com.trivadis.oracle.plsql.plsql.CursorDeclarationOrDefinition;
import com.trivadis.oracle.plsql.plsql.ExceptionDeclaration;
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration;
import com.trivadis.oracle.plsql.plsql.RecordTypeDefinition;
import com.trivadis.oracle.plsql.plsql.SubTypeDefinition;
import com.trivadis.oracle.plsql.plsql.UserDefinedType;
import com.trivadis.oracle.plsql.plsql.VariableDeclaration;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.Remediation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

public class TrivadisPlsqlNaming extends PLSQLValidator implements PLSQLCopValidator {
    private HashMap<Integer, PLSQLCopGuideline> guidelines;

    public static final String PROPERTIES_FILE_NAME = "TrivadisPlsqlNaming.properties";
    public static final int ISSUE_GLOBAL_VARIABLE_NAME = 9101;
    public static final int ISSUE_LOCAL_VARIABLE_NAME = 9102;
    public static final int ISSUE_CURSOR_NAME = 9103;
    public static final int ISSUE_RECORD_NAME = 9104;
    public static final int ISSUE_ARRAY_NAME = 9105;
    public static final int ISSUE_OBJECT_NAME = 9106;
    public static final int ISSUE_CURSOR_PARAMETER_NAME = 9107;
    public static final int ISSUE_IN_PARAMETER_NAME = 9108;
    public static final int ISSUE_OUT_PARAMETER_NAME = 9109;
    public static final int ISSUE_IN_OUT_PARAMETER_NAME = 9110;
    public static final int ISSUE_RECORD_TYPE_NAME = 9111;
    public static final int ISSUE_ARRAY_TYPE_NAME = 9112;
    public static final int ISSUE_EXCEPTION_NAME = 9113;
    public static final int ISSUE_CONSTANT_NAME = 9114;
    public static final int ISSUE_SUBTYPE_NAME = 9115;

    // default naming conventions, can be overridden via TrivadisPlsqlNaming.properties
    private static String PREFIX_GLOBAL_VARIABLE_NAME = "g_";
    private static String PREFIX_LOCAL_VARIABLE_NAME = "l_";
    private static String PREFIX_CURSOR_NAME = "c_";
    private static String PREFIX_RECORD_NAME = "r_";
    private static String PREFIX_ARRAY_NAME = "t_";
    private static String PREFIX_OBJECT_NAME = "o_";
    private static String PREFIX_CURSOR_PARAMETER_NAME = "p_";
    private static String PREFIX_IN_PARAMETER_NAME = "in_";
    private static String PREFIX_OUT_PARAMETER_NAME = "out_";
    private static String PREFIX_IN_OUT_PARAMETER_NAME = "io_";
    private static String PREFIX_RECORD_TYPE_NAME = "r_";
    private static String SUFFIX_RECORD_TYPE_NAME = "_type";
    private static String PREFIX_ARRAY_TYPE_NAME = "t_";
    private static String SUFFIX_ARRAY_TYPE_NAME = "_type";
    private static String PREFIX_EXCEPTION_NAME = "e_";
    private static String PREFIX_CONSTANT_NAME = "co_";
    private static String SUFFIX_SUBTYPE_NAME = "_type";
    private static String VALID_LOCAL_VARIABLE_NAMES = "^([ij])$";

    public TrivadisPlsqlNaming() {
        super();
        readProperties();
    }

    // must be overridden to avoid duplicate issues when used via ComposedChecks
    @Override
    public void register(EValidatorRegistrar registrar) {
        final List<EPackage> ePackages = getEPackages();
        if (registrar.getRegistry().get(ePackages.get(0)) == null) {
            // standalone validator, default registration required
            super.register(registrar);
        }
    }

    private void readProperties() {
        try {
            final FileInputStream fis = new FileInputStream(
                    System.getProperty("user.home") + File.separator + PROPERTIES_FILE_NAME);
            final Properties prop = new Properties();
            prop.load(fis);
            final List<Field> fields = Arrays.stream(getClass().getDeclaredFields())
                    .filter(it -> it.getName().startsWith("PREFIX_") || it.getName().startsWith("SUFFIX_"))
                    .collect(Collectors.toList());
            for (final Field field : fields) {
                {
                    final Object value = prop.get(field.getName());
                    if (value != null) {
                        field.set(this, prop.get(field.getName()));
                    }
                }
            }
            fis.close();
        } catch (IOException | IllegalArgumentException | IllegalAccessException e) {
            // ignore, see https://github.com/Trivadis/plsql-cop-validators/issues/13
        }
    }

    @Override
    public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
        if (guidelines == null) {
            guidelines = new HashMap<>();
            // inherit all existing guidelines
            for (final Integer k : super.getGuidelines().keySet()) {
                guidelines.put(k, super.getGuidelines().get(k));
            }
            // register custom guidelines
            guidelines.put(ISSUE_GLOBAL_VARIABLE_NAME,
                    new PLSQLCopGuideline(ISSUE_GLOBAL_VARIABLE_NAME,
                            "Always prefix global variables with '" + PREFIX_GLOBAL_VARIABLE_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_LOCAL_VARIABLE_NAME,
                    new PLSQLCopGuideline(ISSUE_LOCAL_VARIABLE_NAME,
                            "Always prefix local variables with '" + PREFIX_LOCAL_VARIABLE_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_CURSOR_NAME,
                    new PLSQLCopGuideline(ISSUE_CURSOR_NAME, "Always prefix cursors with '" + PREFIX_CURSOR_NAME + "'.",
                            MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_RECORD_NAME,
                    new PLSQLCopGuideline(ISSUE_RECORD_NAME, "Always prefix records with '" + PREFIX_RECORD_NAME + "'.",
                            MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_ARRAY_NAME,
                    new PLSQLCopGuideline(ISSUE_ARRAY_NAME,
                            "Always prefix collection types (arrays/tables) with '" + PREFIX_ARRAY_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_OBJECT_NAME,
                    new PLSQLCopGuideline(ISSUE_OBJECT_NAME, "Always prefix objects with '" + PREFIX_OBJECT_NAME + "'.",
                            MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_CURSOR_PARAMETER_NAME,
                    new PLSQLCopGuideline(ISSUE_CURSOR_PARAMETER_NAME,
                            "Always prefix cursor parameters with '" + PREFIX_CURSOR_PARAMETER_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_IN_PARAMETER_NAME,
                    new PLSQLCopGuideline(ISSUE_IN_PARAMETER_NAME,
                            "Always prefix in parameters with '" + PREFIX_IN_PARAMETER_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_OUT_PARAMETER_NAME,
                    new PLSQLCopGuideline(ISSUE_OUT_PARAMETER_NAME,
                            "Always prefix out parameters with '" + PREFIX_OUT_PARAMETER_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_IN_OUT_PARAMETER_NAME,
                    new PLSQLCopGuideline(ISSUE_IN_OUT_PARAMETER_NAME,
                            "Always prefix in/out parameters with '" + PREFIX_IN_OUT_PARAMETER_NAME + "'.", MAJOR,
                            UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_RECORD_TYPE_NAME,
                    new PLSQLCopGuideline(ISSUE_RECORD_TYPE_NAME,
                            "Always prefix record type definitions with '" + PREFIX_RECORD_TYPE_NAME
                                    + "' and add the suffix '" + SUFFIX_RECORD_TYPE_NAME + "'.",
                            MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_ARRAY_TYPE_NAME,
                    new PLSQLCopGuideline(ISSUE_ARRAY_TYPE_NAME,
                            "Always prefix collection type definitions (arrays/tables) with '" + PREFIX_ARRAY_TYPE_NAME
                                    + "' and add the suffix '" + SUFFIX_ARRAY_TYPE_NAME + "'.",
                            MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_EXCEPTION_NAME,
                    new PLSQLCopGuideline(ISSUE_EXCEPTION_NAME,
                            "Always prefix exceptions with '" + PREFIX_EXCEPTION_NAME + "'.", MAJOR, UNDERSTANDABILITY,
                            Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_CONSTANT_NAME,
                    new PLSQLCopGuideline(ISSUE_CONSTANT_NAME,
                            "Always prefix constants with '" + PREFIX_CONSTANT_NAME + "'.", MAJOR, UNDERSTANDABILITY,
                            Remediation.createConstantPerIssue(1)));
            guidelines.put(ISSUE_SUBTYPE_NAME,
                    new PLSQLCopGuideline(ISSUE_SUBTYPE_NAME,
                            "Always prefix subtypes with '" + SUFFIX_SUBTYPE_NAME + "'.", MAJOR, UNDERSTANDABILITY,
                            Remediation.createConstantPerIssue(1)));
        }
        return guidelines;
    }

    private boolean isRowtype(EObject obj) {
        boolean ret = false;
        final List<UserDefinedType> types = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType.class);
        if (types.size() > 0) {
            if (types.get(0).isRefByRowtype()) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean isRecordType(EObject obj) {
        boolean ret = false;
        List<UserDefinedType> allTypes = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType.class);
        UserDefinedType type;
        type = allTypes.get(0);
        if (type != null) {
            List<RecordTypeDefinition> rts = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj),
                    RecordTypeDefinition.class);
            if (rts.size() > 0) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                if (rts.stream().anyMatch(it -> it.getType().getValue().compareToIgnoreCase(typeName) == 0)) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isCollectionType(EObject obj) {
        boolean ret = false;
        List<UserDefinedType> allTypes = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType.class);
        UserDefinedType type;
        type = allTypes.get(0);
        if (type != null) {
            List<CollectionTypeDefinition> cts = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj),
                    CollectionTypeDefinition.class);
            if (cts.size() > 0) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                if (cts.stream().anyMatch(it -> it.getType().getValue().compareToIgnoreCase(typeName) == 0)) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isObjectType(EObject obj) {
        boolean ret = false;
        List<UserDefinedType> allTypes = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType.class);
        UserDefinedType type;
        type = allTypes.get(0);
        if (type != null) {
            List<CreateType> ots = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), CreateType.class)
                    .stream().filter(it -> it.getObjectTypeDef() != null).collect(Collectors.toList());
            if (ots.size() > 0) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                if (ots.stream().anyMatch(it -> it.getType().getValue().compareToIgnoreCase(typeName) == 0)) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isSysRefcursor(VariableDeclaration v) {
        final ICompositeNode node = NodeModelUtils.getNode(v.getType());
        return "sys_refcursor".equalsIgnoreCase(node.getText().trim());
    }

    private boolean isQualifiedUdt(EObject obj) {
        boolean ret = false;
        List<UserDefinedType> allTypes = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType.class);
        UserDefinedType type;
        type = allTypes.get(0);
        if (type != null) {
            if (type.getUserDefinedType().getNames().size() > 1) {
                ret = true;
            }
        }
        return ret;
    }

    @Check
    public void checkVariableName(VariableDeclaration v) {
        final EObject parent = v.eContainer().eContainer();
        final String name = v.getVariable().getValue().toLowerCase();
        if (isSysRefcursor(v)) {
            if (!name.startsWith(PREFIX_CURSOR_NAME)) {
                warning(ISSUE_CURSOR_NAME, v.getVariable(), v);
            }
        } else if (isObjectType(v)) {
            if (!name.startsWith(PREFIX_OBJECT_NAME)) {
                warning(ISSUE_OBJECT_NAME, v.getVariable(), v);
            }
        } else if (isCollectionType(v)) {
            if (!name.startsWith(PREFIX_ARRAY_NAME)) {
                warning(ISSUE_ARRAY_NAME, v.getVariable(), v);
            }
        } else if (isRowtype(v) || isRecordType(v)) {
            if (!name.startsWith(PREFIX_RECORD_NAME)) {
                warning(ISSUE_RECORD_NAME, v.getVariable(), v);
            }
        } else {
            // reduce false positives, skip checking variables base on qualified UDTs
            if (!isQualifiedUdt(v)) {
                if (parent instanceof CreatePackage || parent instanceof CreatePackageBody) {
                    if (!name.startsWith(PREFIX_GLOBAL_VARIABLE_NAME)) {
                        warning(ISSUE_GLOBAL_VARIABLE_NAME, v.getVariable(), v);
                    }
                } else {
                    // reduce false positives, allow cursor/object/array names and common indices i, j
                    if (!name.startsWith(PREFIX_LOCAL_VARIABLE_NAME) && !name.startsWith(PREFIX_CURSOR_NAME)
                            && !name.startsWith(PREFIX_OBJECT_NAME) && !name.startsWith(PREFIX_ARRAY_NAME)
                            && !name.matches(VALID_LOCAL_VARIABLE_NAMES)) {
                        warning(ISSUE_LOCAL_VARIABLE_NAME, v.getVariable(), v);
                    }
                }
            }
        }
    }

    @Check
    public void checkCursorName(CursorDeclarationOrDefinition c) {
        if (!c.getCursor().getValue().toLowerCase().startsWith(PREFIX_CURSOR_NAME)) {
            warning(ISSUE_CURSOR_NAME, c.getCursor());
        }
        for (final ParameterDeclaration p : c.getParams()) {
            if (!p.getParameter().getValue().toLowerCase().startsWith(PREFIX_CURSOR_PARAMETER_NAME)) {
                warning(ISSUE_CURSOR_PARAMETER_NAME, p.getParameter(), p);
            }
        }
    }

    @Check
    public void checkParameterName(ParameterDeclaration p) {
        final EObject parent = p.eContainer();
        if (!(parent instanceof CursorDeclarationOrDefinition)) {
            final String name = p.getParameter().getValue();
            if (!("self".equalsIgnoreCase(name))) {
                if (p.isIn() && p.isOut()) {
                    if (!name.startsWith(PREFIX_IN_OUT_PARAMETER_NAME)) {
                        warning(ISSUE_IN_OUT_PARAMETER_NAME, p.getParameter(), p);
                    }
                } else if (p.isOut()) {
                    if (!name.startsWith(PREFIX_OUT_PARAMETER_NAME)) {
                        warning(ISSUE_OUT_PARAMETER_NAME, p.getParameter(), p);
                    }
                } else {
                    if (!name.startsWith(PREFIX_IN_PARAMETER_NAME)) {
                        warning(ISSUE_IN_PARAMETER_NAME, p.getParameter(), p);
                    }
                }
            }
        }
    }

    @Check
    public void checkRecordTypeName(RecordTypeDefinition rt) {
        final String name = rt.getType().getValue().toLowerCase();
        if (!(name.startsWith(PREFIX_RECORD_TYPE_NAME) && name.endsWith(SUFFIX_RECORD_TYPE_NAME))) {
            warning(ISSUE_RECORD_TYPE_NAME, rt.getType(), rt);
        }
    }

    @Check
    public void checkArrayTypeName(CollectionTypeDefinition ct) {
        final String name = ct.getType().getValue().toLowerCase();
        if (!(name.startsWith(PREFIX_ARRAY_TYPE_NAME) && name.endsWith(SUFFIX_ARRAY_TYPE_NAME))) {
            warning(ISSUE_ARRAY_TYPE_NAME, ct.getType(), ct);
        }
    }

    @Check
    public void checkExceptionName(ExceptionDeclaration e) {
        if (!e.getException().getValue().toLowerCase().startsWith(PREFIX_EXCEPTION_NAME)) {
            warning(ISSUE_EXCEPTION_NAME, e.getException(), e);
        }
    }

    @Check
    public void checkConstantName(ConstantDeclaration co) {
        if (!co.getConstant().getValue().toLowerCase().startsWith(PREFIX_CONSTANT_NAME)) {
            warning(ISSUE_CONSTANT_NAME, co.getConstant(), co);
        }
    }

    @Check
    public void checkSubtypeName(SubTypeDefinition st) {
        if (!st.getType().getValue().toLowerCase().endsWith(SUFFIX_SUBTYPE_NAME)) {
            warning(ISSUE_SUBTYPE_NAME, st.getType(), st);
        }
    }
}
