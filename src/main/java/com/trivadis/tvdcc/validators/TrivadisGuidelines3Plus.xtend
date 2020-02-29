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

import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator
import com.trivadis.oracle.plsql.validation.PLSQLJavaValidator
import java.util.HashMap
import org.eclipse.xtext.validation.ComposedChecks

@ComposedChecks(validators = #[OverrideTrivadisGuidelines, SQLInjection, TrivadisPlsqlNaming])
class TrivadisGuidelines3Plus extends PLSQLJavaValidator implements PLSQLCopValidator {

	val TrivadisGuidelines3 converter = new TrivadisGuidelines3
	var HashMap<Integer, PLSQLCopGuideline> guidelines

	// TODO: remove when TrivadisGuidelines2 uses new IDs
	override String getGuidelineId(Integer id) {
		if (id < 1000) {
			return String.format("G-%04d", converter.getNewId(id))
		} else {
			return super.getGuidelineId(id)
		}
	}

	// TODO: remove when TrivadisGuidelines2 uses new IDs
	override String getGuidelineMsg(Integer id) {
		if (id < 1000) {
			val newId = converter.getNewId(id)
			val guideline = getGuidelines.get(newId)
			return String.format("G-%04d: %s", newId, guideline?.getMsg());
		} else {
			return super.getGuidelineMsg(id)
		}
	}
	
	override getGuidelines() {
		if (guidelines === null) {
			guidelines = new HashMap<Integer, PLSQLCopGuideline>()
			val annotations = this.class.annotations.filter[it.annotationType == ComposedChecks].toList.map [
				it as ComposedChecks
			];
			for (annotation : annotations) {
				for (validator : annotation.validators) {
					val validatorInstance = validator.newInstance;
					for (guideline : (validatorInstance as PLSQLCopValidator).guidelines.values) {
						if (guideline.id < 1000) {
							// TODO: remove when TrivadisGuidelines2 uses new IDs
							guidelines.put(converter.getNewId(guideline.id), guideline)
						} else {
							guidelines.put(guideline.id, guideline)
						}
					}
				}
			}
		}
		return guidelines
	}

}
