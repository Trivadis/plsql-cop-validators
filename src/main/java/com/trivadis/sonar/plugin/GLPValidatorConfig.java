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
import com.trivadis.tvdcc.validators.GLP;

public class GLPValidatorConfig implements ValidatorConfig<GLP> {

	@Override
	public String getModelResourcePath() {
		return "/GLP/genmodel/plsqlcop-model.xml";
	}

	@Override
	public String getRulesResourcePath() {
		return "/GLP/genmodel/rules.xml";
	}

	@Override
	public Class<GLP> getValidatorClass() {
		return GLP.class;
	}

}
