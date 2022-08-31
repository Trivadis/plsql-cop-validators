-- G-9102: Always prefix local variables with 'l_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
declare
   some_name integer;
begin
   null;
end;
/

-- Good
declare
   l_some_name integer;
begin
   null;
end;
/
