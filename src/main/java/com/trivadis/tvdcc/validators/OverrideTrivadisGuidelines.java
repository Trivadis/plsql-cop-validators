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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel6;
import com.trivadis.oracle.plsql.plsql.BinaryCompoundExpressionLevel7;
import com.trivadis.oracle.plsql.plsql.CollectionTypeDefinition;
import com.trivadis.oracle.plsql.plsql.ConstantDeclaration;
import com.trivadis.oracle.plsql.plsql.PLSQLFile;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNameValue;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionNumberValue;
import com.trivadis.oracle.plsql.plsql.SimpleExpressionStringValue;
import com.trivadis.oracle.plsql.plsql.UserDefinedType;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;

public class OverrideTrivadisGuidelines extends TrivadisGuidelines3 implements PLSQLCopValidator {

	// must be overridden to avoid duplicate issues when used via ComposedChecks 
	@Override
	public void register(EValidatorRegistrar registrar) {
		List<EPackage> ePackages = getEPackages();
		if (registrar.getRegistry().get(ePackages.get(0)) == null) {
			// standalone validator, default registration required
			super.register(registrar);
		}
	}

	public boolean isConstantDeclaration(

	final EObject obj) {
		ConstantDeclaration constantDeclaration = EcoreUtil2.getContainerOfType(obj, ConstantDeclaration.class);
		return (constantDeclaration != null);
	}

	public boolean isLoggerCall(EObject obj) {
		if (obj == null) {
			return false;
		} else {
			BinaryCompoundExpressionLevel7 func = EcoreUtil2.getContainerOfType(obj, BinaryCompoundExpressionLevel7.class);
			if (func == null) {
				return false;
			} else {
				if (func.getLeft() instanceof SimpleExpressionNameValue) {
					if (func.eContainer() instanceof BinaryCompoundExpressionLevel6) {
						BinaryCompoundExpressionLevel6 pkg = (BinaryCompoundExpressionLevel6) func.eContainer();
						if (pkg.getBinaryOperator().equals(".")) {
							if (pkg.getLeft() instanceof SimpleExpressionNameValue) {
								SimpleExpressionNameValue pkgName = (SimpleExpressionNameValue) pkg.getLeft();
								if (pkgName.getValue().equalsIgnoreCase("logger")) {
									return true;
								}
							}
						}
					}
				}
				return isLoggerCall(func.eContainer());
			}
		}
	}

	/*
	 * Override G-1050: Avoid using literals in your code. (guidelines version 3.x)
	 * Override G-05: Avoid using literals in your code. (guidelines version 2.x)
	 * re-implement existing functionality while ignoring logger calls 
	 * "&& !isLoggerCall(obj)" is the only addition to the original code
	 */
	@Check
	@Override
	public void checkGuideline5(PLSQLFile file) {
		HashMap<String, Integer> map = new HashMap<>();
		int threshold = Integer.parseInt(System.getProperty(COP_1050_THRESHOLD, "2"));
		List<EObject> warnings = new ArrayList<>();
		List<SimpleExpressionNumberValue> numbers = EcoreUtil2.getAllContentsOfType(file, SimpleExpressionNumberValue.class);
		for (SimpleExpressionNumberValue literal : numbers) {
			ConstantDeclaration declartion = EcoreUtil2.getContainerOfType(literal, ConstantDeclaration.class);
			UserDefinedType udf = EcoreUtil2.getContainerOfType(literal, UserDefinedType.class);
			CollectionTypeDefinition collDef = EcoreUtil2.getContainerOfType(literal, CollectionTypeDefinition.class);
			if (declartion == null && udf == null && collDef == null && !literal.getValue().replace(".", "").equals("0")
					&& !literal.getValue().replace(".", "").equals("1")) {
				updateLiteralMap(literal, map, threshold);
				warnings.add(literal);
			}			
		}
		List<SimpleExpressionStringValue> strings = EcoreUtil2.getAllContentsOfType(file, SimpleExpressionStringValue.class);
		for (SimpleExpressionStringValue literal : strings) {
			ConstantDeclaration declartion = EcoreUtil2.getContainerOfType(literal, ConstantDeclaration.class);
			if (declartion == null) {
				UserDefinedType udf = EcoreUtil2.getContainerOfType(literal, UserDefinedType.class);
				if (udf == null) {
					updateLiteralMap(literal, map, threshold);
					warnings.add(literal);
				}
			}			
		}		
		for (EObject obj : warnings) {
			if (!withinThreshold(obj, map, threshold) && !isLoggerCall(obj)) {
				warning(getGuidelineMsg(5), obj, null, getGuidelineId(5), serialize(NodeModelUtils.getNode(obj)));
			}
		}
	}	
}
