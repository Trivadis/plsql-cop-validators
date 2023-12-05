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

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.plsql.CollectionTypeDefinition;
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration;
import com.trivadis.oracle.plsql.plsql.CreatePackage;
import com.trivadis.oracle.plsql.plsql.CreatePackageBody;
import com.trivadis.oracle.plsql.plsql.CreateType;
import com.trivadis.oracle.plsql.plsql.CursorDeclarationOrDefinition;
import com.trivadis.oracle.plsql.plsql.ExceptionDeclaration;
import com.trivadis.oracle.plsql.plsql.ObjectTypeDef;
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
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
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

    private static String VALID_LOCAL_VARIABLE_NAMES = "^(i|j)$";

    public TrivadisPlsqlNaming() {
        super();
        this.readProperties();
    }

    @Override
    public void register(final EValidatorRegistrar registrar) {
        final List<EPackage> ePackages = this.getEPackages();
        Object _get = registrar.getRegistry().get(ePackages.get(0));
        boolean _tripleEquals = (_get == null);
        if (_tripleEquals) {
            super.register(registrar);
        }
    }

    private Object readProperties() {
        try {
            Object _xtrycatchfinallyexpression = null;
            try {
                String _property = System.getProperty("user.home");
                String _plus = (_property + File.separator);
                String _plus_1 = (_plus + TrivadisPlsqlNaming.PROPERTIES_FILE_NAME);
                final FileInputStream fis = new FileInputStream(_plus_1);
                final Properties prop = new Properties();
                prop.load(fis);
                final Function1<Field, Boolean> _function = (Field it) -> {
                    return Boolean.valueOf((it.getName().startsWith("PREFIX_") || it.getName().startsWith("SUFFIX_")));
                };
                Iterable<Field> _filter = IterableExtensions.<Field>filter(
                        ((Iterable<Field>) Conversions.doWrapArray(this.getClass().getDeclaredFields())), _function);
                for (final Field field : _filter) {
                    {
                        final Object value = prop.get(field.getName());
                        if ((value != null)) {
                            field.set(this, prop.get(field.getName()));
                        }
                    }
                }
                fis.close();
            } catch (final Throwable _t) {
                if (_t instanceof FileNotFoundException) {
                    _xtrycatchfinallyexpression = null;
                } else {
                    throw Exceptions.sneakyThrow(_t);
                }
            }
            return _xtrycatchfinallyexpression;
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
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
            _builder.append("Always prefix global variables with \'");
            _builder.append(TrivadisPlsqlNaming.PREFIX_GLOBAL_VARIABLE_NAME);
            _builder.append("\'.");
            Remediation _createConstantPerIssue = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_GLOBAL_VARIABLE_NAME), _builder.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_GLOBAL_VARIABLE_NAME), _pLSQLCopGuideline);
            StringConcatenation _builder_1 = new StringConcatenation();
            _builder_1.append("Always prefix local variables with \'");
            _builder_1.append(TrivadisPlsqlNaming.PREFIX_LOCAL_VARIABLE_NAME);
            _builder_1.append("\'.");
            Remediation _createConstantPerIssue_1 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_1 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_LOCAL_VARIABLE_NAME), _builder_1.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_1);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_LOCAL_VARIABLE_NAME), _pLSQLCopGuideline_1);
            StringConcatenation _builder_2 = new StringConcatenation();
            _builder_2.append("Always prefix cursors with \'");
            _builder_2.append(TrivadisPlsqlNaming.PREFIX_CURSOR_NAME);
            _builder_2.append("\'.");
            Remediation _createConstantPerIssue_2 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_2 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_NAME), _builder_2.toString(), PLSQLValidator.MAJOR,
                    PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_2);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_NAME), _pLSQLCopGuideline_2);
            StringConcatenation _builder_3 = new StringConcatenation();
            _builder_3.append("Always prefix records with \'");
            _builder_3.append(TrivadisPlsqlNaming.PREFIX_RECORD_NAME);
            _builder_3.append("\'.");
            Remediation _createConstantPerIssue_3 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_3 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_NAME), _builder_3.toString(), PLSQLValidator.MAJOR,
                    PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_3);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_NAME), _pLSQLCopGuideline_3);
            StringConcatenation _builder_4 = new StringConcatenation();
            _builder_4.append("Always prefix collection types (arrays/tables) with \'");
            _builder_4.append(TrivadisPlsqlNaming.PREFIX_ARRAY_NAME);
            _builder_4.append("\'.");
            Remediation _createConstantPerIssue_4 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_4 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_NAME), _builder_4.toString(), PLSQLValidator.MAJOR,
                    PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_4);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_NAME), _pLSQLCopGuideline_4);
            StringConcatenation _builder_5 = new StringConcatenation();
            _builder_5.append("Always prefix objects with \'");
            _builder_5.append(TrivadisPlsqlNaming.PREFIX_OBJECT_NAME);
            _builder_5.append("\'.");
            Remediation _createConstantPerIssue_5 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_5 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OBJECT_NAME), _builder_5.toString(), PLSQLValidator.MAJOR,
                    PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_5);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OBJECT_NAME), _pLSQLCopGuideline_5);
            StringConcatenation _builder_6 = new StringConcatenation();
            _builder_6.append("Always prefix cursor parameters with \'");
            _builder_6.append(TrivadisPlsqlNaming.PREFIX_CURSOR_PARAMETER_NAME);
            _builder_6.append("\'.");
            Remediation _createConstantPerIssue_6 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_6 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_PARAMETER_NAME), _builder_6.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_6);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_PARAMETER_NAME), _pLSQLCopGuideline_6);
            StringConcatenation _builder_7 = new StringConcatenation();
            _builder_7.append("Always prefix in parameters with \'");
            _builder_7.append(TrivadisPlsqlNaming.PREFIX_IN_PARAMETER_NAME);
            _builder_7.append("\'.");
            Remediation _createConstantPerIssue_7 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_7 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_PARAMETER_NAME), _builder_7.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_7);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_PARAMETER_NAME), _pLSQLCopGuideline_7);
            StringConcatenation _builder_8 = new StringConcatenation();
            _builder_8.append("Always prefix out parameters with \'");
            _builder_8.append(TrivadisPlsqlNaming.PREFIX_OUT_PARAMETER_NAME);
            _builder_8.append("\'.");
            Remediation _createConstantPerIssue_8 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_8 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OUT_PARAMETER_NAME), _builder_8.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_8);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OUT_PARAMETER_NAME), _pLSQLCopGuideline_8);
            StringConcatenation _builder_9 = new StringConcatenation();
            _builder_9.append("Always prefix in/out parameters with \'");
            _builder_9.append(TrivadisPlsqlNaming.PREFIX_IN_OUT_PARAMETER_NAME);
            _builder_9.append("\'.");
            Remediation _createConstantPerIssue_9 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_9 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_OUT_PARAMETER_NAME), _builder_9.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_9);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_OUT_PARAMETER_NAME), _pLSQLCopGuideline_9);
            StringConcatenation _builder_10 = new StringConcatenation();
            _builder_10.append("Always prefix record type definitions with \'");
            _builder_10.append(TrivadisPlsqlNaming.PREFIX_RECORD_TYPE_NAME);
            _builder_10.append("\' and add the suffix \'");
            _builder_10.append(TrivadisPlsqlNaming.SUFFIX_RECORD_TYPE_NAME);
            _builder_10.append("\'.");
            Remediation _createConstantPerIssue_10 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_10 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_TYPE_NAME), _builder_10.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_10);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_TYPE_NAME), _pLSQLCopGuideline_10);
            StringConcatenation _builder_11 = new StringConcatenation();
            _builder_11.append("Always prefix collection type definitions (arrays/tables) with \'");
            _builder_11.append(TrivadisPlsqlNaming.PREFIX_ARRAY_TYPE_NAME);
            _builder_11.append("\' and add the suffix \'");
            _builder_11.append(TrivadisPlsqlNaming.SUFFIX_ARRAY_TYPE_NAME);
            _builder_11.append("\'.");
            Remediation _createConstantPerIssue_11 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_11 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_TYPE_NAME), _builder_11.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_11);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_TYPE_NAME), _pLSQLCopGuideline_11);
            StringConcatenation _builder_12 = new StringConcatenation();
            _builder_12.append("Always prefix exceptions with \'");
            _builder_12.append(TrivadisPlsqlNaming.PREFIX_EXCEPTION_NAME);
            _builder_12.append("\'.");
            Remediation _createConstantPerIssue_12 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_12 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_EXCEPTION_NAME), _builder_12.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_12);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_EXCEPTION_NAME), _pLSQLCopGuideline_12);
            StringConcatenation _builder_13 = new StringConcatenation();
            _builder_13.append("Always prefix constants with \'");
            _builder_13.append(TrivadisPlsqlNaming.PREFIX_CONSTANT_NAME);
            _builder_13.append("\'.");
            Remediation _createConstantPerIssue_13 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_13 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CONSTANT_NAME), _builder_13.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_13);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CONSTANT_NAME), _pLSQLCopGuideline_13);
            StringConcatenation _builder_14 = new StringConcatenation();
            _builder_14.append("Always prefix subtypes with \'");
            _builder_14.append(TrivadisPlsqlNaming.SUFFIX_SUBTYPE_NAME);
            _builder_14.append("\'.");
            Remediation _createConstantPerIssue_14 = Remediation.createConstantPerIssue(Integer.valueOf(1));
            PLSQLCopGuideline _pLSQLCopGuideline_14 = new PLSQLCopGuideline(
                    Integer.valueOf(TrivadisPlsqlNaming.ISSUE_SUBTYPE_NAME), _builder_14.toString(),
                    PLSQLValidator.MAJOR, PLSQLValidator.UNDERSTANDABILITY, _createConstantPerIssue_14);
            this.guidelines.put(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_SUBTYPE_NAME), _pLSQLCopGuideline_14);
        }
        return this.guidelines;
    }

    private boolean isRowtype(final EObject obj) {
        boolean ret = false;
        final List<UserDefinedType> types = EcoreUtil2.<UserDefinedType>getAllContentsOfType(obj,
                UserDefinedType.class);
        int _size = types.size();
        boolean _greaterThan = (_size > 0);
        if (_greaterThan) {
            boolean _isRefByRowtype = types.get(0).isRefByRowtype();
            if (_isRefByRowtype) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean isRecordType(final EObject obj) {
        boolean ret = false;
        List<UserDefinedType> _allContentsOfType = EcoreUtil2.<UserDefinedType>getAllContentsOfType(obj,
                UserDefinedType.class);
        UserDefinedType _get = null;
        if (_allContentsOfType != null) {
            _get = _allContentsOfType.get(0);
        }
        final UserDefinedType type = _get;
        if ((type != null)) {
            List<RecordTypeDefinition> rts = EcoreUtil2.<RecordTypeDefinition>getAllContentsOfType(
                    EcoreUtil2.getRootContainer(obj), RecordTypeDefinition.class);
            int _size = rts.size();
            boolean _greaterThan = (_size > 0);
            if (_greaterThan) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                final Function1<RecordTypeDefinition, Boolean> _function = (RecordTypeDefinition it) -> {
                    int _compareToIgnoreCase = it.getType().getValue().compareToIgnoreCase(typeName);
                    return Boolean.valueOf((_compareToIgnoreCase == 0));
                };
                int _size_1 = IterableExtensions.size(IterableExtensions.<RecordTypeDefinition>filter(rts, _function));
                boolean _greaterThan_1 = (_size_1 > 0);
                if (_greaterThan_1) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isCollectionType(final EObject obj) {
        boolean ret = false;
        List<UserDefinedType> _allContentsOfType = EcoreUtil2.<UserDefinedType>getAllContentsOfType(obj,
                UserDefinedType.class);
        UserDefinedType _get = null;
        if (_allContentsOfType != null) {
            _get = _allContentsOfType.get(0);
        }
        final UserDefinedType type = _get;
        if ((type != null)) {
            List<CollectionTypeDefinition> cts = EcoreUtil2.<CollectionTypeDefinition>getAllContentsOfType(
                    EcoreUtil2.getRootContainer(obj), CollectionTypeDefinition.class);
            int _size = cts.size();
            boolean _greaterThan = (_size > 0);
            if (_greaterThan) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                final Function1<CollectionTypeDefinition, Boolean> _function = (CollectionTypeDefinition it) -> {
                    int _compareToIgnoreCase = it.getType().getValue().compareToIgnoreCase(typeName);
                    return Boolean.valueOf((_compareToIgnoreCase == 0));
                };
                int _size_1 = IterableExtensions
                        .size(IterableExtensions.<CollectionTypeDefinition>filter(cts, _function));
                boolean _greaterThan_1 = (_size_1 > 0);
                if (_greaterThan_1) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isObjectType(final EObject obj) {
        boolean ret = false;
        List<UserDefinedType> _allContentsOfType = EcoreUtil2.<UserDefinedType>getAllContentsOfType(obj,
                UserDefinedType.class);
        UserDefinedType _get = null;
        if (_allContentsOfType != null) {
            _get = _allContentsOfType.get(0);
        }
        final UserDefinedType type = _get;
        if ((type != null)) {
            final Function1<CreateType, Boolean> _function = (CreateType it) -> {
                ObjectTypeDef _objectTypeDef = it.getObjectTypeDef();
                return Boolean.valueOf((_objectTypeDef != null));
            };
            Iterable<CreateType> ots = IterableExtensions.<CreateType>filter(
                    EcoreUtil2.<CreateType>getAllContentsOfType(EcoreUtil2.getRootContainer(obj), CreateType.class),
                    _function);
            int _size = IterableExtensions.size(ots);
            boolean _greaterThan = (_size > 0);
            if (_greaterThan) {
                final String typeName = type.getUserDefinedType().getNames().get(0).getValue();
                final Function1<CreateType, Boolean> _function_1 = (CreateType it) -> {
                    int _compareToIgnoreCase = it.getType().getValue().compareToIgnoreCase(typeName);
                    return Boolean.valueOf((_compareToIgnoreCase == 0));
                };
                int _size_1 = IterableExtensions.size(IterableExtensions.<CreateType>filter(ots, _function_1));
                boolean _greaterThan_1 = (_size_1 > 0);
                if (_greaterThan_1) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    private boolean isSysRefcursor(final VariableDeclaration v) {
        final ICompositeNode node = NodeModelUtils.getNode(v.getType());
        String _lowerCase = node.getText().trim().toLowerCase();
        final boolean ret = Objects.equal(_lowerCase, "sys_refcursor");
        return ret;
    }

    private boolean isQualifiedUdt(final EObject obj) {
        boolean ret = false;
        List<UserDefinedType> _allContentsOfType = EcoreUtil2.<UserDefinedType>getAllContentsOfType(obj,
                UserDefinedType.class);
        UserDefinedType _get = null;
        if (_allContentsOfType != null) {
            _get = _allContentsOfType.get(0);
        }
        final UserDefinedType type = _get;
        if ((type != null)) {
            int _size = type.getUserDefinedType().getNames().size();
            boolean _greaterThan = (_size > 1);
            if (_greaterThan) {
                ret = true;
            }
        }
        return ret;
    }

    @Check
    public void checkVariableName(final VariableDeclaration v) {
        final EObject parent = v.eContainer().eContainer();
        final String name = v.getVariable().getValue().toLowerCase();
        boolean _isSysRefcursor = this.isSysRefcursor(v);
        if (_isSysRefcursor) {
            boolean _startsWith = name.startsWith(TrivadisPlsqlNaming.PREFIX_CURSOR_NAME);
            boolean _not = (!_startsWith);
            if (_not) {
                this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_NAME), v.getVariable(), v);
            }
        } else {
            boolean _isObjectType = this.isObjectType(v);
            if (_isObjectType) {
                boolean _startsWith_1 = name.startsWith(TrivadisPlsqlNaming.PREFIX_OBJECT_NAME);
                boolean _not_1 = (!_startsWith_1);
                if (_not_1) {
                    this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OBJECT_NAME), v.getVariable(), v);
                }
            } else {
                boolean _isCollectionType = this.isCollectionType(v);
                if (_isCollectionType) {
                    boolean _startsWith_2 = name.startsWith(TrivadisPlsqlNaming.PREFIX_ARRAY_NAME);
                    boolean _not_2 = (!_startsWith_2);
                    if (_not_2) {
                        this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_NAME), v.getVariable(), v);
                    }
                } else {
                    if ((this.isRowtype(v) || this.isRecordType(v))) {
                        boolean _startsWith_3 = name.startsWith(TrivadisPlsqlNaming.PREFIX_RECORD_NAME);
                        boolean _not_3 = (!_startsWith_3);
                        if (_not_3) {
                            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_NAME), v.getVariable(), v);
                        }
                    } else {
                        boolean _isQualifiedUdt = this.isQualifiedUdt(v);
                        boolean _not_4 = (!_isQualifiedUdt);
                        if (_not_4) {
                            if (((parent instanceof CreatePackage) || (parent instanceof CreatePackageBody))) {
                                boolean _startsWith_4 = name
                                        .startsWith(TrivadisPlsqlNaming.PREFIX_GLOBAL_VARIABLE_NAME);
                                boolean _not_5 = (!_startsWith_4);
                                if (_not_5) {
                                    this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_GLOBAL_VARIABLE_NAME),
                                            v.getVariable(), v);
                                }
                            } else {
                                if ((((((!name.startsWith(TrivadisPlsqlNaming.PREFIX_LOCAL_VARIABLE_NAME))
                                        && (!name.startsWith(TrivadisPlsqlNaming.PREFIX_CURSOR_NAME)))
                                        && (!name.startsWith(TrivadisPlsqlNaming.PREFIX_OBJECT_NAME)))
                                        && (!name.startsWith(TrivadisPlsqlNaming.PREFIX_ARRAY_NAME)))
                                        && (!name.matches(TrivadisPlsqlNaming.VALID_LOCAL_VARIABLE_NAMES)))) {
                                    this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_LOCAL_VARIABLE_NAME),
                                            v.getVariable(), v);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Check
    public void checkCursorName(final CursorDeclarationOrDefinition c) {
        boolean _startsWith = c.getCursor().getValue().toLowerCase().startsWith(TrivadisPlsqlNaming.PREFIX_CURSOR_NAME);
        boolean _not = (!_startsWith);
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_NAME), c.getCursor());
        }
        EList<ParameterDeclaration> _params = c.getParams();
        for (final ParameterDeclaration p : _params) {
            boolean _startsWith_1 = p.getParameter().getValue().toLowerCase()
                    .startsWith(TrivadisPlsqlNaming.PREFIX_CURSOR_PARAMETER_NAME);
            boolean _not_1 = (!_startsWith_1);
            if (_not_1) {
                this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CURSOR_PARAMETER_NAME), p.getParameter(), p);
            }
        }
    }

    @Check
    public void checkParameterName(final ParameterDeclaration p) {
        final EObject parent = p.eContainer();
        if ((!(parent instanceof CursorDeclarationOrDefinition))) {
            final String name = p.getParameter().getValue().toLowerCase();
            boolean _notEquals = (!Objects.equal(name, "self"));
            if (_notEquals) {
                if ((p.isIn() && p.isOut())) {
                    boolean _startsWith = name.startsWith(TrivadisPlsqlNaming.PREFIX_IN_OUT_PARAMETER_NAME);
                    boolean _not = (!_startsWith);
                    if (_not) {
                        this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_OUT_PARAMETER_NAME), p.getParameter(),
                                p);
                    }
                } else {
                    boolean _isOut = p.isOut();
                    if (_isOut) {
                        boolean _startsWith_1 = name.startsWith(TrivadisPlsqlNaming.PREFIX_OUT_PARAMETER_NAME);
                        boolean _not_1 = (!_startsWith_1);
                        if (_not_1) {
                            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_OUT_PARAMETER_NAME),
                                    p.getParameter(), p);
                        }
                    } else {
                        boolean _startsWith_2 = name.startsWith(TrivadisPlsqlNaming.PREFIX_IN_PARAMETER_NAME);
                        boolean _not_2 = (!_startsWith_2);
                        if (_not_2) {
                            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_IN_PARAMETER_NAME), p.getParameter(),
                                    p);
                        }
                    }
                }
            }
        }
    }

    @Check
    public void checkRecordTypeName(final RecordTypeDefinition rt) {
        final String name = rt.getType().getValue().toLowerCase();
        boolean _not = (!(name.startsWith(TrivadisPlsqlNaming.PREFIX_RECORD_TYPE_NAME)
                && name.endsWith(TrivadisPlsqlNaming.SUFFIX_RECORD_TYPE_NAME)));
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_RECORD_TYPE_NAME), rt.getType(), rt);
        }
    }

    @Check
    public void checkArrayTypeName(final CollectionTypeDefinition ct) {
        final String name = ct.getType().getValue().toLowerCase();
        boolean _not = (!(name.startsWith(TrivadisPlsqlNaming.PREFIX_ARRAY_TYPE_NAME)
                && name.endsWith(TrivadisPlsqlNaming.SUFFIX_ARRAY_TYPE_NAME)));
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_ARRAY_TYPE_NAME), ct.getType(), ct);
        }
    }

    @Check
    public void checkExceptionName(final ExceptionDeclaration e) {
        boolean _startsWith = e.getException().getValue().toLowerCase()
                .startsWith(TrivadisPlsqlNaming.PREFIX_EXCEPTION_NAME);
        boolean _not = (!_startsWith);
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_EXCEPTION_NAME), e.getException(), e);
        }
    }

    @Check
    public void checkConstantName(final ConstantDeclaration co) {
        boolean _startsWith = co.getConstant().getValue().toLowerCase()
                .startsWith(TrivadisPlsqlNaming.PREFIX_CONSTANT_NAME);
        boolean _not = (!_startsWith);
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_CONSTANT_NAME), co.getConstant(), co);
        }
    }

    @Check
    public void checkSubtypeName(final SubTypeDefinition st) {
        boolean _endsWith = st.getType().getValue().toLowerCase().endsWith(TrivadisPlsqlNaming.SUFFIX_SUBTYPE_NAME);
        boolean _not = (!_endsWith);
        if (_not) {
            this.warning(Integer.valueOf(TrivadisPlsqlNaming.ISSUE_SUBTYPE_NAME), st.getType(), st);
        }
    }
}
