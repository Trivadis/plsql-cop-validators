-- G-9106: Always name objects to match '^o_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_OBJECT_NAME.</p>*/

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
