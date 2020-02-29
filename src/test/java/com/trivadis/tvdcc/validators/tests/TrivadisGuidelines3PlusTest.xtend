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

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences
import com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class TrivadisGuidelines3PlusTest extends AbstractValidatorTest {

	@BeforeClass
	static def void setupTest() {
		TrivadisPlsqlNamingTest.stashPropertiesFile
		setupValidator
	}

	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = TrivadisGuidelines3Plus
	}
	
	@Test
	def void guidelines() {
		val guidelines = (getValidator() as TrivadisGuidelines3Plus).guidelines
		Assert.assertEquals(16, guidelines.values.filter[it.id >= 9000].size)
		Assert.assertEquals(92, guidelines.values.filter[it.id < 9000].size)
	}
	
	// issue avoided by OverrideTrivadisGuidelines (would throw an error via TrivadisGuidelines3)
	@Test
	def void literalInLoggerCallIsOkay() {
		val stmt = '''
			BEGIN
			   logger.log('Hello World');
			END;
		'''
		val issues = stmt.issues.filter[it.code == "G-1050"]
		Assert.assertEquals(0, issues.size)
	}

	// issue thrown by TrivadisGuidelines3
	@Test
	def void literalInDbmsOutputCallIsNotOkay() {
		val stmt = '''
			BEGIN
			   dbms_output.put_line('Hello World');
			END;
		'''
		val issues = stmt.issues.filter[it.code == "G-1050"]
		Assert.assertEquals(1, issues.size)
	}

	// issue thrown by SQLInjection
	@Test 
	def void executeImmediateNotAssertedVariable() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			   l_table_name VARCHAR2(128 BYTE);
			   l_sql        VARCHAT2(4000 BYTE);
			BEGIN
			   l_table_name := in_table_name;
			   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			   EXECUTE IMMEDIATE l_sql;
			END p;
		'''
		val issues = stmt.issues.filter[it.code == "G-9501"]
		Assert.assertEquals(1, issues.size)
	}

	// issue thrown by TrivadisPlsqlNaming
	@Test
	def void globalVariableNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE example AS
			   some_name INTEGER;
			END example;
			/
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9001"].size)
	}

	@AfterClass
	static def void restorePropertiesFile() {
		TrivadisPlsqlNamingTest.restorePropertiesFile
	}

}