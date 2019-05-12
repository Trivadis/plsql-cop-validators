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
package com.trivadis.tvdcc.validators

import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel6
import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel7
import com.trivadis.oracle.plsql.plsql.Body
import com.trivadis.oracle.plsql.plsql.ConstructorDeclaration
import com.trivadis.oracle.plsql.plsql.CreateFunction
import com.trivadis.oracle.plsql.plsql.CreateProcedure
import com.trivadis.oracle.plsql.plsql.ExecuteImmediateStatement
import com.trivadis.oracle.plsql.plsql.FuncDeclInType
import com.trivadis.oracle.plsql.plsql.FunctionDefinition
import com.trivadis.oracle.plsql.plsql.OpenForStatement
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration
import com.trivadis.oracle.plsql.plsql.ProcDeclInType
import com.trivadis.oracle.plsql.plsql.ProcedureCallOrAssignmentStatement
import com.trivadis.oracle.plsql.plsql.ProcedureDefinition
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue
import com.trivadis.oracle.plsql.plsql.UserDefinedType
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import com.trivadis.oracle.plsql.validation.PLSQLJavaValidator
import com.trivadis.oracle.plsql.validation.Remediation
import java.util.HashMap
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.Check

class SQLInjection extends PLSQLJavaValidator implements PLSQLCopValidator {
	HashMap<Integer, PLSQLCopGuideline> guidelines

	override getGuidelines() {
		if (guidelines === null) {
			guidelines = new HashMap<Integer, PLSQLCopGuideline>()
			// register guidelines
			guidelines.put(9501,
				new PLSQLCopGuideline(9501, '''Parameter used in string expression of dynamic SQL. Use asserted local variable instead.''', CRITICAL, SECURITY_FEATURES,
					Remediation.createConstantPerIssue(1)))
		}
		return guidelines
	}
	
	def isStringParameter(ParameterDeclaration param) {
		var etype = param.type
		if (etype instanceof UserDefinedType) {
			var udf = etype.userDefinedType
			var node = NodeModelUtils.getNode(udf)
			var nodeText = node.text
			if (!nodeText.toLowerCase.contains("char")) {
				return false
			}
		}
		return true
	}
	
	def isParameter(CreateProcedure proc, SimpleExpressionNameValue n) {
		for (p : proc.params) {
			if (p.parameter.value.equalsIgnoreCase(n.value)) {
				return p.stringParameter
			}
		}
		return false
	}
	
	def isParameter(CreateFunction func, SimpleExpressionNameValue n) {
		for (f : func.params) {
			if (f.parameter.value.equalsIgnoreCase(n.value)) {
				return f.stringParameter
			}
		}
		return false
	}
	
	def isParameter(ProcedureDefinition proc, SimpleExpressionNameValue n) {
		for (p : proc.heading.params) {
			if (p.parameter.value.equalsIgnoreCase(n.value)) {
				return p.stringParameter
			}
		}
		return false
	}

	def isParameter(FunctionDefinition func, SimpleExpressionNameValue n) {
		for (f : func.heading.params) {
			if (f.parameter.value.equalsIgnoreCase(n.value)) {
				return f.stringParameter
			}
		}
		return false
	}

	def isParameter(ProcDeclInType proc, SimpleExpressionNameValue n) {
		for (p : proc.params) {
			if (p.parameter.value.equalsIgnoreCase(n.value)) {
				return p.stringParameter
			}
		}
		return false
	}

	def isParameter(FuncDeclInType func, SimpleExpressionNameValue n) {
		for (f : func.params) {
			if (f.parameter.value.equalsIgnoreCase(n.value)) {
				return f.stringParameter
			}
		}
		return false
	}

	def isParameter(ConstructorDeclaration func, SimpleExpressionNameValue n) {
		for (f : func.params) {
			if (f.parameter.value.equalsIgnoreCase(n.value)) {
				return f.stringParameter
			}
		}
		return false
	}

