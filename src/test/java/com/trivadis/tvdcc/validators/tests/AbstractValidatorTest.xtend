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
package com.trivadis.tvdcc.validators.tests

import com.google.inject.Injector
import com.trivadis.oracle.plsql.PLSQLStandaloneSetup
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.validation.CheckMode
import com.trivadis.oracle.plsql.validation.PLSQLJavaValidator

abstract class AbstractValidatorTest {
	Injector injector = new PLSQLStandaloneSetup().createInjectorAndDoEMFRegistration();

	def parse(String stmt) {
		val resourceSet = injector.getInstance(XtextResourceSet);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		resourceSet.addLoadOption(XtextResource.OPTION_ENCODING, Charset.defaultCharset().name());
		val resource = resourceSet.createResource(URI.createURI("dummy:/test.plsql"));
		val input = new ByteArrayInputStream(stmt.bytes);
		resource.load(input, resourceSet.loadOptions)
		return resource as XtextResource;
	}

	def getIssues(String stmt) {
		val resource = stmt.parse
		val errors = resource.errors
		if (errors.size > 0) {
			val firstError = errors.get(0)
			throw new RuntimeException('''Syntax error: «firstError.message» at line «firstError.line».''')
		}
		return resource.resourceServiceProvider.resourceValidator.validate(resource, CheckMode.ALL, null)
	}
	
	def getValidator() {
		return injector.getInstance(PLSQLJavaValidator)
	}
}
