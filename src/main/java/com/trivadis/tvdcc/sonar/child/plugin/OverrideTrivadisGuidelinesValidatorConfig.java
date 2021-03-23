/*
 * Copyright 2021 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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

package com.trivadis.tvdcc.sonar.child.plugin;

import com.trivadis.sonar8.plugins.plsqlcop.ValidatorConfig;
import com.trivadis.tvdcc.validators.OverrideTrivadisGuidelines;

public class OverrideTrivadisGuidelinesValidatorConfig implements ValidatorConfig<OverrideTrivadisGuidelines> {

	@Override
	public String getModelResourcePath() {
		return null;
	}

	@Override
	public String getRulesResourcePath() {
		return null;
	}

	@Override
	public Class<OverrideTrivadisGuidelines> getValidatorClass() {
		return OverrideTrivadisGuidelines.class;
	}

}
