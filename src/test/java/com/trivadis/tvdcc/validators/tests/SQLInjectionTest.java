/**
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
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.SQLInjection;
import java.util.HashMap;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.ArrayExtensions;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class SQLInjectionTest extends AbstractValidatorTest {
    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(SQLInjection.class);
    }

    @Test
    public void registeredChecks() {
        PLSQLValidator _validator = this.getValidator();
        final HashMap<Integer, PLSQLCopGuideline> guidelines = ((SQLInjection) _validator).getGuidelines();
        Assert.assertEquals(1, guidelines.values().size());
        Assert.assertEquals(9501,
                ((((PLSQLCopGuideline[]) Conversions.unwrapArray(guidelines.values(), PLSQLCopGuideline.class))[0])
                        .getId()).intValue());
    }

    @Test
    public void openCursorNotAssertedVariable() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'SELECT * FROM #in_table_name#\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_cur        SYS_REFCURSOR;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("OPEN l_cur FOR l_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CLOSE l_cur;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(20, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateAssertedVariable() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := sys.dbms_assert.enquote_name(in_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedNumberParameter() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN INTEGER) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedVariable() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(6, (issue.getLineNumber()).intValue());
        Assert.assertEquals(20, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInFunction() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("RETURN true;");
        _builder.newLine();
        _builder.append("END f;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(6, (issue.getLineNumber()).intValue());
        Assert.assertEquals(20, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInPackageProcedure() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY pkg IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END p;");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(23, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInPackageFunction() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY pkg IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("RETURN true;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END f;");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(23, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeProcedure() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE BODY typ IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("MEMBER PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END p;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(23, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeFunction() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE BODY typ IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("MEMBER FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("RETURN true;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END f;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(23, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateNotAssertedVariableInTypeConstructor() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE TYPE BODY typ IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CONSTRUCTOR FUNCTION f (in_table_name IN VARCHAR2) RETURN SELF AS RESULT AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'DROP TABLE #in_table_name# PURGE\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("RETURN;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END f;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(7, (issue.getLineNumber()).intValue());
        Assert.assertEquals(23, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateAssertedExpression() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE \'DROP TABLE \'");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| sys.dbms_assert.enquote_name(in_table_name)");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| \' PURGE\';");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void executeImmediateNotAssertedExpression() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE \'DROP TABLE \'");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| in_table_name");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| \' PURGE\';");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        final Issue issue = issues.get(0);
        Assert.assertEquals("G-9501", issue.getCode());
        Assert.assertEquals("in_table_name", issue.getData()[0]);
        Assert.assertEquals(4, (issue.getLineNumber()).intValue());
        Assert.assertEquals(10, (issue.getColumn()).intValue());
        Assert.assertEquals(13, (issue.getLength()).intValue());
    }

    @Test
    public void executeImmediateAssertedViaPublicSynonym() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE \'DROP TABLE \'");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| dbms_assert.enquote_name(in_table_name)");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("|| \' PURGE\';");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue1_ut_utils_wrapper() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("create or replace package body ut_annotation_manager as");
        _builder.newLine();
        _builder.append("  ");
        _builder.append("-- ...");
        _builder.newLine();
        _builder.append("  ");
        _builder.append(
                "function get_missing_objects(a_object_owner varchar2, a_object_type varchar2) return ut_annotation_objs_cache_info is");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_rows         sys_refcursor;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_ut_owner     varchar2(250) := ut_utils.ut_owner;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_objects_view varchar2(200) := ut_metadata.get_objects_view_name();");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_cursor_text  varchar2(32767);");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_data         ut_annotation_objs_cache_info;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_result       ut_annotation_objs_cache_info;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_card         natural;");
        _builder.newLine();
        _builder.append("  ");
        _builder.append("begin");
        _builder.newLine();
        _builder.append("    ");
        _builder.append(
                "l_data := ut_annotation_cache_manager.get_annotations_objects_info(a_object_owner, a_object_type);");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_card := ut_utils.scale_cardinality(cardinality(l_data));");
        _builder.newLine();
        _builder.newLine();
        _builder.append("    ");
        _builder.append("l_cursor_text :=");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("\'select /*+ cardinality(i \'||l_card||\') */");
        _builder.newLine();
        _builder.append("                ");
        _builder.append("value(i)");
        _builder.newLine();
        _builder.append("           ");
        _builder.append("from table( cast( :l_data as \'||l_ut_owner||\'.ut_annotation_objs_cache_info ) ) i");
        _builder.newLine();
        _builder.append("           ");
        _builder.append("where");
        _builder.newLine();
        _builder.append("             ");
        _builder.append("not exists (");
        _builder.newLine();
        _builder.append("                ");
        _builder.append("select 1  from \'||l_objects_view||q\'[ o");
        _builder.newLine();
        _builder.append("                 ");
        _builder.append("where o.owner = i.object_owner");
        _builder.newLine();
        _builder.append("                   ");
        _builder.append("and o.object_name = i.object_name");
        _builder.newLine();
        _builder.append("                   ");
        _builder.append("and o.object_type = i.object_type");
        _builder.newLine();
        _builder.append("                   ");
        _builder.append("and o.owner       = \']\'||ut_utils.qualified_sql_name(a_object_owner)||q\'[\'");
        _builder.newLine();
        _builder.append("                   ");
        _builder.append("and o.object_type = \']\'||ut_utils.qualified_sql_name(a_object_type)||q\'[\'");
        _builder.newLine();
        _builder.append("                ");
        _builder.append(")]\';");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("open l_rows for l_cursor_text  using l_data;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("fetch l_rows bulk collect into l_result limit ut_utils.gc_max_objects_fetch_limit;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("close l_rows;");
        _builder.newLine();
        _builder.append("    ");
        _builder.append("return l_result;");
        _builder.newLine();
        _builder.append("  ");
        _builder.append("end;");
        _builder.newLine();
        _builder.append("  ");
        _builder.append("-- ...");
        _builder.newLine();
        _builder.append("end ut_annotation_manager;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue14_duplicate_warnings() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY pkg IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p (");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("p_config_name IN VARCHAR2,");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("p_where       IN VARCHAR2");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") IS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_sql           VARCHAR2(32767);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_search_config search_config_t%ROWTYPE;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_source        VARCHAR2(40) := \'\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_search_config := get_config_info(p_config_name => p_config_name);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("IF p_where IS NULL THEN");
        _builder.newLine();
        _builder.append("         ");
        _builder.append("RETURN 0;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("END IF;");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_sql := \'DELETE FROM \' || v_search_config.load_target");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| \' <WHERE>\';");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("v_sql := REPLACE(v_sql, \'<WHERE>\', p_where);");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("EXECUTE IMMEDIATE v_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END refresh_work_full;");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(ArrayExtensions.contains(it.getData(), "p_config_name"));
        };
        final Issue issue_p_config_name = IterableExtensions.<Issue>findFirst(issues, _function);
        Assert.assertEquals(10, (issue_p_config_name.getLineNumber()).intValue());
        final Function1<Issue, Boolean> _function_1 = (Issue it) -> {
            return Boolean.valueOf(ArrayExtensions.contains(it.getData(), "p_where"));
        };
        final Issue issue_p_where = IterableExtensions.<Issue>findFirst(issues, _function_1);
        Assert.assertEquals(16, (issue_p_where.getLineNumber()).intValue());
    }

    @Test
    public void issue14_duplicate_warnings_are_ok_for_multiple_plsql_statements() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ     CONSTANT VARCHAR2(4000 BYTE) := \'SELECT * FROM #in_table_name#\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name VARCHAR2(128 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql        VARCHAR2(4000 BYTE);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_cur        SYS_REFCURSOR;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_table_name := in_table_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := replace(l_templ, \'#in_table_name#\', l_table_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("OPEN l_cur FOR l_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("CLOSE l_cur;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql := \'BEGIN dbms_output.put_line(\' || l_table_name || \'); END;\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE l_sql;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(ArrayExtensions.contains(it.getData(), "in_table_name"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(2, IterableExtensions.size(issues));
        Assert.assertEquals(7,
                ((((Issue[]) Conversions.unwrapArray(issues, Issue.class))[0]).getLineNumber()).intValue());
        Assert.assertEquals(7,
                ((((Issue[]) Conversions.unwrapArray(issues, Issue.class))[1]).getLineNumber()).intValue());
    }

    @Test
    public void issue23_assign_parameter_in_declare_section_nok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE get_record (");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("user_name     IN   VARCHAR2,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("service_type  IN   VARCHAR2,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rec           OUT  VARCHAR2");
        _builder.newLine();
        _builder.append(") IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_user_name     VARCHAR2(4000) := user_name;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_service_type  VARCHAR2(4000) := service_type;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("query           VARCHAR2(4000);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("-- Following SELECT statement is vulnerable to modification");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("-- because it uses concatenation to build WHERE clause.");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("query := q\'[SELECT value FROM secret_records WHERE user_name=\']\'");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| l_user_name");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| q\'[\' AND service_type=\']\'");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| l_service_type");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| q\'[\']\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("DBMS_OUTPUT.PUT_LINE(\'Query: \' || query);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE query INTO rec;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("DBMS_OUTPUT.PUT_LINE(\'Rec: \' || rec);");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(2, issues.size());
    }

    @Test
    public void issue23_assign_parameter_in_declare_section_ok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE get_record (");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("user_name     IN   VARCHAR2,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("service_type  IN   VARCHAR2,");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("rec           OUT  VARCHAR2");
        _builder.newLine();
        _builder.append(") IS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_user_name     VARCHAR2(4000) := sys.dbms_assert.enquote_name(user_name);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_service_type  VARCHAR2(4000) := sys.dbms_assert.enquote_name(service_type);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("query           VARCHAR2(4000);");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("-- Following SELECT statement is vulnerable to modification");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("-- because it uses concatenation to build WHERE clause.");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("query := q\'[SELECT value FROM secret_records WHERE user_name=\']\'");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| l_user_name");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| q\'[\' AND service_type=\']\'");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| l_service_type");
        _builder.newLine();
        _builder.append("            ");
        _builder.append("|| q\'[\']\';");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("DBMS_OUTPUT.PUT_LINE(\'Query: \' || query);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE query INTO rec;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("DBMS_OUTPUT.PUT_LINE(\'Rec: \' || rec);");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue24_using_parameter_without_expression_in_execute_immediate() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PROCEDURE p (");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("in_sql VARCHAR2");
        _builder.newLine();
        _builder.append(") IS");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("EXECUTE IMMEDIATE in_sql;");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void issue55_using_unasserted_constant_in_execute_immediate() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("create or replace procedure exec_sql(in_sql in varchar2) is");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_sql constant varchar2(1000 char) := in_sql;");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("execute immediate co_sql;");
        _builder.newLine();
        _builder.append("end exec_sql;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(1, issues.size());
    }

    @Test
    public void issue55_using_asserted_constant_in_execute_immediate() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("create or replace procedure exec_sql(in_sql in varchar2) is");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_sql constant varchar2(1000 char) := sys.dbms_assert.noop(in_sql);");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("execute immediate co_sql;");
        _builder.newLine();
        _builder.append("end exec_sql;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue56_using_asserted_variable_with_default_in_execute_immediate() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("create or replace procedure exec_sql(in_sql in varchar2) is");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql          varchar2(1000 char) := in_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql_asserted varchar2(1000 char);");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql_asserted := sys.dbms_assert.noop(l_sql);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("execute immediate l_sql_asserted;");
        _builder.newLine();
        _builder.append("end exec_sql;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void issue56_using_asserted_constant_with_default_in_execute_immediate() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("create or replace procedure exec_sql(in_sql in varchar2) is");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_sql         constant varchar2(1000 char) := in_sql;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql_asserted varchar2(1000 char);");
        _builder.newLine();
        _builder.append("begin");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_sql_asserted := sys.dbms_assert.noop(co_sql);");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("execute immediate l_sql_asserted;");
        _builder.newLine();
        _builder.append("end exec_sql;");
        _builder.newLine();
        _builder.append("/");
        _builder.newLine();
        final String stmt = _builder.toString();
        final List<Issue> issues = this.getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }
}
