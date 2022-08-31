-- G-9111: Always prefix record type definitions with 'r_' and add the suffix '_type'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
declare
   type dept_typ is
      record(
         deptno number,
         dname  varchar2(14 char),
         loc    loc(13 char)
      );
begin
   null;
end;
/

-- Good
declare
   type r_dept_type is
      record(
         deptno number,
         dname  varchar2(14 char),
         loc    loc(13 char)
      );
begin
   null;
end;
/
