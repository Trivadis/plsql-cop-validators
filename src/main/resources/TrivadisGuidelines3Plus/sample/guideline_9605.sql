-- G-9605: Never use an invalid stats keyword.

-- Reason
/*<p>The syntax for the gather_stats hints is:</p>
<pre>table_stats([&lt;schema&gt;.]&lt;table&gt; &lt;method&gt; [,] &lt;keyword&gt;=&lt;value&gt; [[,] &lt;keyword&gt;=&lt;value&gt;]...)</pre>
<p>Valid keywords are:</p>
<ul>
   <li>ROWS</li>
   <li>BLOCKS</li>
   <li>ROW_LENGTH</li>
</ul>
<p>The Oracle Database treats other keywords as a syntax error and will ignore the hint.</p>*/

-- Bad
select /*+ table_stats(emp default rec=14 blk=1 rowlen=10) */ empno, ename
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
select /*+ table_stats(emp default rows=14 blocks=1 row_length=10) */ empno, ename
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
