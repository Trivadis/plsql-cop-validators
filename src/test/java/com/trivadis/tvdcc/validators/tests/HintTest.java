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
import com.trivadis.tvdcc.validators.Hint;
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
public class HintTest extends AbstractValidatorTest {
  @BeforeClass
  public static void setupValidator() {
    PLSQLValidatorPreferences.INSTANCE.setValidatorClass(Hint.class);
  }

  @Test
  public void registeredChecks() {
    PLSQLValidator _validator = this.getValidator();
    final HashMap<Integer, PLSQLCopGuideline> guidelines = ((Hint) _validator).getGuidelines();
    Assert.assertEquals(6, guidelines.values().size());
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9600)));
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9601)));
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9602)));
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9603)));
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9604)));
    Assert.assertNotNull(guidelines.get(Integer.valueOf(9605)));
  }

  @Test
  public void multipleHintComments() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT -- a comment");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("/*+ full(e) */");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("/* another comment */");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("--+ full(d)");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("e.empno,");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("e.ename,");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("d.dname");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM emp e");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("JOIN dept d");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("ON d.deptno = e.deptno");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("WHERE empno > 7900;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    Assert.assertEquals(4, (issues.get(0).getLineNumber()).intValue());
    Assert.assertEquals(8, (issues.get(0).getColumn()).intValue());
    Assert.assertTrue((((issues.get(0).getLength()).intValue() == 12) || ((issues.get(0).getLength()).intValue() == 13)));
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9600: Never define more than one comment with hints.");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
  }

  @Test
  public void commentWithInvalidHint() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /* NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void unknownMultiLineHint() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    Assert.assertEquals(1, (issues.get(0).getLineNumber()).intValue());
    Assert.assertEquals(12, (issues.get(0).getColumn()).intValue());
    Assert.assertEquals(9, (issues.get(0).getLength()).intValue());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
  }

  @Test
  public void unknownSingleLineHint() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT --+ APPEND NOLOGGING");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO sales_hist ");
    _builder.newLine();
    _builder.append("SELECT * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    Assert.assertEquals(1, (issues.get(0).getLineNumber()).intValue());
    Assert.assertEquals(19, (issues.get(0).getColumn()).intValue());
    Assert.assertEquals(9, (issues.get(0).getLength()).intValue());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
  }

  @Test
  public void validHint() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ APPEND */ INTO sales_hist SELECT * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void unknownHintInHintSequenceWithNestedParenthesis() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) NOLOGGING */ ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO sales_hist ");
    _builder.newLine();
    _builder.append("SELECT /*+ fulL(sales) */ * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    Assert.assertEquals("G-9601", issues.get(0).getCode());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
  }

  @Test
  public void validHintInHintSequenceWithNestedParenthesis() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) */ ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO sales_hist ");
    _builder.newLine();
    _builder.append("SELECT /*+ fulL(sales) */ * FROM sales;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validLeadingUsingTable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ leading(emp dept) */ * ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM emp");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("JOIN dept on dept.deptno = emp.deptno;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validLeadingUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ leading(e d) */ * ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM emp e");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("JOIN dept d on d.deptno = e.deptno;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidLeading() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ leading(emp dep) */ * ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM emp e");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("JOIN dept d on d.deptno = e.deptno;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(2, issues.size());
    Assert.assertEquals(1, (issues.get(0).getLineNumber()).intValue());
    Assert.assertEquals(20, (issues.get(0).getColumn()).intValue());
    Assert.assertEquals(3, (issues.get(0).getLength()).intValue());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9602: Always use the alias name instead of the table name. Use e instead of emp in leading hint.");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    Assert.assertEquals(1, (issues.get(1).getLineNumber()).intValue());
    Assert.assertEquals(24, (issues.get(1).getColumn()).intValue());
    Assert.assertEquals(3, (issues.get(1).getLength()).intValue());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9603: Never reference an unknown table/alias. (dep in leading hint).");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
  }

  @Test
  public void validLeadingWithSubquery() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ leading(e d) */ * ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM emp e");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("JOIN (SELECT * FROM dept d2) d on d.deptno = e.deptno;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validIgnoreRowOnDupkeyIndexUsingTable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept,pk_dept) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    _builder.newLine();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validIgnoreRowOnDupkeyIndexUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d,pk_dept) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept d VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    _builder.newLine();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d(deptno)) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept d VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidIgnoreRowOnDupkeyIndex() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dep,pk_dept) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept d VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    _builder.newLine();
    _builder.append("INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("INTO dept d VALUES (10, \'dname\', \'loc\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(2, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9603: Never reference an unknown table/alias. (dep in IGNORE_ROW_ON_DUPKEY_INDEX hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9602: Always use the alias name instead of the table name. Use d instead of dept in IGNORE_ROW_ON_DUPKEY_INDEX hint.");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
  }

  @Test
  public void validDynamicSamplingUsingTable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ DYNAMIC_SAMPLING(employees 10) */ count(*)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validDynamicSamplingUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ DYNAMIC_SAMPLING(e 5) */ count(*)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidDynamicSampling() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ DYNAMIC_SAMPLING(emp 15) DYNAMIC_SAMPLING(employees 15) */ count(*)");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(2, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9603: Never reference an unknown table/alias. (emp in DYNAMIC_SAMPLING hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9602: Always use the alias name instead of the table name. Use e instead of employees in DYNAMIC_SAMPLING hint.");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
  }

  @Test
  public void validIndexUsingTable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ INDEX ( employees emp_department_ix ) */ employee_id, department_id");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("WHERE department_id > 50;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validIndexUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ INDEX ( e emp_department_ix ) */ employee_id, department_id");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees e");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("WHERE department_id > 50;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidIndex() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ INDEX ( emp emp_department_ix ) INDEX (employees emp_department_ix) */ employee_id, department_id");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees e");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("WHERE department_id > 50;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(2, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9603: Never reference an unknown table/alias. (emp in INDEX hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9602: Always use the alias name instead of the table name. Use e instead of employees in INDEX hint.");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
  }

  @Test
  public void validMergeUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ MERGE(v) */ e1.last_name, e1.salary, v.avg_salary");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("FROM employees e1,");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("(SELECT department_id, avg(salary) avg_salary ");
    _builder.newLine();
    _builder.append("           ");
    _builder.append("FROM employees e2");
    _builder.newLine();
    _builder.append("           ");
    _builder.append("GROUP BY department_id) v ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("WHERE e1.department_id = v.department_id");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("AND e1.salary > v.avg_salary");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("ORDER BY e1.last_name;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidMerge() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ MERGE(e2) */ e1.last_name, e1.salary, v.avg_salary");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("FROM employees e1,");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("(SELECT department_id, avg(salary) avg_salary ");
    _builder.newLine();
    _builder.append("           ");
    _builder.append("FROM employees e2");
    _builder.newLine();
    _builder.append("           ");
    _builder.append("GROUP BY department_id) v ");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("WHERE e1.department_id = v.department_id");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("AND e1.salary > v.avg_salary");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("ORDER BY e1.last_name;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9603: Never reference an unknown table/alias. (e2 in MERGE hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
  }

  @Test
  public void validParallelWithoutTableReference() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ PARALLEL (DEFAULT) */ col2");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM parallel_table;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("SELECT /*+ PARALLEL (AUTO) */ last_name");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("SELECT /*+ PARALLEL (MANUAL) */ col2");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM parallel_table;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("SELECT /*+ PARALLEL (10) */ col2");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM parallel_table;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validParallelUsingTable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ FULL(employees) PARALLEL(employees, 5) */ last_name");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void validParallelUsingAlias() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ FULL(hr_emp) PARALLEL(hr_emp, DEFAULT) */ last_name");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees hr_emp;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidParallel() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("SELECT /*+ FULL(hr_emp) PARALLEL(emp, DEFAULT) */ last_name");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees hr_emp;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("SELECT /*+ FULL(hr_emp) PARALLEL(employees, DEFAULT) */ last_name");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("FROM employees hr_emp;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(2, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9603: Never reference an unknown table/alias. (emp in PARALLEL hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9602: Always use the alias name instead of the table name. Use hr_emp instead of employees in PARALLEL hint.");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
  }

  @Test
  public void validMethodInTableStats() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ table_stats(plscope.emp default rows=14) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("select /*+ table_stats(plscope.emp set rows=14) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("select /*+ table_stats(plscope.emp scale rows=14) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    _builder.newLine();
    _builder.append("select /*+ table_stats(plscope.emp sample rows=14) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidMethodInTableStats() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ table_stats(plscope.emp faster rows=14) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(1, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9604: Never use an invalid stats method. (faster in table_stats hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    Assert.assertEquals(1, (issues.get(0).getLineNumber()).intValue());
    Assert.assertEquals(36, (issues.get(0).getColumn()).intValue());
    Assert.assertEquals(42, (issues.get(0).getColumnEnd()).intValue());
  }

  @Test
  public void validKeywordsInTableStats() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ table_stats(plscope.emp default rows=14 blocks=1 row_length=10) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void invalidKeywordInTableStats() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ table_stats(plscope.emp default rec=14 blk=1 rowlen=10) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(3, issues.size());
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("G-9605: Never use an invalid stats keyword. (rec in table_stats hint).");
    Assert.assertEquals(_builder_1.toString(), issues.get(0).getMessage());
    StringConcatenation _builder_2 = new StringConcatenation();
    _builder_2.append("G-9605: Never use an invalid stats keyword. (blk in table_stats hint).");
    Assert.assertEquals(_builder_2.toString(), issues.get(1).getMessage());
    StringConcatenation _builder_3 = new StringConcatenation();
    _builder_3.append("G-9605: Never use an invalid stats keyword. (rowlen in table_stats hint).");
    Assert.assertEquals(_builder_3.toString(), issues.get(2).getMessage());
    Assert.assertEquals(44, (issues.get(0).getColumn()).intValue());
    Assert.assertEquals(47, (issues.get(0).getColumnEnd()).intValue());
    Assert.assertEquals(51, (issues.get(1).getColumn()).intValue());
    Assert.assertEquals(54, (issues.get(1).getColumnEnd()).intValue());
    Assert.assertEquals(57, (issues.get(2).getColumn()).intValue());
    Assert.assertEquals(63, (issues.get(2).getColumnEnd()).intValue());
  }

  @Test
  public void validHintWithBeginOutlineDataAndQueryBlockReferences() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select ");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("/*+");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("BEGIN_OUTLINE_DATA");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("IGNORE_OPTIM_EMBEDDED_HINTS");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("OPTIMIZER_FEATURES_ENABLE(\'12.2.0.1\')");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("DB_VERSION(\'12.2.0.1\')");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("ALL_ROWS");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("OUTLINE_LEAF(@\"SEL$58A6D7F6\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("MERGE(@\"SEL$1\" >\"SEL$2\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("OUTLINE(@\"SEL$2\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("OUTLINE(@\"SEL$1\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("FULL(@\"SEL$58A6D7F6\" \"E\"@\"SEL$1\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("INDEX(@\"SEL$58A6D7F6\" \"T\"@\"SEL$1\" (\"TASKS\".\"EMP_ID\"))");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("LEADING(@\"SEL$58A6D7F6\" \"E\"@\"SEL$1\" \"T\"@\"SEL$1\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("USE_NL(@\"SEL$58A6D7F6\" \"T\"@\"SEL$1\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("NLJ_BATCHING(@\"SEL$58A6D7F6\" \"T\"@\"SEL$1\")");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("END_OUTLINE_DATA");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("count(distinct e.ename),");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("count(distinct t.tname)");
    _builder.newLine();
    _builder.append("from   employees        e");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("join tasks t on (t.emp_id = e.id)");
    _builder.newLine();
    _builder.append("where  e.etype <= 200;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void falsePositiveForHintsReferringTableAliasInUsingClauseOfMerge() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("merge /*+ use_hash (d s) */ into bonuses d");
    _builder.newLine();
    _builder.append("using (select employee_id, salary, department_id");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("from employees");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("where department_id = 80) s");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("on (d.employee_id = s.employee_id)");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("when matched then");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("update");
    _builder.newLine();
    _builder.append("         ");
    _builder.append("set d.bonus = d.bonus + s.salary *.01");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("delete");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("where (s.salary > 8000)");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("when not matched then");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("insert (d.employee_id, d.bonus)");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("values (s.employee_id, s.salary *.01)");
    _builder.newLine();
    _builder.append("       ");
    _builder.append("where (s.salary <= 8000);");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    Assert.assertEquals(0, issues.size());
  }

  @Test
  public void usingTableNameInTableStatsOk() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ table_stats(plscope.emp set rows=14 blocks=1 row_length=10) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e;");
    _builder.newLine();
    _builder.append("select * from dbms_xplan.display_cursor(format => \'basic +hint_report\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9602"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(0, IterableExtensions.size(issues));
  }

  @Test
  public void usingTableNameInIndexStatsOk() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ index_stats(plscope.emp pk_emp scale blocks=1 rows=14)  */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp e where empno = 7788;");
    _builder.newLine();
    _builder.append("select * from dbms_xplan.display_cursor(format => \'basic +hint_report\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9602"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(0, IterableExtensions.size(issues));
  }

  @Test
  public void usingTableNameInColumnStatsOk() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("select /*+ column_stats(plscope.emp ename set length=6 distinct=14 nulls=0) */ *");
    _builder.newLine();
    _builder.append("  ");
    _builder.append("from plscope.emp where ename like \'S%\';");
    _builder.newLine();
    _builder.append("select * from dbms_xplan.display_cursor(format => \'basic +hint_report\');");
    _builder.newLine();
    final String stmt = _builder.toString();
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9602"));
    };
    final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
    Assert.assertEquals(0, IterableExtensions.size(issues));
  }
}
