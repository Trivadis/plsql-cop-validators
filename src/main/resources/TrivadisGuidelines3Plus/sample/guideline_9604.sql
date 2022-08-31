-- G-9604: Never use an invalid stats method.

-- Reason
/*<p>The syntax for the gather_stats hints is:</p>
<pre>table_stats([&lt;schema&gt;.]&lt;table&gt; &lt;method&gt; [,] &lt;keyword&gt;=&lt;value&gt; [[,] &lt;keyword&gt;=&lt;value&gt;]...)</pre>
<p>Valid methods are:</p>
<ul>
   <li>DEFAULT</li>
   <li>SET</li>
   <li>SCALE</li>
   <li>SAMPLE</li>
</ul>
<p>The Oracle Database treats other methods as a syntax error and will ignore the hint.</p>*/

-- Bad
select /*+ table_stats(emp faster rows=14) */ empno, ename
  from emp e
 where deptno = 20;
select * from dbms_xplan.display_cursor(format => 'basic +hint_report');  

/*
----------------------------------
| Id  | Operation         | Name |
----------------------------------
|   0 | SELECT STATEMENT  |      |
|   1 |  TABLE ACCESS FULL| EMP  |
----------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 1 (E - Syntax error (1))
---------------------------------------------------------------------------
 
   1 -  SEL$1
         E -  table_stats
*/

-- Good
select /*+ table_stats(emp set rows=14) */ empno, ename
  from emp e
 where deptno = 20;
select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
----------------------------------
| Id  | Operation         | Name |
----------------------------------
|   0 | SELECT STATEMENT  |      |
|   1 |  TABLE ACCESS FULL| EMP  |
----------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 1
---------------------------------------------------------------------------
 
   0 -  STATEMENT
           -  table_stats(emp set rows=14)
*/
