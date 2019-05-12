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
import com.trivadis.tvdcc.validators.GLP
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class GLPTest extends AbstractValidatorTest {

	@BeforeClass
	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = GLP
	}
	
	@Test
	def void guidelines() {
		val guidelines = (getValidator() as GLP).guidelines
		Assert.assertEquals(3, guidelines.values.filter[it.id >= 9000].size)
	}
	
	@Test 
	def void procedureOk() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (p_1 IN INTEGER, p_2 IN VARCHAR2) AS
			   l_something INTEGER;
			BEGIN
			   NULL;
			END p;
		'''
		val issues = stmt.issues.filter[it.code.startsWith("G-9")]
		Assert.assertEquals(0, issues.size)
	}

	@Test 
	def void procedureNok() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (a IN INTEGER, b IN VARCHAR2) AS
			   c INTEGER;
			BEGIN
			   NULL;
			END p;
		'''
		val issues = stmt.issues.filter[it.code.startsWith("G-9")]
		Assert.assertEquals(3, issues.size)
		// a
		val issue1 = issues.get(0)
		Assert.assertEquals(1, issue1.lineNumber)
		Assert.assertEquals(32, issue1.column)
		Assert.assertEquals(1, issue1.length)
		Assert.assertEquals("G-9003", issue1.code)
		Assert.assertEquals("G-9003: Parameters should start with 'p_'.", issue1.message)
		Assert.assertEquals("a IN INTEGER", issue1.data.get(0))
		// b
		val issue2 = issues.get(1)
		Assert.assertEquals(1, issue2.lineNumber)
		Assert.assertEquals(46, issue2.column)
		Assert.assertEquals(1, issue2.length)
		Assert.assertEquals("G-9003", issue2.code)
		Assert.assertEquals("G-9003: Parameters should start with 'p_'.", issue2.message)
		Assert.assertEquals("b IN VARCHAR2", issue2.data.get(0))
		// c
		val issue3 = issues.get(2)
		Assert.assertEquals(2, issue3.lineNumber)
		Assert.assertEquals(4, issue3.column)
		Assert.assertEquals(1, issue3.length)
		Assert.assertEquals("G-9002", issue3.code)
		Assert.assertEquals("G-9002: Local variables should start with 'l_'.", issue3.message)
		Assert.assertEquals("c INTEGER;", issue3.data.get(0))
	}

	@Test 
	def void packageBodyOk() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY pkg AS
			   g_global_variable INTEGER;
			
			   PROCEDURE p (
			   	   p_parameter1 IN INTEGER,
			   	   p_parameter2 IN VARCHAR2
			   ) AS
			      l_local_variable INTEGER;
			   BEGIN
			      NULL;
			   END p;
			 
			   FUNCTION f (
			   	   p_parameter1 IN INTEGER,
			   	   p_parameter2 IN INTEGER
			   ) RETURN INTEGER AS
			      l_local_variable INTEGER;
			   BEGIN
			      RETURN p_parameter1 * p_parameter2;
			   END f;
			END pkg;
		'''
		val issues = stmt.issues.filter[it.code.startsWith("G-9")]
		Assert.assertEquals(0, issues.size)
	}

	@Test 
	def void packageBodyNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY pkg AS
			   global_variable INTEGER;
			
			   PROCEDURE p (
			   	   parameter1 IN INTEGER,
			   	   parameter2 IN VARCHAR2
			   ) AS
			      local_variable INTEGER;
			   BEGIN
			      NULL;
			   END p;
			 
			   FUNCTION f (
			   	   parameter1 IN INTEGER,
			   	   parameter2 IN INTEGER
			   ) RETURN INTEGER AS
			      local_variable INTEGER;
			   BEGIN
			      RETURN parameter1 * p_parameter2;
			   END f;
			END pkg;
		'''
		val issues = stmt.issues.filter[it.code.startsWith("G-9")]
		Assert.assertEquals(7, issues.size)
		// global_variable
		val issue1 = issues.get(0)
		Assert.assertEquals(2, issue1.lineNumber)
		Assert.assertEquals(4, issue1.column)
		Assert.assertEquals(15, issue1.length)
		Assert.assertEquals("G-9001", issue1.code)
		Assert.assertEquals("G-9001: Global variables should start with 'g_'.", issue1.message)
		Assert.assertEquals("global_variable INTEGER;", issue1.data.get(0))
	}

}