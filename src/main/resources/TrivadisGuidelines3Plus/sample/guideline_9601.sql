-- G-9601: Never use unknown hints.

-- Reason
/*<p>Using unknown hints might invalidate all subsequent hints. 
This happens when you use for example NOLOGGING. That's expected
and not a bug. See MOS note 285285.1 or bug 8432870 for details.</p>*/

-- Bad
-- "nologging" is not a hint. It does not exist in v$sql_hint.
-- The "append" hint is ignored as a result "LOAD TABLE CONVENTIONAL" is applied.
insert --+ nologging append
  into sales_hist
select * from sales;

select * from dbms_xplan.display_cursor(format => 'basic');

/*
-----------------------------------------------
| Id  | Operation                | Name       |
-----------------------------------------------
|   0 | INSERT STATEMENT         |            |
|   1 |  LOAD TABLE CONVENTIONAL | SALES_HIST |
|   2 |   PARTITION RANGE ALL    |            |
|   3 |    TABLE ACCESS FULL     | SALES      |
-----------------------------------------------
*/


-- Good
-- "nologging" is applied on the table, however this is in most 
-- environments overridden by the force logging option on database level.
-- The "append" hint is applied, as a result "LOAD AS SELECT" is applied.
alter table sales_hist nologging;

insert --+ append
  into sales_hist
select * from sales;

select * from dbms_xplan.display_cursor(format => 'basic');

/*
-------------------------------------------------------
| Id  | Operation                        | Name       |
-------------------------------------------------------
|   0 | INSERT STATEMENT                 |            |
|   1 |  LOAD AS SELECT                  | SALES_HIST |
|   2 |   OPTIMIZER STATISTICS GATHERING |            |
|   3 |    PARTITION RANGE ALL           |            |
|   4 |     TABLE ACCESS FULL            | SALES      |
-------------------------------------------------------
*/