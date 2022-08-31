-- G-9603: Never reference an unknown table/alias.

-- Reason
/*<p> There are various hints that reference a table or view. If the table or
view reference in the hint is neither a table name nor an alias, then the hint
is ignored by the Oracle Database.</p>*/

-- Bad
select --+ leading(emps depts)
       *
  from emp
  join dept on emp.deptno = dept.deptno;

select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
------------------------------------------------
| Id  | Operation                    | Name    |
------------------------------------------------
|   0 | SELECT STATEMENT             |         |
|   1 |  MERGE JOIN                  |         |
|   2 |   TABLE ACCESS BY INDEX ROWID| DEPT    |
|   3 |    INDEX FULL SCAN           | PK_DEPT |
|   4 |   SORT JOIN                  |         |
|   5 |    TABLE ACCESS FULL         | EMP     |
------------------------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 1 (U - Unused (1))
---------------------------------------------------------------------------
 
   1 -  SEL$58A6D7F6
         U -  leading(emps depts)
*/


-- Good
select --+ leading(emp dept)
       *
  from emp
  join dept on emp.deptno = dept.deptno;

select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
-----------------------------------
-----------------------------------
| Id  | Operation          | Name |
-----------------------------------
|   0 | SELECT STATEMENT   |      |
|   1 |  HASH JOIN         |      |
|   2 |   TABLE ACCESS FULL| EMP  |
|   3 |   TABLE ACCESS FULL| DEPT |
-----------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 1
---------------------------------------------------------------------------
 
   1 -  SEL$58A6D7F6
           -  leading(emp dept)
*/
