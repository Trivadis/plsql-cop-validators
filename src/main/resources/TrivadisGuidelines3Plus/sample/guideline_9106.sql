-- G-9106: Always prefix objects with 'o_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
create or replace type dept_type as object (
   deptno integer,
   dname  varchar2(14 char),
   loc    varchar2(13 char)
);
/

declare
   dept dept_type;
begin
   null;
end;
/

-- Good
create or replace type dept_type as object (
   deptno integer,
   dname  varchar2(14 char),
   loc    varchar2(13 char)
);
/

declare
   o_dept dept_type;
begin
   null;
end;
/
