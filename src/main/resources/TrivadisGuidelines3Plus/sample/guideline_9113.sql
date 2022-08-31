-- G-9113: Always prefix exceptions with 'e_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
declare
   some_name exception;
begin
   null;
end;
/

-- Good
declare
   e_some_name exception;
begin
   null;
end;
/