	def isParameter(SimpleExpressionNameValue n) {
		val parentProcedure = EcoreUtil2.getContainerOfType(n, CreateProcedure)
		if (parentProcedure !== null) {
			return parentProcedure.isParameter(n)
		} else {
			val parentFunction = EcoreUtil2.getContainerOfType(n, CreateFunction)
			if (parentFunction !== null) {
				return parentFunction.isParameter(n)
			} else {
				val parentPackageProcedure = EcoreUtil2.getContainerOfType(n, ProcedureDefinition)
				if (parentPackageProcedure !== null) {
					return parentPackageProcedure.isParameter(n)
				} else {
					val parentPackageFunction = EcoreUtil2.getContainerOfType(n, FunctionDefinition) 
					if (parentPackageFunction !== null) {
						return parentPackageFunction.isParameter(n)
					} else {
						val parentTypeProcedure = EcoreUtil2.getContainerOfType(n, ProcDeclInType)
						if (parentTypeProcedure !== null) {
							return parentTypeProcedure.isParameter(n)
						} else {
							val parentTypeFunction = EcoreUtil2.getContainerOfType(n, FuncDeclInType)
							if (parentTypeFunction !== null) {
								return parentTypeFunction.isParameter(n)
							} else {
								val parentTypeConstructor = EcoreUtil2.getContainerOfType(n, ConstructorDeclaration)
								if (parentTypeConstructor !== null) {
									return parentTypeConstructor.isParameter(n)
								}
							}
						}
					}
				}
			}
		}
		return false
	}
	
	def getQualifiedFunctionName(EObject obj) {
		val expr7 = EcoreUtil2.getContainerOfType(obj, BinaryCompoundExpressionLevel7)
		if (expr7 !== null) {
			val left = expr7.left
			if (left instanceof SimpleExpressionNameValue) {
				val functionName = left.value
				val parent = EcoreUtil2.getContainerOfType(expr7, BinaryCompoundExpressionLevel6)
				if (parent !== null) {
					val parentLeft = parent.left
					if (parentLeft instanceof BinaryCompoundExpressionLevel6) {
						val parentLeftLeft = parentLeft.left
						if (parentLeftLeft instanceof SimpleExpressionNameValue) {
							val schemaName = parentLeftLeft.value
							val parentLeftRight = parentLeft.right
							if (parentLeftRight instanceof SimpleExpressionNameValue) {
								val packageName = parentLeftRight.value
								return ''''«schemaName».«packageName».«functionName»'''
							}
						}
					} else if (parentLeft instanceof SimpleExpressionNameValue) {
						val packageName = parentLeft.value
						return '''«packageName».«functionName»'''
					}
				}
			}
		}
		return ""
	}
	
	
	def isAsserted(SimpleExpressionNameValue n) {
		val body = EcoreUtil2.getContainerOfType(n, Body)
		val usages = EcoreUtil2.getAllContentsOfType(body, SimpleExpressionNameValue).filter[it.value.equalsIgnoreCase(n.value)]
		for (usage : usages) {
			val name = usage.qualifiedFunctionName
			if (name.toLowerCase.contains("dbms_assert.")) {
				return true
			}
		}
		return false
	}

	def void check(SimpleExpressionNameValue n) {
		if (n.isParameter) {
			if (!n.isAsserted) {
				warning(9501, n, n)
			}
		}
		n.checkAssignments
	}
	
	def void checkAssignments(SimpleExpressionNameValue n) {
		val body = EcoreUtil2.getContainerOfType(n, Body)
		val assignments = EcoreUtil2.getAllContentsOfType(body, ProcedureCallOrAssignmentStatement).filter[it.assignment !== null]
		for (assignment : assignments) {
			val varName = assignment.procedureOrTarget?.object
			if (varName instanceof SimpleExpressionNameValue) {
				if (varName.value.equalsIgnoreCase(n.value)) {
					var a = assignment.assignment
					if (a instanceof SimpleExpressionNameValue) {
						a.check
					} else {
						val names = EcoreUtil2.getAllContentsOfType(assignment?.assignment, SimpleExpressionNameValue)
						for (name : names) {
							name.check
						}
					}
				}
			}
		}
	}
	
	def checkAll(EObject obj) {
		if (obj !== null) {
			if (obj instanceof SimpleExpressionNameValue) {
				obj.checkAssignments
			} else {
				val names = EcoreUtil2.getAllContentsOfType(obj, SimpleExpressionNameValue)
				for (name : names) {
					name.check
				}
			}
		}
	}

	@Check
	def checkExecuteImmediate(ExecuteImmediateStatement s) {
		s.statement.checkAll
	}

	@Check
	def checkOpenFor(OpenForStatement s) {
		s.expression?.checkAll
	}

}
