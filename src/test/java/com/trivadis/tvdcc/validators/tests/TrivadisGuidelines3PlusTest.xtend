/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class TrivadisGuidelines3PlusTest extends AbstractValidatorTest {

	@BeforeClass
	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = TrivadisGuidelines3Plus
	}
	
	@Test
	def void guidelines() {
		val guidelines = (getValidator() as TrivadisGuidelines3Plus).guidelines
		Assert.assertEquals(16, guidelines.values.filter[it.id >= 9000].size)
		Assert.assertEquals(92, guidelines.values.filter[it.id < 9000].size)
		Assert.assertEquals(79, guidelines.values.filter[it.id < 1000].size)
	}
	
	@Test
	def void getGuidelineId_mapped_via_Trivadis2() {
		val validator = new TrivadisGuidelines3Plus
		Assert.assertEquals("G-1010", validator.getGuidelineId(1))
	}
	
	@Test
	def void getGuidelineId_of_Trivadis3() {
		val validator = new TrivadisGuidelines3Plus
		Assert.assertEquals("G-2130", validator.getGuidelineId(2130))
	}

	@Test
	def void getGuidelineMsg_mapped_via_Trivadis2() {
		val validator = new TrivadisGuidelines3Plus
		Assert.assertEquals("G-1010: Try to label your sub blocks.", validator.getGuidelineMsg(1))
	}

	@Test
	def void getGuidelineMsg_mapped_via_Trivadis3() {
		val validator = new TrivadisGuidelines3Plus
		Assert.assertEquals("G-2130: Try to use subtypes for constructs used often in your code.", validator.getGuidelineMsg(2130))
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

	// issue thrown by OverrideTrivadisGuidelines (check in parent)
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
	
	// issue thrown by TrivadisGuidelines3
	@Test
	def void guideline2230_na() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY constants_up IS
			   co_big_increase CONSTANT NUMBER(5,0) := 1;
			   
			   FUNCTION big_increase RETURN NUMBER DETERMINISTIC IS
			   BEGIN
			      RETURN co_big_increase;
			   END big_increase;
			END constants_up;
			/
		'''
		val issues = stmt.issues.filter[it.code == "G-2230"]
		Assert.assertEquals(1, issues.size)
	}

	// issue thrown by TrivadisGuidelines2
	@Test
	def void guideline1010_10() {
		val stmt = '''
			BEGIN
			   BEGIN 
			      NULL;
			   END;
			END;
			/
		'''
		val issues = stmt.issues.filter[it.code == "G-1010"]
		Assert.assertEquals(1, issues.size)
	}

	// issue thrown by SQLInjection
	@Test 
	def void executeImmediateNotAssertedVariable() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			   l_table_name VARCHAR2(128 BYTE);
			   l_sql        VARCHAR2(4000 BYTE);
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

}