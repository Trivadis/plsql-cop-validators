-- G-9103: Always prefix cursors with 'c_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

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
