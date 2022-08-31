-- G-9600: Never define more than one comment with hints.

-- Reason
/*<p>Only the first comment containing hints is considered by the Oracle Database, 
therefore all hints violating this rule are treated as ordinary comments.</p>*/

-- Bad
select -- a comment
       /*+ full(e) */
       /* another comment */
       --+ full(d)
       e.empno,
       e.ename,
       d.dname
  from emp e
  join dept d
    on d.deptno = e.deptno
 where empno > 7900;
 
select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
------------------------------------------------
| Id  | Operation                    | Name    |
------------------------------------------------
|   0 | SELECT STATEMENT             |         |
|   1 |  NESTED LOOPS                |         |
|   2 |   NESTED LOOPS               |         |
|   3 |    TABLE ACCESS FULL         | EMP     |
|   4 |    INDEX UNIQUE SCAN         | PK_DEPT |
|   5 |   TABLE ACCESS BY INDEX ROWID| DEPT    |
------------------------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 1
---------------------------------------------------------------------------
 
   3 -  SEL$58A6D7F6 / E@SEL$1
           -  full(e)
*/

-- Better
select -- a comment
       /*+ full(e) full(d) */
       /* another comment */
       e.empno,
       e.ename,
       d.dname
  from emp e
  join dept d
    on d.deptno = e.deptno
 where empno > 7900;
 
select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
-----------------------------------
| Id  | Operation          | Name |
-----------------------------------
|   0 | SELECT STATEMENT   |      |
|   1 |  HASH JOIN         |      |
|   2 |   TABLE ACCESS FULL| EMP  |
|   3 |   TABLE ACCESS FULL| DEPT |
-----------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 2
---------------------------------------------------------------------------
 
   2 -  SEL$58A6D7F6 / E@SEL$1
           -  full(e)
 
   3 -  SEL$58A6D7F6 / D@SEL$1
           -  full(d)
*/

-- Good
-- do not mix single-line and mult-line comments
-- use hints first or hints last, do not hide them within other comments
select --+ full(e) full(d)
       -- a comment
       -- another comment
       e.empno,
       e.ename,
       d.dname
  from emp e
  join dept d
    on d.deptno = e.deptno
 where empno > 7900;

select * from dbms_xplan.display_cursor(format => 'basic +hint_report');

/*
-----------------------------------
| Id  | Operation          | Name |
-----------------------------------
|   0 | SELECT STATEMENT   |      |
|   1 |  HASH JOIN         |      |
|   2 |   TABLE ACCESS FULL| EMP  |
|   3 |   TABLE ACCESS FULL| DEPT |
-----------------------------------
 
Hint Report (identified by operation id / Query Block Name / Object Alias):
Total hints for statement: 2
---------------------------------------------------------------------------
 
   2 -  SEL$58A6D7F6 / E@SEL$1
           -  full(e)
 
   3 -  SEL$58A6D7F6 / D@SEL$1
           -  full(d)
*/