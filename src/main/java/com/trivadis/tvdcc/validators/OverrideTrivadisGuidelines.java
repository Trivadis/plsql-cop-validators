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
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue
import com.trivadis.oracle.plsql.plsql.SimpleExpressionStringValue
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.EValidatorRegistrar

class OverrideTrivadisGuidelines extends TrivadisGuidelines3 implements PLSQLCopValidator {

	// must be overridden to avoid duplicate issues when used via ComposedChecks 
	override register(EValidatorRegistrar registrar) {
		val ePackages = getEPackages()
		if (registrar.registry.get(ePackages.get(0)) === null) {
			// standalone validator, default registration required
			super.register(registrar);
		}
	}

	def boolean isConstantDeclaration(EObject obj) {
		val constantDeclaration = EcoreUtil2.getContainerOfType(obj, ConstantDeclaration)
		return constantDeclaration !== null
	}
	
	def boolean isLoggerCall(EObject obj) {
		if (obj === null) {
			return false
		} else {
			val func = EcoreUtil2.getContainerOfType(obj, BinaryCompoundExpressionLevel7)
			if (func === null) {
				return false;
			} else {
				if (func.left instanceof SimpleExpressionNameValue) {
					if (func.eContainer instanceof BinaryCompoundExpressionLevel6) {
						val pkg = func.eContainer as BinaryCompoundExpressionLevel6
						if (pkg.binaryOperator == ".") {
							if (pkg.left instanceof SimpleExpressionNameValue) {
								val pkgName = pkg.left as SimpleExpressionNameValue
								if (pkgName.value.equalsIgnoreCase("logger")) {
									return true;
								}
							}
						}
					}
				}
				return isLoggerCall(func.eContainer)
			}
		}
	}

	/**
	 * Override G-1050: Avoid using literals in your code. (guidelines version 3.x)
	 * Override G-05: Avoid using literals in your code. (guidelines version 2.x)
	 * re-implement existing functionality for string literals while ignoring logger calls
	 */
	@Check
	override checkGuideline5(SimpleExpressionStringValue literal) {
		if (!literal.isConstantDeclaration && !literal.isLoggerCall) {
			warning(1050, literal, literal)
		}
	}

}
