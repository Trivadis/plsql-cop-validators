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
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class TrivadisPlsqlNamingTest extends AbstractValidatorTest {

	@BeforeClass
	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = TrivadisPlsqlNaming
	}

	@Test
	def void globalVariableNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE example AS
			   some_name INTEGER;
			END example;
			/
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9101"].size)
	}

	@Test
	def void globalVariableOk() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE example AS
			   g_some_name INTEGER;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9101"].size)
	}

	@Test
	def void localVariableNok() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY example AS
			   PROCEDURE a IS
			      some_name INTEGER;
			   BEGIN
			      NULL;
			   END a;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9102"].size)
	}

	@Test
	def void localVariableOk() {
		val stmt = '''
			CREATE OR REPLACE PACKAGE BODY example AS
			   PROCEDURE a IS
			      l_some_name INTEGER;
			   BEGIN
			      NULL;
			   END a;
			END example;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9102"].size)
	}

	@Test
	def void cursorNameNok() {
		val stmt = '''
			DECLARE
			   CURSOR some_name IS SELECT * FROM emp;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9103"].size)
	}
	
	@Test
	def void cursorNameOk() {
		val stmt = '''
			DECLARE
			   CURSOR c_some_name IS SELECT * FROM emp;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9103"].size)
	}

	@Test
	def void sysrefcursorNameNOk_bug5() {
		val stmt = '''
			DECLARE
			   l_dept SYS_REFCURSOR;
			BEGIN
			   NULL;
			END;
			/
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9103"].size)
	}

	@Test
	def void sysrefcursorNameOk_bug5() {
		val stmt = '''
			DECLARE
			   c_dept SYS_REFCURSOR;
			BEGIN
			   NULL;
			END;
			/
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9103"].size)
	}

	@Test
	def void recordNameNok() {
		val stmt = '''
			DECLARE
			   emp emp%ROWTYPE;
			   TYPE r_dept_type IS RECORD (
			      deptno NUMBER,
			      dname  VARCHAR2(14 CHAR),
			      loc    LOC(13 CHAR)
			   );
			   dept r_dept_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.filter[it.code == "G-9104"].size)
	}

	@Test
	def void recordNameOk() {
		val stmt = '''
			DECLARE
			   r_emp emp%ROWTYPE;
			   TYPE r_dept_type IS RECORD (
			      deptno NUMBER,
			      dname  VARCHAR2(14 CHAR),
			      loc    LOC(13 CHAR)
			   );
			   r_dept r_dept_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9104"].size)
	}

	@Test
	def void arrayNameNok() {
		val stmt = '''
			DECLARE
			   TYPE t_varray_type IS VARRAY(10) OF STRING;
			   array1 t_varray_type;
			   TYPE t_nested_table_type IS TABLE OF STRING;
			   array2 t_nested_table_type;
			   TYPE t_assoc_array_type IS TABLE OF STRING INDEX BY PLS_INTEGER;
			   array3 t_assoc_array_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(3, issues.filter[it.code == "G-9105"].size)
	}

	@Test
	def void arrayNameOk() {
		val stmt = '''
			DECLARE
			   TYPE t_varray_type IS VARRAY(10) OF STRING;
			   t_array1 t_varray_type;
			   TYPE t_nested_table_type IS TABLE OF STRING;
			   t_array2 t_nested_table_type;
			   TYPE t_assoc_array_type IS TABLE OF STRING INDEX BY PLS_INTEGER;
			   t_array3 t_assoc_array_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9105"].size)
	}

	@Test
	def void objectNameNok() {
		val stmt = '''
			CREATE OR REPLACE TYPE dept_type AS OBJECT (
				deptno INTEGER,
				dname  VARCHAR2(14 CHAR),
				loc    VARCHAR2(13 CHAR)
			);
			
			DECLARE
			   dept dept_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9106"].size)
	}

	@Test
	def void objectNameOk() {
		val stmt = '''
			CREATE OR REPLACE TYPE dept_type AS OBJECT (
				deptno INTEGER,
				dname  VARCHAR2(14 CHAR),
				loc    VARCHAR2(13 CHAR)
			);
			
			DECLARE
			   o_dept dept_type;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9106"].size)
	}

	@Test
	def void cursorParameterNameNok() {
		val stmt = '''
			DECLARE
			   CURSOR c_emp (x_ename IN VARCHAR2) IS 
			      SELECT * 
			        FROM emp
			       WHERE ename LIKE x_ename;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9107"].size)
	}

	@Test
	def void cursorParameterNameOk() {
		val stmt = '''
			DECLARE
			   CURSOR c_emp (p_ename IN VARCHAR2) IS 
			      SELECT * 
			        FROM emp
			       WHERE ename LIKE p_ename;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9107"].size)
	}

	@Test
	def void inParameterNameNok() {
		val stmt = '''
			CREATE PROCEDURE p1 (param INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (param IN INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.filter[it.code == "G-9108"].size)
	}

	@Test
	def void inParameterNameOk() {
		val stmt = '''
			CREATE PROCEDURE p1 (in_param INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (in_param IN INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9108"].size)
	}

	@Test
	def void SelfInParameterNameOk() {
		val stmt = '''
			CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (
			   rect_length  NUMBER,
			   rect_width   NUMBER, 
			   member FUNCTION get_surface (
			      self IN rectangle
			   ) RETURN NUMBER
			);
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9108"].size)
	}



	@Test
	def void outParameterNameNok() {
		val stmt = '''
			CREATE PROCEDURE p1 (param OUT INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (param OUT INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.filter[it.code == "G-9109"].size)
	}

	@Test
	def void outParameterNameOk() {
		val stmt = '''
			CREATE PROCEDURE p1 (out_param OUT INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (out_param OUT INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9109"].size)
	}

	@Test
	def void inOutParameterNameNok() {
		val stmt = '''
			CREATE PROCEDURE p1 (param IN OUT INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (param IN OUT INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.filter[it.code == "G-9110"].size)
	}

	@Test
	def void inOutParameterNameOk() {
		val stmt = '''
			CREATE PROCEDURE p1 (io_param IN OUT INTEGER) IS
			BEGIN
			   NULL;
			END p1;
			
			CREATE PACKAGE p IS
			   PROCEDURE p2 (io_param IN OUT INTEGER);
			END p;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9110"].size)
	}

	@Test
	def void SelfInOutParameterNameOk() {
		val stmt = '''
			CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (
			   rect_length  NUMBER,
			   rect_width   NUMBER, 
			   CONSTRUCTOR FUNCTION rectangle (
			      self                 IN OUT NOCOPY rectangle,
			      in_length_and_width  IN NUMBER
			   ) RETURN SELF AS RESULT
			);
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9110"].size)
	}

	@Test
	def void recordTypeNameNok() {
		val stmt = '''
			DECLARE
			   TYPE dept_typ IS RECORD (
			      deptno NUMBER,
			      dname  VARCHAR2(14 CHAR),
			      loc    LOC(13 CHAR)
			   );
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9111"].size)
	}

	@Test
	def void recordTypeNameOk() {
		val stmt = '''
			DECLARE
			   TYPE r_dept_type IS RECORD (
			      deptno NUMBER,
			      dname  VARCHAR2(14 CHAR),
			      loc    LOC(13 CHAR)
			   );
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9111"].size)
	}

	@Test
	def void arrayTypeNameNok() {
		val stmt = '''
			DECLARE
			   TYPE t_varray          IS VARRAY(10) OF STRING;
			   TYPE nested_table_type IS TABLE OF STRING;
			   TYPE x_assoc_array_y   IS TABLE OF STRING INDEX BY PLS_INTEGER;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(3, issues.filter[it.code == "G-9112"].size)
	}

	@Test
	def void arrayTypeNameOk() {
		val stmt = '''
			DECLARE
			   TYPE t_varray_type       IS VARRAY(10) OF STRING;
			   TYPE t_nested_table_type IS TABLE OF STRING;
			   TYPE t_assoc_array_type  IS TABLE OF STRING INDEX BY PLS_INTEGER;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9112"].size)
	}

	@Test
	def void exceptionNameNok() {
		val stmt = '''
			DECLARE
			   some_name EXCEPTION;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9113"].size)
	}

	@Test
	def void exceptionNameOk() {
		val stmt = '''
			DECLARE
			   e_some_name EXCEPTION;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9113"].size)
	}

	@Test
	def void constantNameNok() {
		val stmt = '''
			DECLARE
			   maximum CONSTANT INTEGER := 1000;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9114"].size)
	}

	@Test
	def void constantNameOk() {
		val stmt = '''
			DECLARE
			   co_maximum CONSTANT INTEGER := 1000;
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9114"].size)
	}

	@Test
	def void subtypeNameNok() {
		val stmt = '''
			DECLARE
			   SUBTYPE short_text IS VARCHAR2(100 CHAR);
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.filter[it.code == "G-9115"].size)
	}

	@Test
	def void subtypeNameOk() {
		val stmt = '''
			DECLARE
			   SUBTYPE short_text_type IS VARCHAR2(100 CHAR);
			BEGIN
			   NULL;
			END;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.filter[it.code == "G-9115"].size)
	}

}
