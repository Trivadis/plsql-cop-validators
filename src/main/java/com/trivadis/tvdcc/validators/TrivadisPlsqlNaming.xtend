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
package com.trivadis.tvdcc.validators

import com.trivadis.oracle.plsql.plsql.CollectionTypeDefinition
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration
import com.trivadis.oracle.plsql.plsql.CreatePackage
import com.trivadis.oracle.plsql.plsql.CreatePackageBody
import com.trivadis.oracle.plsql.plsql.CreateType
import com.trivadis.oracle.plsql.plsql.CursorDeclarationOrDefinition
import com.trivadis.oracle.plsql.plsql.ExceptionDeclaration
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration
import com.trivadis.oracle.plsql.plsql.RecordTypeDefinition
import com.trivadis.oracle.plsql.plsql.SubTypeDefinition
import com.trivadis.oracle.plsql.plsql.UserDefinedType
import com.trivadis.oracle.plsql.plsql.VariableDeclaration
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import com.trivadis.oracle.plsql.validation.PLSQLJavaValidator
import com.trivadis.oracle.plsql.validation.Remediation
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.HashMap
import java.util.Properties
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.EValidatorRegistrar

class TrivadisPlsqlNaming extends PLSQLJavaValidator implements PLSQLCopValidator {
	HashMap<Integer, PLSQLCopGuideline> guidelines

	public static val PROPERTIES_FILE_NAME = "TrivadisPlsqlNaming.properties"

	public static val ISSUE_GLOBAL_VARIABLE_NAME = 9001
	public static val ISSUE_LOCAL_VARIABLE_NAME = 9002
	public static val ISSUE_CURSOR_NAME = 9003
	public static val ISSUE_RECORD_NAME = 9004
	public static val ISSUE_ARRAY_NAME = 9005
	public static val ISSUE_OBJECT_NAME = 9006
	public static val ISSUE_CURSOR_PARAMETER_NAME = 9007
	public static val ISSUE_IN_PARAMETER_NAME = 9008
	public static val ISSUE_OUT_PARAMETER_NAME = 9009
	public static val ISSUE_IN_OUT_PARAMETER_NAME = 9010
	public static val ISSUE_RECORD_TYPE_NAME = 9011
	public static val ISSUE_ARRAY_TYPE_NAME = 9012
	public static val ISSUE_EXCEPTION_NAME = 9013
	public static val ISSUE_CONSTANT_NAME = 9014
	public static val ISSUE_SUBTYPE_NAME = 9015

	static var PREFIX_GLOBAL_VARIABLE_NAME = "g_"
	static var PREFIX_LOCAL_VARIABLE_NAME = "l_"
	static var PREFIX_CURSOR_NAME = "c_"
	static var PREFIX_RECORD_NAME = "r_"
	static var PREFIX_ARRAY_NAME = "t_"
	static var PREFIX_OBJECT_NAME = "o_"
	static var PREFIX_CURSOR_PARAMETER_NAME = "p_"
	static var PREFIX_IN_PARAMETER_NAME = "in_"
	static var PREFIX_OUT_PARAMETER_NAME = "out_"
	static var PREFIX_IN_OUT_PARAMETER_NAME = "io_"
	static var PREFIX_RECORD_TYPE_NAME = "r_"
	static var SUFFIX_RECORD_TYPE_NAME = "_type"
	static var PREFIX_ARRAY_TYPE_NAME = "t_"
	static var SUFFIX_ARRAY_TYPE_NAME = "_type"
	static var PREFIX_EXCEPTION_NAME = "e_"
	static var PREFIX_CONSTANT_NAME = "co_"
	static var SUFFIX_SUBTYPE_NAME = "_type"

	new() {
		super()
		readProperties
	}

	// must be overridden to avoid duplicate issues when used via ComposedChecks 
	override register(EValidatorRegistrar registrar) {
		val ePackages = getEPackages()
		if (registrar.registry.get(ePackages.get(0)) == null) {
			// standalone validator, default registration required
			super.register(registrar);
		}
	}

	def private readProperties() {
		try {
			val input = new FileInputStream(System.getProperty("user.home") + File.separator + com.trivadis.tvdcc.validators.TrivadisPlsqlNaming.PROPERTIES_FILE_NAME)
			val prop = new Properties
			prop.load(input)
			for (field : this.class.declaredFields.filter[it.name.startsWith("PREFIX_") || it.name.startsWith("SUFFIX_")]) {
				val value = prop.get(field.name);
				if (value != null) {
					field.set(this, prop.get(field.name))
				}
			}
			input.close()
		} catch (FileNotFoundException e) {
			// ignore, see https://github.com/Trivadis/plsql-cop-validators/issues/13
		}
	}

