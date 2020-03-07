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
	
	@Test
	def void issue1_ut_utils_wrapper() {
		val stmt = '''
			create or replace package body ut_annotation_manager as
			  -- ...
			  function get_missing_objects(a_object_owner varchar2, a_object_type varchar2) return ut_annotation_objs_cache_info is
			    l_rows         sys_refcursor;
			    l_ut_owner     varchar2(250) := ut_utils.ut_owner;
			    l_objects_view varchar2(200) := ut_metadata.get_objects_view_name();
			    l_cursor_text  varchar2(32767);
			    l_data         ut_annotation_objs_cache_info;
			    l_result       ut_annotation_objs_cache_info;
			    l_card         natural;
			  begin
			    l_data := ut_annotation_cache_manager.get_annotations_objects_info(a_object_owner, a_object_type);
			    l_card := ut_utils.scale_cardinality(cardinality(l_data));
			
			    l_cursor_text :=
			      'select /*+ cardinality(i '||l_card||') */
			                value(i)
			           from table( cast( :l_data as '||l_ut_owner||'.ut_annotation_objs_cache_info ) ) i
			           where
			             not exists (
			                select 1  from '||l_objects_view||q'[ o
			                 where o.owner = i.object_owner
			                   and o.object_name = i.object_name
			                   and o.object_type = i.object_type
			                   and o.owner       = ']'||ut_utils.qualified_sql_name(a_object_owner)||q'['
			                   and o.object_type = ']'||ut_utils.qualified_sql_name(a_object_type)||q'['
			                )]';
			    open l_rows for l_cursor_text  using l_data;
			    fetch l_rows bulk collect into l_result limit ut_utils.gc_max_objects_fetch_limit;
			    close l_rows;
			    return l_result;
			  end;
			  -- ...
			end ut_annotation_manager;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}
	
	@Test
	def void issue14_duplicate_warnings() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY pkg IS
			   PROCEDURE p (
			      p_config_name IN VARCHAR2,
			      p_where       IN VARCHAR2
			   ) IS
			      v_sql           VARCHAR2(32767);
			      v_search_config search_config_t%ROWTYPE;
			      v_source        VARCHAR2(40) := '';
			   BEGIN
			      v_search_config := get_config_info(p_config_name => p_config_name);
			      IF p_where IS NULL THEN
			         RETURN 0;
			      END IF;
			      v_sql := 'DELETE FROM ' || v_search_config.load_target
			            || ' <WHERE>';
			      v_sql := REPLACE(v_sql, '<WHERE>', p_where);
			      EXECUTE IMMEDIATE v_sql;
			   END refresh_work_full;
			END pkg;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size)
		val issue_p_config_name = issues.findFirst[it.data.contains("p_config_name")]
		Assert.assertEquals(10, issue_p_config_name.lineNumber)
		val issue_p_where = issues.findFirst[it.data.contains("p_where")]
		Assert.assertEquals(16, issue_p_where.lineNumber)
	}

	@Test
	def void issue14_duplicate_warnings_are_ok_for_multiple_plsql_statements() {
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
			   l_sql := 'BEGIN dbms_output.put_line(' || l_table_name || '); END;';
			   EXECUTE IMMEDIATE l_sql;
			END p;
		'''
		// every use of OpenForStatement and ExecuteImmediateStatement leads to a warning
		val issues = stmt.issues.filter[it.data.contains("in_table_name")]
		Assert.assertEquals(2, issues.size)
		Assert.assertEquals(7, issues.get(0).lineNumber)
		Assert.assertEquals(7, issues.get(1).lineNumber)
	}

}