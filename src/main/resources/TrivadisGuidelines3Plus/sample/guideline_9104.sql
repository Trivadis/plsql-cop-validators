-- G-9104: Always name records to match '^r_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_RECORD_NAME.</p>*/

-- Bad
declare
   emp  emp%rowtype;
   type r_dept_type is record(
         deptno number,
         dname  varchar2(14 char),
         loc    loc(13 char)
      );
   dept r_dept_type;
begin
   null;
end;
/

-- Good
declare
   r_emp  emp%rowtype;
   type r_dept_type is record(
         deptno number,
         dname  varchar2(14 char),
         loc    loc(13 char)
      );
   r_dept r_dept_type;
begin
   null;
end;
/
