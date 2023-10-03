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
import com.trivadis.oracle.plsql.plsql.DeclareSection
import com.trivadis.oracle.plsql.plsql.ExecuteImmediateStatement
import com.trivadis.oracle.plsql.plsql.FuncDeclInType
import com.trivadis.oracle.plsql.plsql.FunctionDefinition
import com.trivadis.oracle.plsql.plsql.FunctionOrParenthesisParameter
import com.trivadis.oracle.plsql.plsql.OpenForStatement
import com.trivadis.oracle.plsql.plsql.ParameterDeclaration
import com.trivadis.oracle.plsql.plsql.PlsqlBlock
import com.trivadis.oracle.plsql.plsql.ProcDeclInType
import com.trivadis.oracle.plsql.plsql.ProcedureCallOrAssignmentStatement
import com.trivadis.oracle.plsql.plsql.ProcedureDefinition
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue
import com.trivadis.oracle.plsql.plsql.SimpleExpressionStringValue
import com.trivadis.oracle.plsql.plsql.UserDefinedType
import com.trivadis.oracle.plsql.plsql.VariableDeclaration
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import com.trivadis.oracle.plsql.validation.PLSQLValidator
import com.trivadis.oracle.plsql.validation.Remediation
import java.util.HashMap
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.EValidatorRegistrar
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration

class SQLInjection extends PLSQLValidator implements PLSQLCopValidator {
	HashMap<Integer, PLSQLCopGuideline> guidelines
	val ASSERT_PACKAGES = #["dbms_assert", "ut_utils"]

	// must be overridden to avoid duplicate issues when used via ComposedChecks 
	override register(EValidatorRegistrar registrar) {
		val ePackages = getEPackages()
		if (registrar.registry.get(ePackages.get(0)) === null) {
			// standalone validator, default registration required
			super.register(registrar);
		}
	}

	override getGuidelines() {
		if (guidelines === null) {
			guidelines = new HashMap<Integer, PLSQLCopGuideline>()
			// register guidelines
			guidelines.put(9501,
				new PLSQLCopGuideline(9501, '''Never use parameter in string expression of dynamic SQL. Use asserted local variable instead.''', CRITICAL, SECURITY_FEATURES,
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
		var EObject obj = EcoreUtil2.getContainerOfType(n, Body)
		if (obj === null) {
			obj = EcoreUtil2.getContainerOfType(n, DeclareSection)
		}
		val usages = EcoreUtil2.getAllContentsOfType(obj, SimpleExpressionNameValue).filter[it.value.equalsIgnoreCase(n.value)]
		for (usage : usages) {
			val name = usage.qualifiedFunctionName
			for (assertPackage : ASSERT_PACKAGES) {
				if (name.toLowerCase.contains('''«assertPackage».''')) {
					return true
				}
			}
		}
		return false
	}

	def isParameterName(SimpleExpressionNameValue n) {
		if (n.eContainer instanceof FunctionOrParenthesisParameter) {
			val param = n.eContainer as FunctionOrParenthesisParameter
			if (param.parameterName === n) {
				return true
			}
		}
		return false
	}
	
	def getRelevantSimplExpressionNameValues(EObject obj) {
		return EcoreUtil2.getAllContentsOfType(obj, SimpleExpressionNameValue).filter [
			!(it instanceof SimpleExpressionStringValue) && !it.isParameterName
		]
	}

	def void check(SimpleExpressionNameValue n, HashMap<String, SimpleExpressionNameValue> expressions) {
		if (!n.parameterName) {
			if (n.isParameter) {
				if (!n.isAsserted) {
					warning(9501, n, n)
					return
				}
			}
			val recursiveExpressions = n.simpleExpressinNamesFromAssignments
			val newExpressions = new HashMap<String, SimpleExpressionNameValue>
			newExpressions.putAll(expressions)
			newExpressions.putAll(recursiveExpressions)
			for (key : recursiveExpressions.keySet) {
				if (expressions.get(key) === null) {
					check(recursiveExpressions.get(key), newExpressions)
				}
			}
		}
	}
	
	def getDeclareSection(Body body) {
		val parent = body.eContainer
		var DeclareSection declareSection;
		if (parent instanceof CreateFunction) {
			declareSection = parent.declareSection
		} else if (parent instanceof CreateProcedure) {
			declareSection = parent.declareSection
		} else if (parent instanceof FuncDeclInType) {
			declareSection = parent.declareSection
		} else if (parent instanceof ProcDeclInType) {
			declareSection = parent.declareSection
		} else if (parent instanceof ConstructorDeclaration) {
			declareSection = parent.declareSection
		} else if (parent instanceof PlsqlBlock) {
			declareSection = parent.declareSection
		} else if (parent instanceof FunctionDefinition) {
			declareSection = parent.declareSection
		} else if (parent instanceof ProcedureDefinition) {
			declareSection = parent.declareSection
		} else {
			// CreatePackageBody, CreateTrigger, CreateTypeBody
			declareSection = null;
		}
		return declareSection;		
	}

	def HashMap<String, SimpleExpressionNameValue> getSimpleExpressinNamesFromAssignments(SimpleExpressionNameValue n) {
		val expressions = new HashMap<String, SimpleExpressionNameValue>
		val body = EcoreUtil2.getContainerOfType(n, Body)
		val assignments = EcoreUtil2.getAllContentsOfType(body, ProcedureCallOrAssignmentStatement).filter[it.assignment !== null].toList
		for (assignment : assignments) {
			val varName = assignment.procedureOrTarget?.object
			if (varName instanceof SimpleExpressionNameValue) {
				if (varName.value.equalsIgnoreCase(n.value)) {
					var a = assignment.assignment
					if (a instanceof SimpleExpressionNameValue) {
						expressions.put(a.value.toLowerCase, a)
					} else {
						for (name : getRelevantSimplExpressionNameValues(assignment?.assignment)) {
							expressions.put(name.value.toLowerCase, name)
						}
					}
				}
			}
		}
		val declareSection = body.declareSection
		if (declareSection !== null) {
			var EObject varOrConst = EcoreUtil2.getAllContentsOfType(declareSection, VariableDeclaration).findFirst [
				it.variable.value.equalsIgnoreCase(n.value) && it.getDefault() !== null
			]
			if (varOrConst !== null) {
				for (name : getRelevantSimplExpressionNameValues((varOrConst as VariableDeclaration).getDefault())) {
					expressions.put(name.value.toLowerCase, name)
				}
			} else {
				varOrConst = EcoreUtil2.getAllContentsOfType(declareSection, ConstantDeclaration).findFirst [
					it.constant.value.equalsIgnoreCase(n.value) && it.getDefault() !== null
				]
				if (varOrConst !== null) {
					for (name : getRelevantSimplExpressionNameValues((varOrConst as ConstantDeclaration).getDefault())) {
						expressions.put(name.value.toLowerCase, name)
					}
				}
			}
		}
		return expressions;
	}
	
	def checkAll(EObject obj) {
		val expressions = new HashMap<String, SimpleExpressionNameValue>
		if (obj !== null) {
			if (obj instanceof SimpleExpressionNameValue) {
				expressions.putAll(obj.simpleExpressinNamesFromAssignments)
				if (expressions.size == 0) {
					expressions.put(obj.value.toLowerCase, obj);
				}
			} else {
				for (name : getRelevantSimplExpressionNameValues(obj)) {
					expressions.put(name.value.toLowerCase, name)
				}
			}
		}
		for (name : expressions.values) {
			name.check(expressions)
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
