/*
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

import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.Hint;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HintTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(Hint.class);
    }

    // --- common ---

    @Test
    public void registeredChecks() {
        final HashMap<Integer, PLSQLCopGuideline> guidelines = getValidator().getGuidelines();
        Assert.assertEquals(6, guidelines.values().size());
        Assert.assertNotNull(guidelines.get(9600));
        Assert.assertNotNull(guidelines.get(9601));
        Assert.assertNotNull(guidelines.get(9602));
        Assert.assertNotNull(guidelines.get(9603));
        Assert.assertNotNull(guidelines.get(9604));
        Assert.assertNotNull(guidelines.get(9605));
    }

    // --- G-9600 ---

    @Test
    public void multipleHintComments() {
        var stmt = """
                SELECT -- a comment
                       /*+ full(e) */
                       /* another comment */
                       --+ full(d)
                       e.empno,
                       e.ename,
                       d.dname
                  FROM emp e
                  JOIN dept d
                    ON d.deptno = e.deptno
                 WHERE empno > 7900;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(4, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(8, issues.get(0).getColumn().intValue());
        Assert.assertTrue(issues.get(0).getLength() == 12 || issues.get(0).getLength() == 13); // includes OS specific line break 
        Assert.assertEquals("G-9600: Never define more than one comment with hints.", issues.get(0).getMessage());
    }

    // --- G-9601 ---

    @Test
    public void commentWithInvalidHint() {
        var stmt = """
                INSERT /* NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void unknownMultiLineHint() {
        var stmt = """
                INSERT /*+ NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(1, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(12, issues.get(0).getColumn().intValue());
        Assert.assertEquals(9, issues.get(0).getLength().intValue());
        Assert.assertEquals("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.", issues.get(0).getMessage());
    }

    @Test
    public void unknownSingleLineHint() {
        var stmt = """
                INSERT --+ APPEND NOLOGGING
                  INTO sales_hist
                SELECT * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals(1, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(19, issues.get(0).getColumn().intValue());
        Assert.assertEquals(9, issues.get(0).getLength().intValue());
        Assert.assertEquals("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.", issues.get(0).getMessage());
    }

    @Test
    public void validHint() {
        var stmt = """
                INSERT /*+ APPEND */ INTO sales_hist SELECT * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void unknownHintInHintSequenceWithNestedParenthesis() {
        var stmt = """
                INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) NOLOGGING */
                  INTO sales_hist
                SELECT /*+ fulL(sales) */ * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals("G-9601", issues.get(0).getCode());
        Assert.assertEquals("G-9601: Never use unknown hints. \"NOLOGGING\" is unknown.", issues.get(0).getMessage());
    }

    @Test
    public void validHintInHintSequenceWithNestedParenthesis() {
        var stmt = """
                INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) */
                  INTO sales_hist
                SELECT /*+ fulL(sales) */ * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    // --- G-9602, G-9603 for leading ---

    @Test
    public void validLeadingUsingTable() {
        var stmt = """
                SELECT /*+ leading(emp dept) */ *
                  FROM emp
                  JOIN dept on dept.deptno = emp.deptno;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validLeadingUsingAlias() {
        var stmt = """
                SELECT /*+ leading(e d) */ *
                  FROM emp e
                  JOIN dept d on d.deptno = e.deptno;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidLeading() {
        var stmt = """
                SELECT /*+ leading(emp dep) */ *
                  FROM emp e
                  JOIN dept d on d.deptno = e.deptno;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals(1, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(20, issues.get(0).getColumn().intValue());
        Assert.assertEquals(3, issues.get(0).getLength().intValue());
        Assert.assertEquals(
                "G-9602: Always use the alias name instead of the table name. Use e instead of emp in leading hint.",
                issues.get(0).getMessage());
        Assert.assertEquals(1, issues.get(1).getLineNumber().intValue());
        Assert.assertEquals(24, issues.get(1).getColumn().intValue());
        Assert.assertEquals(3, issues.get(1).getLength().intValue());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (dep in leading hint).",
                issues.get(1).getMessage());
    }

    @Test
    public void validLeadingWithSubquery() {
        // the table name and alias in the subquery must not be evaluated!
        var stmt = """
                    SELECT /*+ leading(e d) */ *
                      FROM emp e
                      JOIN (SELECT * FROM dept d2) d on d.deptno = e.deptno;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    // --- G-9602, G-9603 for ignore_row_on_dupkey_index ---

    @Test
    public void validIgnoreRowOnDupkeyIndexUsingTable() {
        var stmt = """
                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept,pk_dept) */
                  INTO dept VALUES (10, 'dname', 'loc');

                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */
                  INTO dept VALUES (10, 'dname', 'loc');
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validIgnoreRowOnDupkeyIndexUsingAlias() {
        var stmt = """
                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d,pk_dept) */
                  INTO dept d VALUES (10, 'dname', 'loc');

                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d(deptno)) */
                  INTO dept d VALUES (10, 'dname', 'loc');
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidIgnoreRowOnDupkeyIndex() {
        var stmt = """
                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dep,pk_dept) */
                  INTO dept d VALUES (10, 'dname', 'loc');

                INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */
                  INTO dept d VALUES (10, 'dname', 'loc');
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (dep in IGNORE_ROW_ON_DUPKEY_INDEX hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(
                "G-9602: Always use the alias name instead of the table name. Use d instead of dept in IGNORE_ROW_ON_DUPKEY_INDEX hint.",
                issues.get(1).getMessage());
    }

    // --- G-9602, G-9603 for dynamic_sampling ---

    @Test
    public void validDynamicSamplingUsingTable() {
        var stmt = """
                SELECT /*+ DYNAMIC_SAMPLING(employees 10) */ count(*)
                  FROM employees;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validDynamicSamplingUsingAlias() {
        var stmt = """
                SELECT /*+ DYNAMIC_SAMPLING(e 5) */ count(*)
                  FROM employees e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidDynamicSampling() {
        var stmt = """
                SELECT /*+ DYNAMIC_SAMPLING(emp 15) DYNAMIC_SAMPLING(employees 15) */ count(*)
                  FROM employees e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (emp in DYNAMIC_SAMPLING hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(
                "G-9602: Always use the alias name instead of the table name. Use e instead of employees in DYNAMIC_SAMPLING hint.",
                issues.get(1).getMessage());
    }

    // --- G-9602, G-9603 for index ---

    @Test
    public void validIndexUsingTable() {
        var stmt = """
                SELECT /*+ INDEX ( employees emp_department_ix ) */ employee_id, department_id
                  FROM employees
                 WHERE department_id > 50;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validIndexUsingAlias() {
        var stmt = """
                SELECT /*+ INDEX ( e emp_department_ix ) */ employee_id, department_id
                  FROM employees e
                 WHERE department_id > 50;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidIndex() {
        var stmt = """
                SELECT /*+ INDEX ( emp emp_department_ix ) INDEX (employees emp_department_ix) */ employee_id, department_id
                  FROM employees e
                 WHERE department_id > 50;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (emp in INDEX hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(
                "G-9602: Always use the alias name instead of the table name. Use e instead of employees in INDEX hint.",
                issues.get(1).getMessage());
    }

    // --- G-9603 for merge ---

    @Test
    public void validMergeUsingAlias() {
        var stmt = """
                SELECT /*+ MERGE(v) */ e1.last_name, e1.salary, v.avg_salary
                   FROM employees e1,
                        (SELECT department_id, avg(salary) avg_salary
                           FROM employees e2
                           GROUP BY department_id) v
                   WHERE e1.department_id = v.department_id
                     AND e1.salary > v.avg_salary
                   ORDER BY e1.last_name;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidMerge() {
        var stmt = """
                SELECT /*+ MERGE(e2) */ e1.last_name, e1.salary, v.avg_salary
                   FROM employees e1,
                        (SELECT department_id, avg(salary) avg_salary
                           FROM employees e2
                           GROUP BY department_id) v
                   WHERE e1.department_id = v.department_id
                     AND e1.salary > v.avg_salary
                   ORDER BY e1.last_name;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (e2 in MERGE hint).",
                issues.get(0).getMessage());
    }

    // --- G-9602, G-9603 for parallel ---

    @Test
    public void validParallelWithoutTableReference() {
        var stmt = """
                SELECT /*+ PARALLEL (DEFAULT) */ col2
                  FROM parallel_table;

                SELECT /*+ PARALLEL (AUTO) */ last_name
                  FROM employees;

                SELECT /*+ PARALLEL (MANUAL) */ col2
                  FROM parallel_table;

                SELECT /*+ PARALLEL (10) */ col2
                  FROM parallel_table;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validParallelUsingTable() {
        var stmt = """
                SELECT /*+ FULL(employees) PARALLEL(employees, 5) */ last_name
                  FROM employees;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void validParallelUsingAlias() {
        var stmt = """
                SELECT /*+ FULL(hr_emp) PARALLEL(hr_emp, DEFAULT) */ last_name
                  FROM employees hr_emp;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidParallel() {
        var stmt = """
                SELECT /*+ FULL(hr_emp) PARALLEL(emp, DEFAULT) */ last_name
                  FROM employees hr_emp;

                SELECT /*+ FULL(hr_emp) PARALLEL(employees, DEFAULT) */ last_name
                  FROM employees hr_emp;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(2, issues.size());
        Assert.assertEquals("G-9603: Never reference an unknown table/alias. (emp in PARALLEL hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(
                "G-9602: Always use the alias name instead of the table name. Use hr_emp instead of employees in PARALLEL hint.",
                issues.get(1).getMessage());
    }

    // --- G-9604, G-9605 for table_stats ---

    @Test
    public void validMethodInTableStats() {
        var stmt = """
                select /*+ table_stats(plscope.emp default rows=14) */ *
                  from plscope.emp e;

                select /*+ table_stats(plscope.emp set rows=14) */ *
                  from plscope.emp e;

                select /*+ table_stats(plscope.emp scale rows=14) */ *
                  from plscope.emp e;

                select /*+ table_stats(plscope.emp sample rows=14) */ *
                  from plscope.emp e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void invalidMethodInTableStats() {
        var stmt = """
                select /*+ table_stats(plscope.emp faster rows=14) */ *
                  from plscope.emp e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals("G-9604: Never use an invalid stats method. (faster in table_stats hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(1, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(36, issues.get(0).getColumn().intValue());
        Assert.assertEquals(42, issues.get(0).getColumnEnd().intValue());
    }

    @Test
    public void validKeywordsInTableStats() {
        var stmt = """
                select /*+ table_stats(plscope.emp faster rows=14) */ *
                  from plscope.emp e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.size());
        Assert.assertEquals("G-9604: Never use an invalid stats method. (faster in table_stats hint).",
                issues.get(0).getMessage());
        Assert.assertEquals(1, issues.get(0).getLineNumber().intValue());
        Assert.assertEquals(36, issues.get(0).getColumn().intValue());
        Assert.assertEquals(42, issues.get(0).getColumnEnd().intValue());
    }

    @Test
    public void invalidKeywordInTableStats() {
        var stmt = """
                select /*+ table_stats(plscope.emp default rec=14 blk=1 rowlen=10) */ *
                  from plscope.emp e;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(3, issues.size());
        Assert.assertEquals("G-9605: Never use an invalid stats keyword. (rec in table_stats hint).",
                issues.get(0).getMessage());
        Assert.assertEquals("G-9605: Never use an invalid stats keyword. (blk in table_stats hint).",
                issues.get(1).getMessage());
        Assert.assertEquals("G-9605: Never use an invalid stats keyword. (rowlen in table_stats hint).",
                issues.get(2).getMessage());
        Assert.assertEquals(44, issues.get(0).getColumn().intValue());
        Assert.assertEquals(47, issues.get(0).getColumnEnd().intValue());
        Assert.assertEquals(51, issues.get(1).getColumn().intValue());
        Assert.assertEquals(54, issues.get(1).getColumnEnd().intValue());
        Assert.assertEquals(57, issues.get(2).getColumn().intValue());
        Assert.assertEquals(63, issues.get(2).getColumnEnd().intValue());
    }

    // --- issue 33 https://github.com/Trivadis/plsql-cop-validators/issues/33

    @Test
    public void validHintWithBeginOutlineDataAndQueryBlockReferences() {
        var stmt = """
                select
                  /*+
                      BEGIN_OUTLINE_DATA
                      IGNORE_OPTIM_EMBEDDED_HINTS
                      OPTIMIZER_FEATURES_ENABLE('12.2.0.1')
                      DB_VERSION('12.2.0.1')
                      ALL_ROWS
                      OUTLINE_LEAF(@"SEL$58A6D7F6")
                      MERGE(@"SEL$1" >"SEL$2")
                      OUTLINE(@"SEL$2")
                      OUTLINE(@"SEL$1")
                      FULL(@"SEL$58A6D7F6" "E"@"SEL$1")
                      INDEX(@"SEL$58A6D7F6" "T"@"SEL$1" ("TASKS"."EMP_ID"))
                      LEADING(@"SEL$58A6D7F6" "E"@"SEL$1" "T"@"SEL$1")
                      USE_NL(@"SEL$58A6D7F6" "T"@"SEL$1")
                      NLJ_BATCHING(@"SEL$58A6D7F6" "T"@"SEL$1")
                      END_OUTLINE_DATA
                  */
                       count(distinct e.ename),
                       count(distinct t.tname)
                from   employees        e
                       join tasks t on (t.emp_id = e.id)
                where  e.etype <= 200;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    // -- issue 44 https://github.com/Trivadis/plsql-cop-validators/issues/44

    @Test
    public void falsePositiveForHintsReferringTableAliasInUsingClauseOfMerge() {
        var stmt = """
                merge /*+ use_hash (d s) */ into bonuses d
                using (select employee_id, salary, department_id
                         from employees
                        where department_id = 80) s
                   on (d.employee_id = s.employee_id)
                 when matched then
                      update
                         set d.bonus = d.bonus + s.salary *.01
                      delete
                       where (s.salary > 8000)
                 when not matched then
                      insert (d.employee_id, d.bonus)
                      values (s.employee_id, s.salary *.01)
                       where (s.salary <= 8000);
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    // -- issue 46 https://github.com/Trivadis/plsql-cop-validators/issues/46

    @Test
    public void usingTableNameInTableStatsOk() {
        /*
         * Syntax according
         * http://orasql.org/2019/04/16/correct-syntax-for-the-table_stats-hint/
         * 
         * table_stats( [<schema>.]<table> [,] {default | set | scale | sample} [,]
         * <keyword>=<value> [[,] <keyword>=<value>]... )
         */
        var stmt = """
                select /*+ table_stats(plscope.emp set rows=14 blocks=1 row_length=10) */ *
                  from plscope.emp e;
                select * from dbms_xplan.display_cursor(format => 'basic +hint_report');
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-9602")).toList();
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void usingTableNameInIndexStatsOk() {
        /*
         * Assumed syntax:
         * 
         * index_stats( [<schema>.]<table> [,] <index_name> [,] {default | set | scale |
         * sample} [,] <keyword>=<value> [[,] <keyword>=<value>]...)
         */
        var stmt = """
                select /*+ index_stats(plscope.emp pk_emp scale blocks=1 rows=14)  */ *
                  from plscope.emp e where empno = 7788;
                select * from dbms_xplan.display_cursor(format => 'basic +hint_report');
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-9602")).toList();
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void usingTableNameInColumnStatsOk() {
        /*
         * Assumed syntax:
         * 
         * column_stats( [<schema>.]<table> [,] <column_name> [,] {default | set | scale
         * | sample} [,] <keyword>=<value> [[,] <keyword>=<value>]...)
         */
        var stmt = """
                select /*+ column_stats(plscope.emp ename set length=6 distinct=14 nulls=0) */ *
                  from plscope.emp where ename like 'S%';
                select * from dbms_xplan.display_cursor(format => 'basic +hint_report');
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-9602")).toList();
        Assert.assertEquals(0, issues.size());
    }
    
    // -- issue 65 https://github.com/Trivadis/plsql-cop-validators/issues/65
    
    @Test
    public void parallelHintWithDegreeWithoutParenthesis() {
        // was reporting G-9601: Never use unknown hints. "2" is unknown.
        var stmt = """
                select /*+ parallel 2 */ count(*) 
                  from sales s
                  join customers c 
                    on c.cust_id = s.cust_id;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void parallelHintWithDegreeInParenthesisWithoutAlias() {
        // was working before fixing issue 65
        var stmt = """
                select /*+ parallel(2) */ count(*) 
                  from sales s
                  join customers c 
                    on c.cust_id = s.cust_id;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.size());
    }
}
