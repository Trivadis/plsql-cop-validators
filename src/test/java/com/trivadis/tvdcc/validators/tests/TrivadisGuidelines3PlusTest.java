/**
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
package com.trivadis.tvdcc.validators.tests;

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus;
import java.util.HashMap;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class TrivadisGuidelines3PlusTest extends AbstractValidatorTest {
  @BeforeClass
  public static void setupValidator() {
    PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisGuidelines3Plus.class);
  }

  @Test
  public void guidelines() {
    PLSQLValidator _validator = this.getValidator();
    final HashMap<Integer, PLSQLCopGuideline> guidelines = ((TrivadisGuidelines3Plus) _validator).getGuidelines();
    final Function1<PLSQLCopGuideline, Boolean> _function = (PLSQLCopGuideline it) -> {
      Integer _id = it.getId();
      return Boolean.valueOf(((_id).intValue() >= 9100));
    };
    Assert.assertEquals(22, IterableExtensions.size(IterableExtensions.<PLSQLCopGuideline>filter(guidelines.values(), _function)));
    final Function1<PLSQLCopGuideline, Boolean> _function_1 = (PLSQLCopGuideline it) -> {
      Integer _id = it.getId();
      return Boolean.valueOf(((_id).intValue() < 9100));
    };
    Assert.assertEquals(123, IterableExtensions.size(IterableExtensions.<PLSQLCopGuideline>filter(guidelines.values(), _function_1)));
    final Function1<PLSQLCopGuideline, Boolean> _function_2 = (PLSQLCopGuideline it) -> {
      Integer _id = it.getId();
      return Boolean.valueOf(((_id).intValue() < 1000));
    };
    Assert.assertEquals(79, IterableExtensions.size(IterableExtensions.<PLSQLCopGuideline>filter(guidelines.values(), _function_2)));
  }

  @Test
  public void getGuidelineId_mapped_via_Trivadis2() {
    Assert.assertEquals("G-1010", this.getValidator().getGuidelineId(Integer.valueOf(1)));
  }

  @Test
  public void getGuidelineId_of_Trivadis3() {
    Assert.assertEquals("G-2130", this.getValidator().getGuidelineId(Integer.valueOf(2130)));
  }

  @Test
  public void getGuidelineMsg_mapped_via_Trivadis2() {
    Assert.assertEquals("G-1010: Try to label your sub blocks.", this.getValidator().getGuidelineMsg(Integer.valueOf(1)));
  }

  @Test
  public void getGuidelineMsg_mapped_via_Trivadis3() {
    Assert.assertEquals("G-2130: Try to use subtypes for constructs used often in your code.", this.getValidator().getGuidelineMsg(Integer.valueOf(2130)));
  }

  @Test
  public void literalInLoggerCallIsOkay() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("logger.log(\'Hello World\');");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-1050"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(0, IterableExtensions.size(issues));
  }

  @Test
  public void literalInDbmsOutputCallIsNotOkay() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("dbms_output.put_line(\'Hello World\');");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("dbms_output.put_line(\'Hello World\');");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-1050"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(2, IterableExtensions.size(issues));
  }

  @Test
  public void guideline2230_na() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE BODY constants_up IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("co_big_increase CONSTANT NUMBER(5,0) := 1;");
    _builder.newLine();
    _builder.append("   ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("FUNCTION big_increase RETURN NUMBER DETERMINISTIC IS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("RETURN co_big_increase;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END big_increase;");
    _builder.newLine();
    _builder.append("END constants_up;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-2230"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(1, IterableExtensions.size(issues));
  }

  @Test
  public void guideline1010_10() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN ");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("NULL;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END;");
    _builder.newLine();
    _builder.append("END;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-1010"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(1, IterableExtensions.size(issues));
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
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9501"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(1, IterableExtensions.size(issues));
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
  public void unknownHint() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9601"));
    };
    Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
  }
}
