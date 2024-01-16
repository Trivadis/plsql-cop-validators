-- G-9103: Always name cursors to match '^c_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_CURSOR_NAME.</p>*/

-- Bad
declare
   l_dept sys_refcursor;
begin
   null;
end;
/

-- Good
declare
   c_dept sys_refcursor;
begin
   null;
end;
/
