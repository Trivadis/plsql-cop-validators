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
import com.trivadis.oracle.plsql.validation.PLSQLJavaValidator
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.eclipse.emf.common.util.URI
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.eclipse.xtext.validation.CheckMode
import static org.hamcrest.core.AnyOf.*
import static org.hamcrest.core.StringStartsWith.*
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

abstract class AbstractValidatorTest {

	static val PROPERTIES_FILE_NAME = TrivadisPlsqlNaming.PROPERTIES_FILE_NAME
	public static val FULL_PROPERTIES_FILE_NAME = System.getProperty("user.home") + File.separator + PROPERTIES_FILE_NAME
	static val String FULL_PROPERTIES_FILE_NAME_BACKUP = FULL_PROPERTIES_FILE_NAME + ".backup"

	Injector injector = new PLSQLStandaloneSetup().createInjectorAndDoEMFRegistration();

	@BeforeClass
	static def void commonSetup() {
		stashPropertiesFile
	}

	static def void stashPropertiesFile() {
		if (Files.exists(Paths.get(FULL_PROPERTIES_FILE_NAME))) {
			Files.copy(Paths.get(FULL_PROPERTIES_FILE_NAME), Paths.get(FULL_PROPERTIES_FILE_NAME_BACKUP));
			Files.delete(Paths.get(FULL_PROPERTIES_FILE_NAME))
		}
	}

	@AfterClass
	static def void restorePropertiesFile() {
		if (Files.exists(Paths.get(FULL_PROPERTIES_FILE_NAME_BACKUP))) {
			Files.copy(Paths.get(FULL_PROPERTIES_FILE_NAME_BACKUP), Paths.get(FULL_PROPERTIES_FILE_NAME),
				StandardCopyOption.REPLACE_EXISTING)
			Files.delete(Paths.get(FULL_PROPERTIES_FILE_NAME_BACKUP))
		} else {
			if (Files.exists(Paths.get(FULL_PROPERTIES_FILE_NAME))) {
				Files.delete(Paths.get(FULL_PROPERTIES_FILE_NAME))
			}
		}
	}
	
	@Test
	def void guidelineTitleStartsWithKeyword() {
		val guidelines = getValidator().guidelines.values.filter[it.id >= 9000]
		for (g : guidelines) {
			Assert.assertThat(
				'"' + g.msg + "' does not start with keyword",
				g.msg,
				anyOf(startsWith("Always"), startsWith("Never"), startsWith("Avoid"), startsWith("Try"))
			)
		}
	}

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