	override getGuidelines() {
		if (guidelines === null) {
			guidelines = new HashMap<Integer, PLSQLCopGuideline>()
			// inherit all existing guidelines
			for (k : super.getGuidelines().keySet) {
				guidelines.put(k, super.getGuidelines().get(k))
			}
			// register custom guidelines
			guidelines.put(ISSUE_GLOBAL_VARIABLE_NAME,
				new PLSQLCopGuideline(
					ISSUE_GLOBAL_VARIABLE_NAME, '''Global variables should start with '«PREFIX_GLOBAL_VARIABLE_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_LOCAL_VARIABLE_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_LOCAL_VARIABLE_NAME, '''Local variables should start with '«PREFIX_LOCAL_VARIABLE_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_CURSOR_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_CURSOR_NAME, '''Cursors should start with '«PREFIX_CURSOR_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_RECORD_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_RECORD_NAME, '''Records should start with '«PREFIX_RECORD_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_ARRAY_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_ARRAY_NAME, '''Collection types (arrays/tables) should start with '«PREFIX_ARRAY_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_OBJECT_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_OBJECT_NAME, '''Objects should start with '«PREFIX_OBJECT_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_CURSOR_PARAMETER_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_CURSOR_PARAMETER_NAME, '''Cursor parameters should start with '«PREFIX_CURSOR_PARAMETER_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_IN_PARAMETER_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_IN_PARAMETER_NAME, '''In parameters should start with '«PREFIX_IN_PARAMETER_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_OUT_PARAMETER_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_OUT_PARAMETER_NAME, '''Out parameters should start with '«PREFIX_OUT_PARAMETER_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_IN_OUT_PARAMETER_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_IN_OUT_PARAMETER_NAME, '''In/out parameters should start with '«PREFIX_IN_OUT_PARAMETER_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_RECORD_TYPE_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_RECORD_TYPE_NAME, '''Record Type definitions should start with '«PREFIX_RECORD_TYPE_NAME»' and end with '«SUFFIX_RECORD_TYPE_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_ARRAY_TYPE_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_ARRAY_TYPE_NAME, '''Collection Type definitions (arrays/tables) should start with '«PREFIX_ARRAY_TYPE_NAME»' and end with '«SUFFIX_ARRAY_TYPE_NAME»'.''',
					MAJOR, UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_EXCEPTION_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_EXCEPTION_NAME, '''Exceptions should start with '«PREFIX_EXCEPTION_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_CONSTANT_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_CONSTANT_NAME, '''Constants should start with '«PREFIX_CONSTANT_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
			guidelines.put(TrivadisPlsqlNaming.ISSUE_SUBTYPE_NAME,
				new PLSQLCopGuideline(TrivadisPlsqlNaming.
					ISSUE_SUBTYPE_NAME, '''Subtypes should end with '«SUFFIX_SUBTYPE_NAME»'.''', MAJOR,
					UNDERSTANDABILITY, Remediation.createConstantPerIssue(1)))
		}
		return guidelines
	}

	def private isRowtype(EObject obj) {
		var ret = false
		val types = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType)
		if (types.size > 0) {
			if (types.get(0).refByRowtype) {
				ret = true
			}
		}
		return ret
	}

	def private isRecordType(EObject obj) {
		var ret = false
		val type = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType)?.get(0)
		if (type !== null) {
			var rts = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), RecordTypeDefinition)
			if (rts.size > 0) {
				val typeName = type.userDefinedType.names.get(0).value
				if (rts.filter[it.type.value.compareToIgnoreCase(typeName) == 0].size > 0) {
					ret = true
				}
			}
		}
		return ret
	}

	def private isCollectionType(EObject obj) {
		var ret = false
		val type = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType)?.get(0)
		if (type !== null) {
			var cts = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), CollectionTypeDefinition)
			if (cts.size > 0) {
				val typeName = type.userDefinedType.names.get(0).value
				if (cts.filter[it.type.value.compareToIgnoreCase(typeName) == 0].size > 0) {
					ret = true
				}
			}
		}
		return ret
	}

	def private isObjectType(EObject obj) {
		var ret = false
		val type = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType)?.get(0)
		if (type !== null) {
			var ots = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), CreateType).filter [
				it.objectTypeDef !== null
			]
			if (ots.size > 0) {
				val typeName = type.userDefinedType.names.get(0).value
				if (ots.filter[it.type.value.compareToIgnoreCase(typeName) == 0].size > 0) {
					ret = true
				}
			}
		}
		return ret
	}

	def private isQualifiedUdt(EObject obj) {
		var ret = false
		val type = EcoreUtil2.getAllContentsOfType(obj, UserDefinedType)?.get(0)
		if (type !== null) {
			if (type.userDefinedType.names.size > 1) {
				ret = true
			}
		}
		return ret
	}

	@Check
	def checkVariableName(VariableDeclaration v) {
		val parent = v.eContainer.eContainer
		val name = v.variable.value.toLowerCase
		if (v.isObjectType) {
			if (!name.startsWith(PREFIX_OBJECT_NAME)) {
				warning(ISSUE_OBJECT_NAME, v.variable, v)
			}
		} else if (v.isCollectionType) {
			if (!name.startsWith(PREFIX_ARRAY_NAME)) {
				warning(ISSUE_ARRAY_NAME, v.variable, v)
			}
		} else if (v.isRowtype || v.isRecordType) {
			if (!name.startsWith(PREFIX_RECORD_NAME)) {
				warning(ISSUE_RECORD_NAME, v.variable, v)
			}
		} else {
			// reduce false positives, skip checking variables base on qualified UDTs
			if (!v.isQualifiedUdt) {
				if (parent instanceof CreatePackage || parent instanceof CreatePackageBody) {
					if (!name.startsWith(PREFIX_GLOBAL_VARIABLE_NAME)) {
						warning(ISSUE_GLOBAL_VARIABLE_NAME, v.variable, v)
					}
				} else {
					if (!name.startsWith(PREFIX_LOCAL_VARIABLE_NAME)) {
						warning(ISSUE_LOCAL_VARIABLE_NAME, v.variable, v)
					}
				}
			}
		}
	}

	@Check
	def checkCursorName(CursorDeclarationOrDefinition c) {
		if (!c.cursor.value.toLowerCase.startsWith(PREFIX_CURSOR_NAME)) {
			warning(ISSUE_CURSOR_NAME, c.cursor)
		}
		for (p : c.params) {
			if (!p.parameter.value.toLowerCase.startsWith(PREFIX_CURSOR_PARAMETER_NAME)) {
				warning(ISSUE_CURSOR_PARAMETER_NAME, p.parameter, p)
			}
		}
	}

	@Check
	def checkParameterName(ParameterDeclaration p) {
		val parent = p.eContainer
		if (!(parent instanceof CursorDeclarationOrDefinition)) {
			val name = p.parameter.value.toLowerCase
			if (p.in && p.out) {
				if (!name.startsWith(PREFIX_IN_OUT_PARAMETER_NAME)) {
					warning(ISSUE_IN_OUT_PARAMETER_NAME, p.parameter, p)
				}
			} else if (p.out) {
				if (!name.startsWith(PREFIX_OUT_PARAMETER_NAME)) {
					warning(ISSUE_OUT_PARAMETER_NAME, p.parameter, p)
				}
			} else if (!p.self) {
				if (!name.startsWith(PREFIX_IN_PARAMETER_NAME)) {
					warning(ISSUE_IN_PARAMETER_NAME, p.parameter, p)
				}
			}
		}
	}

	@Check
	def checkRecordTypeName(RecordTypeDefinition rt) {
		val name = rt.type.value.toLowerCase
		if (!(name.startsWith(PREFIX_RECORD_TYPE_NAME) && name.endsWith(SUFFIX_RECORD_TYPE_NAME))) {
			warning(ISSUE_RECORD_TYPE_NAME, rt.type, rt)
		}
	}

	@Check
	def checkArrayTypeName(CollectionTypeDefinition ct) {
		val name = ct.type.value.toLowerCase
		if (!(name.startsWith(PREFIX_ARRAY_TYPE_NAME) && name.endsWith(SUFFIX_ARRAY_TYPE_NAME))) {
			warning(ISSUE_ARRAY_TYPE_NAME, ct.type, ct)
		}
	}

	@Check
	def checkExceptionName(ExceptionDeclaration e) {
		if (!e.exception.value.toLowerCase.startsWith(PREFIX_EXCEPTION_NAME)) {
			warning(ISSUE_EXCEPTION_NAME, e.exception, e)
		}
	}

	@Check
	def checkConstantName(ConstantDeclaration co) {
		if (!co.constant.value.toLowerCase.startsWith(PREFIX_CONSTANT_NAME)) {
			warning(ISSUE_CONSTANT_NAME, co.constant, co)
		}
	}

	@Check
	def checkSubtypeName(SubTypeDefinition st) {
		if (!st.type.value.toLowerCase.endsWith(SUFFIX_SUBTYPE_NAME)) {
			warning(ISSUE_SUBTYPE_NAME, st.type, st)
		}
	}
}
