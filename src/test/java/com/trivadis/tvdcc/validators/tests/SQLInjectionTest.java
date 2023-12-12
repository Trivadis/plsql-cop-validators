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
package com.trivadis.tvdcc.validators.tests;

import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.SQLInjection;

import java.util.Arrays;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SQLInjectionTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(SQLInjection.class);
    }

    @Test
    public void registeredChecks() {
        final HashMap<Integer, PLSQLCopGuideline> guidelines = getValidator().getGuidelines();
        Assert.assertEquals(1, guidelines.values().size());
        Assert.assertEquals(9501, guidelines.values().stream().toList().get(0).getId().intValue());
    }

    @Test
    public void openCursorNotAssertedVariable() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'SELECT * FROM #in_table_name#';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                   l_cur        SYS_REFCURSOR;
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   OPEN l_cur FOR l_sql;
                   CLOSE l_cur;
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(20, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());

    }

    @Test
    public void executeImmediateAssertedVariable() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                BEGIN
                   l_table_name := sys.dbms_assert.enquote_name(in_table_name);
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   EXECUTE IMMEDIATE l_sql;
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedNumberParameter() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN INTEGER) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                   l_table_name INTEGER;
                   l_sql        VARCHAR2(4000 BYTE);
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   EXECUTE IMMEDIATE l_sql;
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedVariable() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   EXECUTE IMMEDIATE l_sql;
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(6, issue.getLineNumber().intValue());
        Assert.assertEquals(20, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInFunction() {
        var stmt = """
                CREATE OR REPLACE FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   EXECUTE IMMEDIATE l_sql;
                   RETURN true;
                END f;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(6, issue.getLineNumber().intValue());
        Assert.assertEquals(20, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInPackageProcedure() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY pkg IS
                   PROCEDURE p (in_table_name IN VARCHAR2) AS
                      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                      l_table_name VARCHAR2(128 BYTE);
                      l_sql        VARCHAR2(4000 BYTE);
                   BEGIN
                      l_table_name := in_table_name;
                      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                      EXECUTE IMMEDIATE l_sql;
                   END p;
                END pkg;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(23, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInPackageFunction() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY pkg IS
                   FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
                      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                      l_table_name VARCHAR2(128 BYTE);
                      l_sql        VARCHAR2(4000 BYTE);
                   BEGIN
                      l_table_name := in_table_name;
                      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                      EXECUTE IMMEDIATE l_sql;
                      RETURN true;
                   END f;
                END pkg;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(23, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeProcedure() {
        var stmt = """
                CREATE OR REPLACE TYPE BODY typ IS
                   MEMBER PROCEDURE p (in_table_name IN VARCHAR2) AS
                      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                      l_table_name VARCHAR2(128 BYTE);
                      l_sql        VARCHAR2(4000 BYTE);
                   BEGIN
                      l_table_name := in_table_name;
                      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                      EXECUTE IMMEDIATE l_sql;
                   END p;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(23, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeFunction() {
        var stmt = """
                CREATE OR REPLACE TYPE BODY typ IS
                   MEMBER FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
                      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                      l_table_name VARCHAR2(128 BYTE);
                      l_sql        VARCHAR2(4000 BYTE);
                   BEGIN
                      l_table_name := in_table_name;
                      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                      EXECUTE IMMEDIATE l_sql;
                      RETURN true;
                   END f;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(23, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeConstructor() {
        var stmt = """
                CREATE OR REPLACE TYPE BODY typ IS
                   CONSTRUCTOR FUNCTION f (in_table_name IN VARCHAR2) RETURN SELF AS RESULT AS
                      co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                      l_table_name VARCHAR2(128 BYTE);
                      l_sql        VARCHAR2(4000 BYTE);
                   BEGIN
                      l_table_name := in_table_name;
                      l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                      EXECUTE IMMEDIATE l_sql;
                      RETURN;
                   END f;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, issue.getLineNumber().intValue());
        Assert.assertEquals(23, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateAssertedExpression() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                BEGIN
                   EXECUTE IMMEDIATE 'DROP TABLE '
                      || sys.dbms_assert.enquote_name(in_table_name)
                      || ' PURGE';
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedExpression() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                BEGIN
                   EXECUTE IMMEDIATE 'DROP TABLE '
                      || in_table_name
                      || ' PURGE';
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        var issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(4, issue.getLineNumber().intValue());
        Assert.assertEquals(10, issue.getColumn().intValue());
        Assert.assertEquals(13, issue.getLength().intValue());
    }

    @Test
    public void executeImmediateAssertedViaPublicSynonym() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                BEGIN
                   EXECUTE IMMEDIATE 'DROP TABLE '
                      || dbms_assert.enquote_name(in_table_name)
                      || ' PURGE';
                END p;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue1_ut_utils_wrapper() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue14_duplicate_warnings() {
        var stmt = """
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
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        var issue_p_config_name = issues.stream()
                .filter(i -> Arrays.stream(i.getData()).anyMatch(d -> d.contains("p_config_name"))).toList()
                .get(0);
        Assert.assertEquals(10, issue_p_config_name.getLineNumber().intValue());
        var issue_p_where = issues.stream()
                .filter(i -> Arrays.stream(i.getData()).anyMatch(d -> d.contains("p_where"))).toList().get(0);
        Assert.assertEquals(16, issue_p_where.getLineNumber().intValue());
    }

    @Test
    public void issue14_duplicate_warnings_are_ok_for_multiple_plsql_statements() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'SELECT * FROM #in_table_name#';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                   l_cur        SYS_REFCURSOR;
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   OPEN l_cur FOR l_sql;
                   CLOSE l_cur;
                   l_sql := 'BEGIN dbms_output.put_line(' || l_table_name || '); END;';
                   EXECUTE IMMEDIATE l_sql;
                END p;
                """;
        // every use of OpenForStatement and ExecuteImmediateStatement leads to a warning
        var issues = getIssues(stmt).stream()
                .filter(it -> Arrays.stream(it.getData()).toList().contains("in_table_name")).toList();
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals(7, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(7, issues.get(1).getLineNumber().intValue());
    }

    @Test
    public void issue23_assign_parameter_in_declare_section_nok() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE get_record (
                   user_name     IN   VARCHAR2,
                   service_type  IN   VARCHAR2,
                   rec           OUT  VARCHAR2
                ) IS
                   l_user_name     VARCHAR2(4000) := user_name;
                   l_service_type  VARCHAR2(4000) := service_type;
                   query           VARCHAR2(4000);
                BEGIN
                   -- Following SELECT statement is vulnerable to modification
                   -- because it uses concatenation to build WHERE clause.
                   query := q'[SELECT value FROM secret_records WHERE user_name=']'
                            || l_user_name
                            || q'[' AND service_type=']'
                            || l_service_type
                            || q'[']';
                   DBMS_OUTPUT.PUT_LINE('Query: ' || query);
                   EXECUTE IMMEDIATE query INTO rec;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
    }

    @Test
    public void issue23_assign_parameter_in_declare_section_ok() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE get_record (
                   user_name     IN   VARCHAR2,
                   service_type  IN   VARCHAR2,
                   rec           OUT  VARCHAR2
                ) IS
                   l_user_name     VARCHAR2(4000) := sys.dbms_assert.enquote_name(user_name);
                   l_service_type  VARCHAR2(4000) := sys.dbms_assert.enquote_name(service_type);
                   query           VARCHAR2(4000);
                BEGIN
                   -- Following SELECT statement is vulnerable to modification
                   -- because it uses concatenation to build WHERE clause.
                   query := q'[SELECT value FROM secret_records WHERE user_name=']'
                            || l_user_name
                            || q'[' AND service_type=']'
                            || l_service_type
                            || q'[']';
                   DBMS_OUTPUT.PUT_LINE('Query: ' || query);
                   EXECUTE IMMEDIATE query INTO rec;
                   DBMS_OUTPUT.PUT_LINE('Rec: ' || rec);
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue24_using_parameter_without_expression_in_execute_immediate() {
        var stmt = """
                CREATE PROCEDURE p (
                   in_sql VARCHAR2
                ) IS
                BEGIN
                   EXECUTE IMMEDIATE in_sql;
                END;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void issue55_using_unasserted_constant_in_execute_immediate() {
        var stmt = """
                create or replace procedure exec_sql(in_sql in varchar2) is
                   co_sql constant varchar2(1000 char) := in_sql;
                begin
                   execute immediate co_sql;
                end exec_sql;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void issue55_using_asserted_constant_in_execute_immediate() {
        var stmt = """
                create or replace procedure exec_sql(in_sql in varchar2) is
                   co_sql constant varchar2(1000 char) := sys.dbms_assert.noop(in_sql);
                begin
                   execute immediate co_sql;
                end exec_sql;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue56_using_asserted_variable_with_default_in_execute_immediate() {
        var stmt = """
                create or replace procedure exec_sql(in_sql in varchar2) is
                   l_sql          varchar2(1000 char) := in_sql;
                   l_sql_asserted varchar2(1000 char);
                begin
                   l_sql_asserted := sys.dbms_assert.noop(l_sql);
                   execute immediate l_sql_asserted;
                end exec_sql;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue56_using_asserted_constant_with_default_in_execute_immediate() {
        var stmt = """
                create or replace procedure exec_sql(in_sql in varchar2) is
                   co_sql         constant varchar2(1000 char) := in_sql;
                   l_sql_asserted varchar2(1000 char);
                begin
                   l_sql_asserted := sys.dbms_assert.noop(co_sql);
                   execute immediate l_sql_asserted;
                end exec_sql;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }
}
