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
package com.trivadis.tvdcc.validators.tests

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences
import com.trivadis.tvdcc.validators.SQLInjection
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class SQLInjectionTest extends AbstractValidatorTest {

	@BeforeClass
	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = SQLInjection
	}
	
	@Test
	def void registeredChecks() {
		val guidelines = (getValidator() as SQLInjection).guidelines
		Assert.assertEquals(1, guidelines.values.size)
		Assert.assertEquals(9501, guidelines.values.get(0).id)
	}
	
	@Test 
	def void openCursorNotAssertedVariable() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'SELECT * FROM #in_table_name#';
			   l_table_name VARCHAR2(128 BYTE);
			   l_sql        VARCHAT2(4000 BYTE);
			   l_cur        SYS_REFCURSOR;
			BEGIN
			   l_table_name := in_table_name;
			   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			   OPEN l_cur FOR l_sql;
			   CLOSE l_cur;
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(20, issue.column)
		Assert.assertEquals(13, issue.length)
	}	
	
	@Test 
	def void executeImmediateAssertedVariable() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			   l_table_name VARCHAR2(128 BYTE);
			   l_sql        VARCHAT2(4000 BYTE);
			BEGIN
			   l_table_name := sys.dbms_assert.enquote_name(in_table_name);
			   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			   EXECUTE IMMEDIATE l_sql;
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}

	@Test 
	def void executeImmediateNotAssertedNumberParameter() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN INTEGER) AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			   l_table_name INTEGER;
			   l_sql        VARCHAT2(4000 BYTE);
			BEGIN
			   l_table_name := in_table_name;
			   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			   EXECUTE IMMEDIATE l_sql;
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}

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
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(6, issue.lineNumber)
		Assert.assertEquals(20, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInFunction() {
		val stmt = '''
			CREATE OR REPLACE FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
			   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			   l_table_name VARCHAR2(128 BYTE);
			   l_sql        VARCHAT2(4000 BYTE);
			BEGIN
			   l_table_name := in_table_name;
			   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			   EXECUTE IMMEDIATE l_sql;
			   RETURN true;
			END f;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(6, issue.lineNumber)
		Assert.assertEquals(20, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInPackageProcedure() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY pkg IS
			   PROCEDURE p (in_table_name IN VARCHAR2) AS
			      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			      l_table_name VARCHAR2(128 BYTE);
			      l_sql        VARCHAT2(4000 BYTE);
			   BEGIN
			      l_table_name := in_table_name;
			      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			      EXECUTE IMMEDIATE l_sql;
			   END p;
			END pkg;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(23, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInPackageFunction() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY pkg IS
			   FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
			      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			      l_table_name VARCHAR2(128 BYTE);
			      l_sql        VARCHAT2(4000 BYTE);
			   BEGIN
			      l_table_name := in_table_name;
			      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			      EXECUTE IMMEDIATE l_sql;
			      RETURN true;
			   END f;
			END pkg;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(23, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInTypeProcedure() {
		val stmt = '''
			CREATE OR REPLACE TYPE BODY typ IS
			   MEMBER PROCEDURE p (in_table_name IN VARCHAR2) AS
			      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			      l_table_name VARCHAR2(128 BYTE);
			      l_sql        VARCHAT2(4000 BYTE);
			   BEGIN
			      l_table_name := in_table_name;
			      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			      EXECUTE IMMEDIATE l_sql;
			   END p;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(23, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInTypeFunction() {
		val stmt = '''
			CREATE OR REPLACE TYPE BODY typ IS
			   MEMBER FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
			      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			      l_table_name VARCHAR2(128 BYTE);
			      l_sql        VARCHAT2(4000 BYTE);
			   BEGIN
			      l_table_name := in_table_name;
			      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			      EXECUTE IMMEDIATE l_sql;
			      RETURN true;
			   END f;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(23, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateNotAssertedVariableInTypeConstructor() {
		val stmt = '''
			CREATE OR REPLACE TYPE BODY typ IS
			   CONSTRUCTOR FUNCTION f (in_table_name IN VARCHAR2) RETURN SELF AS RESULT AS
			      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
			      l_table_name VARCHAR2(128 BYTE);
			      l_sql        VARCHAT2(4000 BYTE);
			   BEGIN
			      l_table_name := in_table_name;
			      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
			      EXECUTE IMMEDIATE l_sql;
			      RETURN;
			   END f;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(7, issue.lineNumber)
		Assert.assertEquals(23, issue.column)
		Assert.assertEquals(13, issue.length)
	}



	@Test 
	def void executeImmediateAssertedExpression() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			BEGIN
			   EXECUTE IMMEDIATE 'DROP TABLE '
			      || sys.dbms_assert.enquote_name(in_table_name)
			      || ' PURGE';
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}

	@Test 
	def void executeImmediateNotAssertedExpression() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			BEGIN
			   EXECUTE IMMEDIATE 'DROP TABLE '
			      || in_table_name
			      || ' PURGE';
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size)
		val issue = issues.get(0)
		Assert.assertEquals("G-9501", issue.code)
		Assert.assertEquals("in_table_name", issue.data.get(0))
		Assert.assertEquals(4, issue.lineNumber)
		Assert.assertEquals(10, issue.column)
		Assert.assertEquals(13, issue.length)
	}

	@Test 
	def void executeImmediateAssertedViaPublicSynonym() {
		val stmt = '''
			CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
			BEGIN
			   EXECUTE IMMEDIATE 'DROP TABLE '
			      || dbms_assert.enquote_name(in_table_name)
			      || ' PURGE';
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}

}