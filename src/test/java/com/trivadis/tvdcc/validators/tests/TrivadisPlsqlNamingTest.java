/**
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

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class TrivadisPlsqlNamingTest extends AbstractValidatorTest {
    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisPlsqlNaming.class);
    }

    @Test
    public void globalVariableNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE example AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("some_name INTEGER;");
        _builder.newLine();
        _builder.append("END example;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9101"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void globalVariableOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE example AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("g_some_name INTEGER;");
        _builder.newLine();
        _builder.append("END example;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9101"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY example AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE a IS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("some_name INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END a;");
        _builder.newLine();
        _builder.append("END example;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY example AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE a IS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_some_name INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END a;");
        _builder.newLine();
        _builder.append("END example;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableForStrongCursorOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("type c_emp_type is ref cursor return employees%rowtype;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("c_emp c_emp_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("r_emp employees%rowtype;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("open c_emp for select * from employees where employee_id = 100;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("fetch c_emp into r_emp;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("close c_emp;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("sys.dbms_output.put_line(\'first_name: \' || r_emp.first_name);");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableForObjectOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("o_game game_ot;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("o_game := game_ot();");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("pkg.proc(o_game);");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableForArrayOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("t_words word_ct;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("t_words := word_ct();");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("pkg.proc(t_words);");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableSingleLetterIOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("i pls_integer := 0;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("while i < 10");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("loop");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dbms_output.put_line(i);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("i := i + 1;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("end loop;");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableSingleLetterJOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("j pls_integer := 0;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("while j < 10");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("loop");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dbms_output.put_line(j);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("j := j + 1;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("end loop;");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void localVariableSingleLetterZNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("declare");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("z pls_integer := 0;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("while z < 10");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("loop");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dbms_output.put_line(z);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("z := z + 1;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("end loop;");
        _builder.newLine();
        _builder.append("end;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9102"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void cursorNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CURSOR some_name IS SELECT * FROM emp;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9103"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void cursorNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CURSOR c_some_name IS SELECT * FROM emp;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9103"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void sysrefcursorNameNOk_bug5() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_dept SYS_REFCURSOR;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9103"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void sysrefcursorNameOk_bug5() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("c_dept SYS_REFCURSOR;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9103"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void recordNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("emp emp%ROWTYPE;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE r_dept_type IS RECORD (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("deptno NUMBER,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("loc    LOC(13 CHAR)");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(");");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("dept r_dept_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9104"));
        };
        Assert.assertEquals(2, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void recordNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("r_emp emp%ROWTYPE;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE r_dept_type IS RECORD (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("deptno NUMBER,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("loc    LOC(13 CHAR)");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(");");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("r_dept r_dept_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9104"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void arrayNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_varray_type IS VARRAY(10) OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("array1 t_varray_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_nested_table_type IS TABLE OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("array2 t_nested_table_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_assoc_array_type IS TABLE OF STRING INDEX BY PLS_INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("array3 t_assoc_array_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9105"));
        };
        Assert.assertEquals(3, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void arrayNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_varray_type IS VARRAY(10) OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("t_array1 t_varray_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_nested_table_type IS TABLE OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("t_array2 t_nested_table_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_assoc_array_type IS TABLE OF STRING INDEX BY PLS_INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("t_array3 t_assoc_array_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9105"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void objectNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE dept_type AS OBJECT (");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("deptno INTEGER,");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("loc    VARCHAR2(13 CHAR)");
        _builder.newLine();
        _builder.append(");");
        _builder.newLine();
        _builder.newLine();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("dept dept_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9106"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void objectNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE dept_type AS OBJECT (");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("deptno INTEGER,");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("loc    VARCHAR2(13 CHAR)");
        _builder.newLine();
        _builder.append(");");
        _builder.newLine();
        _builder.newLine();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("o_dept dept_type;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9106"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void cursorParameterNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CURSOR c_emp (x_ename IN VARCHAR2) IS ");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("SELECT * ");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("FROM emp");
        _builder.newLine();
        _builder.append("       ");
        _builder.append("WHERE ename LIKE x_ename;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9107"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void cursorParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CURSOR c_emp (p_ename IN VARCHAR2) IS ");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("SELECT * ");
        _builder.newLine();
        _builder.append("        ");
        _builder.append("FROM emp");
        _builder.newLine();
        _builder.append("       ");
        _builder.append("WHERE ename LIKE p_ename;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9107"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void inParameterNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (param INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (param IN INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9108"));
        };
        Assert.assertEquals(2, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void inParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (in_param INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (in_param IN INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9108"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void SelfInParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rect_length  NUMBER,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rect_width   NUMBER, ");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("member FUNCTION get_surface (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("self IN rectangle");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") RETURN NUMBER");
        _builder.newLine();
        _builder.append(");");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9108"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void outParameterNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (param OUT INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (param OUT INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9109"));
        };
        Assert.assertEquals(2, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void outParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (out_param OUT INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (out_param OUT INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9109"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void inOutParameterNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (param IN OUT INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (param IN OUT INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9110"));
        };
        Assert.assertEquals(2, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void inOutParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p1 (io_param IN OUT INTEGER) IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p1;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("CREATE PACKAGE p IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p2 (io_param IN OUT INTEGER);");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9110"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void SelfInOutParameterNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE rectangle AUTHID definer AS OBJECT (");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rect_length  NUMBER,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rect_width   NUMBER, ");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CONSTRUCTOR FUNCTION rectangle (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("self                 IN OUT NOCOPY rectangle,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("in_length_and_width  IN NUMBER");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") RETURN SELF AS RESULT");
        _builder.newLine();
        _builder.append(");");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9110"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void recordTypeNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE dept_typ IS RECORD (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("deptno NUMBER,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("loc    LOC(13 CHAR)");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(");");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9111"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void recordTypeNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE r_dept_type IS RECORD (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("deptno NUMBER,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("dname  VARCHAR2(14 CHAR),");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("loc    LOC(13 CHAR)");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(");");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9111"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void arrayTypeNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_varray          IS VARRAY(10) OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE nested_table_type IS TABLE OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE x_assoc_array_y   IS TABLE OF STRING INDEX BY PLS_INTEGER;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9112"));
        };
        Assert.assertEquals(3, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void arrayTypeNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_varray_type       IS VARRAY(10) OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_nested_table_type IS TABLE OF STRING;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("TYPE t_assoc_array_type  IS TABLE OF STRING INDEX BY PLS_INTEGER;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9112"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void exceptionNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("some_name EXCEPTION;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9113"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void exceptionNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("e_some_name EXCEPTION;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9113"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void constantNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("maximum CONSTANT INTEGER := 1000;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9114"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void constantNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_maximum CONSTANT INTEGER := 1000;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9114"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void subtypeNameNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("SUBTYPE short_text IS VARCHAR2(100 CHAR);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9115"));
        };
        Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }

    @Test
    public void subtypeNameOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("DECLARE");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("SUBTYPE short_text_type IS VARCHAR2(100 CHAR);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-9115"));
        };
        Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
    }
}
