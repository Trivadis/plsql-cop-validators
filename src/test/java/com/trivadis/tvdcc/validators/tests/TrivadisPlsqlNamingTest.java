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
package com.trivadis.tvdcc.validators.tests;

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TrivadisPlsqlNamingTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisPlsqlNaming.class);
    }

    @Test
    public void globalVariableNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE example AS
                   some_name INTEGER;
                END example;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9101")).toList().size());
    }

    @Test
    public void globalVariableOk() {
        var stmt = """
                CREATE OR REPLACE PACKAGE example AS
                   g_some_name INTEGER;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9101")).toList().size());
    }

    @Test
    public void localVariableNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY example AS
                   PROCEDURE a IS
                      some_name INTEGER;
                   BEGIN
                      NULL;
                   END a;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableOk() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY example AS
                   PROCEDURE a IS
                      l_some_name INTEGER;
                   BEGIN
                      NULL;
                   END a;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableForStrongCursorOk() {
        var stmt = """
                declare
                   type c_emp_type is ref cursor return employees%rowtype;
                   c_emp c_emp_type;
                   r_emp employees%rowtype;
                begin
                   open c_emp for select * from employees where employee_id = 100;
                   fetch c_emp into r_emp;
                   close c_emp;
                   sys.dbms_output.put_line('first_name: ' || r_emp.first_name);
                end;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableForObjectOk() {
        var stmt = """
                declare
                   o_game game_ot;
                begin
                   o_game := game_ot();
                   pkg.proc(o_game);
                end;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableForArrayOk() {
        var stmt = """
                declare
                   t_words word_ct;
                begin
                   t_words := word_ct();
                   pkg.proc(t_words);
                end;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableSingleLetterIOk() {
        // use as common index "i" is accepted
        var stmt = """
                declare
                   i pls_integer := 0;
                begin
                   while i < 10
                   loop
                      dbms_output.put_line(i);
                      i := i + 1;
                   end loop;
                end;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void localVariableSingleLetterJOk() {
        // use as common index "j" is accepted
        var stmt = """
                declare
                   j pls_integer := 0;
                begin
                   while j < 10
                   loop
                      dbms_output.put_line(j);
                      j := j + 1;
                   end loop;
                end;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    @Test
    public void cursorNameNok() {
        var stmt = """
                DECLARE
                   CURSOR some_name IS SELECT * FROM emp;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9103")).toList().size());
    }

    @Test
    public void cursorNameOk() {
        var stmt = """
                DECLARE
                   CURSOR c_some_name IS SELECT * FROM emp;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9103")).toList().size());
    }

    @Test
    public void sysrefcursorNameNOk_bug5() {
        var stmt = """
                DECLARE
                   l_dept SYS_REFCURSOR;
                BEGIN
                   NULL;
                END;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9103")).toList().size());
    }

    @Test
    public void sysrefcursorNameOk_bug5() {
        var stmt = """
                DECLARE
                   c_dept SYS_REFCURSOR;
                BEGIN
                   NULL;
                END;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9103")).toList().size());
    }

    @Test
    public void recordNameNok() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.stream().filter(it -> it.getCode().equals("G-9104")).toList().size());
    }

    @Test
    public void recordNameOk() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9104")).toList().size());
    }

    @Test
    public void arrayNameNok() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(3, issues.stream().filter(it -> it.getCode().equals("G-9105")).toList().size());
    }

    @Test
    public void arrayNameOk() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9105")).toList().size());
    }

    @Test
    public void objectNameNok() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9106")).toList().size());
    }

    @Test
    public void objectNameOk() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9106")).toList().size());
    }

    @Test
    public void cursorParameterNameNok() {
        var stmt = """
                DECLARE
                   CURSOR c_emp (x_ename IN VARCHAR2) IS
                      SELECT *
                        FROM emp
                       WHERE ename LIKE x_ename;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9107")).toList().size());
    }

    @Test
    public void cursorParameterNameOk() {
        var stmt = """
                DECLARE
                   CURSOR c_emp (p_ename IN VARCHAR2) IS
                      SELECT *
                        FROM emp
                       WHERE ename LIKE p_ename;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9107")).toList().size());
    }

    @Test
    public void inParameterNameNok() {
        var stmt = """
                CREATE PROCEDURE p1 (param INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (param IN INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.stream().filter(it -> it.getCode().equals("G-9108")).toList().size());
    }

    @Test
    public void inParameterNameOk() {
        var stmt = """
                CREATE PROCEDURE p1 (in_param INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (in_param IN INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9108")).toList().size());
    }

    @Test
    public void SelfInParameterNameOk() {
        var stmt = """
                CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (
                   rect_length  NUMBER,
                   rect_width   NUMBER,
                   member FUNCTION get_surface (
                      self IN rectangle
                   ) RETURN NUMBER
                );
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9108")).toList().size());
    }

    @Test
    public void outParameterNameNok() {
        var stmt = """
                CREATE PROCEDURE p1 (param OUT INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (param OUT INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.stream().filter(it -> it.getCode().equals("G-9109")).toList().size());
    }

    @Test
    public void outParameterNameOk() {
        var stmt = """
                CREATE PROCEDURE p1 (out_param OUT INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (out_param OUT INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9109")).toList().size());
    }

    @Test
    public void inOutParameterNameNok() {
        var stmt = """
                CREATE PROCEDURE p1 (param IN OUT INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (param IN OUT INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.stream().filter(it -> it.getCode().equals("G-9110")).toList().size());
    }

    @Test
    public void inOutParameterNameOk() {
        var stmt = """
                CREATE PROCEDURE p1 (io_param IN OUT INTEGER) IS
                BEGIN
                   NULL;
                END p1;

                CREATE PACKAGE p IS
                   PROCEDURE p2 (io_param IN OUT INTEGER);
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9110")).toList().size());
    }

    @Test
    public void SelfInOutParameterNameOk() {
        var stmt = """
                CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (
                   rect_length  NUMBER,
                   rect_width   NUMBER,
                   CONSTRUCTOR FUNCTION rectangle (
                      self                 IN OUT NOCOPY rectangle,
                      in_length_and_width  IN NUMBER
                   ) RETURN SELF AS RESULT
                );
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9110")).toList().size());
    }

    @Test
    public void recordTypeNameNok() {
        var stmt = """
                DECLARE
                   TYPE dept_typ IS RECORD (
                      deptno NUMBER,
                      dname  VARCHAR2(14 CHAR),
                      loc    LOC(13 CHAR)
                   );
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9111")).toList().size());
    }

    @Test
    public void recordTypeNameOk() {
        var stmt = """
                DECLARE
                   TYPE r_dept_type IS RECORD (
                      deptno NUMBER,
                      dname  VARCHAR2(14 CHAR),
                      loc    LOC(13 CHAR)
                   );
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9111")).toList().size());
    }

    @Test
    public void arrayTypeNameNok() {
        var stmt = """
                DECLARE
                   TYPE t_varray          IS VARRAY(10) OF STRING;
                   TYPE nested_table_type IS TABLE OF STRING;
                   TYPE x_assoc_array_y   IS TABLE OF STRING INDEX BY PLS_INTEGER;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(3, issues.stream().filter(it -> it.getCode().equals("G-9112")).toList().size());
    }

    @Test
    public void arrayTypeNameOk() {
        var stmt = """
                DECLARE
                   TYPE t_varray_type       IS VARRAY(10) OF STRING;
                   TYPE t_nested_table_type IS TABLE OF STRING;
                   TYPE t_assoc_array_type  IS TABLE OF STRING INDEX BY PLS_INTEGER;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9112")).toList().size());

    }

    @Test
    public void exceptionNameNok() {
        var stmt = """
                DECLARE
                   some_name EXCEPTION;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9113")).toList().size());
    }

    @Test
    public void exceptionNameOk() {
        var stmt = """
                DECLARE
                   e_some_name EXCEPTION;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9113")).toList().size());
    }

    @Test
    public void constantNameNok() {
        var stmt = """
                DECLARE
                   maximum CONSTANT INTEGER := 1000;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9114")).toList().size());
    }

    @Test
    public void constantNameOk() {
        var stmt = """
                DECLARE
                   co_maximum CONSTANT INTEGER := 1000;
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9114")).toList().size());
    }

    @Test
    public void subtypeNameNok() {
        var stmt = """
                DECLARE
                   SUBTYPE short_text IS VARCHAR2(100 CHAR);
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9115")).toList().size());
    }

    @Test
    public void subtypeNameOk() {
        var stmt = """
                DECLARE
                   SUBTYPE short_text_type IS VARCHAR2(100 CHAR);
                BEGIN
                   NULL;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9115")).toList().size());
    }
}
