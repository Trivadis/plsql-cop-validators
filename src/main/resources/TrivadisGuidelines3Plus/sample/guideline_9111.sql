-- G-9111: Always name record type definitions to match '^r_.+_type$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_RECORD_TYPE_NAME.</p>*/

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
