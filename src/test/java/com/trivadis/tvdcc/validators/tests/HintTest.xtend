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
package com.trivadis.tvdcc.validators.tests

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences
import com.trivadis.tvdcc.validators.Hint
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class HintTest extends AbstractValidatorTest {

	@BeforeClass
	static def setupValidator() {
		PLSQLValidatorPreferences.INSTANCE.validatorClass = Hint
	}
	
	// --- common ---
	
	@Test
	def void registeredChecks() {
		val guidelines = (getValidator() as Hint).guidelines
		Assert.assertEquals(4, guidelines.values.size)
		Assert.assertNotNull(guidelines.get(9600))
		Assert.assertNotNull(guidelines.get(9601))
		Assert.assertNotNull(guidelines.get(9602))
		Assert.assertNotNull(guidelines.get(9603))
	}

	// --- G-9600 ---
	
	@Test
	def void multipleHintComments() {
		val stmt = '''
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
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size);
		Assert.assertEquals(4, issues.get(0).lineNumber)
		Assert.assertEquals(8, issues.get(0).column)
		Assert.assertTrue(issues.get(0).length == 12 || issues.get(0).length == 13) // includes OS specific line break 
		Assert.assertEquals('''G-9600: Never define more than one comment with hints.'''.toString, issues.get(0).message)
	}

	// --- G-9601 ---
	
	@Test
	def void commentWithInvalidHint() {
		val stmt = '''
			INSERT /* NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}
	
	@Test
	def void unknownMultiLineHint() {
		val stmt = '''
			INSERT /*+ NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size);
		Assert.assertEquals(1, issues.get(0).lineNumber)
		Assert.assertEquals(12, issues.get(0).column)
		Assert.assertEquals(9, issues.get(0).length)
		Assert.assertEquals('''G-9601: Never use unknown hints. "NOLOGGING" is unknown.'''.toString, issues.get(0).message)
	}
	
	@Test
	def void unknownSingleLineHint() {
		val stmt = '''
			INSERT --+ APPEND NOLOGGING
			  INTO sales_hist 
			SELECT * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size);
		Assert.assertEquals(1, issues.get(0).lineNumber)
		Assert.assertEquals(19, issues.get(0).column)
		Assert.assertEquals(9, issues.get(0).length)
		Assert.assertEquals('''G-9601: Never use unknown hints. "NOLOGGING" is unknown.'''.toString, issues.get(0).message)
	}

	@Test
	def void validHint() {
		val stmt = '''
			INSERT /*+ APPEND */ INTO sales_hist SELECT * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void unknownHintInHintSequenceWithNestedParenthesis() {
		val stmt = '''
			INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) NOLOGGING */ 
			  INTO sales_hist 
			SELECT /*+ fulL(sales) */ * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size);
		Assert.assertEquals("G-9601", issues.get(0).code)
		Assert.assertEquals('''G-9601: Never use unknown hints. "NOLOGGING" is unknown.'''.toString, issues.get(0).message)
	}

	@Test
	def void validHintInHintSequenceWithNestedParenthesis() {
		val stmt = '''
			INSERT /*+ APPEND IGNORE_ROW_ON_DUPKEY_INDEX(sales_hist (a_colum_name)) */ 
			  INTO sales_hist 
			SELECT /*+ fulL(sales) */ * FROM sales;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}
	
	// --- G-9602, G-9603 for leading ---
	
	@Test
	def void validLeadingUsingTable() {
		val stmt = '''
			SELECT /*+ leading(emp dept) */ * 
			  FROM emp
			  JOIN dept on dept.deptno = emp.deptno;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}
	
	@Test
	def void validLeadingUsingAlias() {
		val stmt = '''
			SELECT /*+ leading(e d) */ * 
			  FROM emp e
			  JOIN dept d on d.deptno = e.deptno;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}
	
	@Test
	def void invalidLeading() {
		val stmt = '''
			SELECT /*+ leading(emp dep) */ * 
			  FROM emp e
			  JOIN dept d on d.deptno = e.deptno;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size);
		Assert.assertEquals(1, issues.get(0).lineNumber)
		Assert.assertEquals(20, issues.get(0).column)
		Assert.assertEquals(3, issues.get(0).length)
		Assert.assertEquals('''G-9602: Always use the alias name instead of the table name. Use e instead of emp in leading hint.'''.toString, issues.get(0).message)
		Assert.assertEquals(1, issues.get(1).lineNumber)
		Assert.assertEquals(24, issues.get(1).column)
		Assert.assertEquals(3, issues.get(1).length)
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (dep in leading hint).'''.toString, issues.get(1).message)
	}

	@Test
	def void validLeadingWithSubquery() {
		// the table name and alias in the subquery must not be evaluated!
		val stmt = '''
			SELECT /*+ leading(e d) */ * 
			  FROM emp e
			  JOIN (SELECT * FROM dept d2) d on d.deptno = e.deptno;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	// --- G-9602, G-9603 for ignore_row_on_dupkey_index ---
	
	@Test
	def void validIgnoreRowOnDupkeyIndexUsingTable() {
		val stmt = '''
			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept,pk_dept) */
			  INTO dept VALUES (10, 'dname', 'loc');

			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */
			  INTO dept VALUES (10, 'dname', 'loc');
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void validIgnoreRowOnDupkeyIndexUsingAlias() {
		val stmt = '''
			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d,pk_dept) */
			  INTO dept d VALUES (10, 'dname', 'loc');

			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(d(deptno)) */
			  INTO dept d VALUES (10, 'dname', 'loc');
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void invalidIgnoreRowOnDupkeyIndex() {
		val stmt = '''
			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dep,pk_dept) */
			  INTO dept d VALUES (10, 'dname', 'loc');

			INSERT /*+ IGNORE_ROW_ON_DUPKEY_INDEX(dept(deptno)) */
			  INTO dept d VALUES (10, 'dname', 'loc');
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size);
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (dep in IGNORE_ROW_ON_DUPKEY_INDEX hint).'''.toString, issues.get(0).message)
		Assert.assertEquals('''G-9602: Always use the alias name instead of the table name. Use d instead of dept in IGNORE_ROW_ON_DUPKEY_INDEX hint.'''.toString, issues.get(1).message)
	}

	// --- G-9602, G-9603 for dynamic_sampling ---
	
	@Test
	def void validDynamicSamplingUsingTable() {
		val stmt = '''
			SELECT /*+ DYNAMIC_SAMPLING(employees 10) */ count(*)
			  FROM employees;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void validDynamicSamplingUsingAlias() {
		val stmt = '''
			SELECT /*+ DYNAMIC_SAMPLING(e 5) */ count(*)
			  FROM employees e;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void invalidDynamicSampling() {
		val stmt = '''
			SELECT /*+ DYNAMIC_SAMPLING(emp 15) DYNAMIC_SAMPLING(employees 15) */ count(*)
			  FROM employees e;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size);
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (emp in DYNAMIC_SAMPLING hint).'''.toString, issues.get(0).message)
		Assert.assertEquals('''G-9602: Always use the alias name instead of the table name. Use e instead of employees in DYNAMIC_SAMPLING hint.'''.toString, issues.get(1).message)
	}

	// --- G-9602, G-9603 for index ---

	@Test
	def void validIndexUsingTable() {
		val stmt = '''
			SELECT /*+ INDEX ( employees emp_department_ix ) */ employee_id, department_id
			  FROM employees
			 WHERE department_id > 50;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void validIndexUsingAlias() {
		val stmt = '''
			SELECT /*+ INDEX ( e emp_department_ix ) */ employee_id, department_id
			  FROM employees e
			 WHERE department_id > 50;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void invalidIndex() {
		val stmt = '''
			SELECT /*+ INDEX ( emp emp_department_ix ) INDEX (employees emp_department_ix) */ employee_id, department_id
			  FROM employees e
			 WHERE department_id > 50;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size);
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (emp in INDEX hint).'''.toString, issues.get(0).message)
		Assert.assertEquals('''G-9602: Always use the alias name instead of the table name. Use e instead of employees in INDEX hint.'''.toString, issues.get(1).message)
	}

	// --- G-9603 for merge ---
	
	@Test
	def void validMergeUsingAlias() {
		val stmt = '''
			SELECT /*+ MERGE(v) */ e1.last_name, e1.salary, v.avg_salary
			   FROM employees e1,
			        (SELECT department_id, avg(salary) avg_salary 
			           FROM employees e2
			           GROUP BY department_id) v 
			   WHERE e1.department_id = v.department_id
			     AND e1.salary > v.avg_salary
			   ORDER BY e1.last_name;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}
	
	@Test
	def void invalidMerge() {
		val stmt = '''
			SELECT /*+ MERGE(e2) */ e1.last_name, e1.salary, v.avg_salary
			   FROM employees e1,
			        (SELECT department_id, avg(salary) avg_salary 
			           FROM employees e2
			           GROUP BY department_id) v 
			   WHERE e1.department_id = v.department_id
			     AND e1.salary > v.avg_salary
			   ORDER BY e1.last_name;
		'''
		val issues = stmt.issues
		Assert.assertEquals(1, issues.size);
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (e2 in MERGE hint).'''.toString, issues.get(0).message)
	}

	// --- G-9602, G-9603 for parallel ---
	
	@Test
	def void validParallelWithoutTableReference() {
		val stmt = '''
			SELECT /*+ PARALLEL (DEFAULT) */ col2
			  FROM parallel_table;
			
			SELECT /*+ PARALLEL (AUTO) */ last_name
			  FROM employees;
			
			SELECT /*+ PARALLEL (MANUAL) */ col2
			  FROM parallel_table;
			
			SELECT /*+ PARALLEL (10) */ col2
			  FROM parallel_table;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void validParallelUsingTable() {
		val stmt = '''
			SELECT /*+ FULL(employees) PARALLEL(employees, 5) */ last_name
			  FROM employees;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void validParallelUsingAlias() {
		val stmt = '''
			SELECT /*+ FULL(hr_emp) PARALLEL(hr_emp, DEFAULT) */ last_name
			  FROM employees hr_emp;
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size);
	}

	@Test
	def void invalidParallel() {
		val stmt = '''
			SELECT /*+ FULL(hr_emp) PARALLEL(emp, DEFAULT) */ last_name
			  FROM employees hr_emp;

			SELECT /*+ FULL(hr_emp) PARALLEL(employees, DEFAULT) */ last_name
			  FROM employees hr_emp;
		'''
		val issues = stmt.issues
		Assert.assertEquals(2, issues.size);
		Assert.assertEquals('''G-9603: Never reference an unknown table/alias. (emp in PARALLEL hint).'''.toString, issues.get(0).message)
		Assert.assertEquals('''G-9602: Always use the alias name instead of the table name. Use hr_emp instead of employees in PARALLEL hint.'''.toString, issues.get(1).message)
		
	}
	
	// --- issue 33 https://github.com/Trivadis/plsql-cop-validators/issues/33
	
	@Test
	def void validHintWithBeginOutlineDataAndQueryBlockReferences() {
		val stmt = '''
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
		'''
		val issues = stmt.issues
		Assert.assertEquals(0, issues.size)
	}

}