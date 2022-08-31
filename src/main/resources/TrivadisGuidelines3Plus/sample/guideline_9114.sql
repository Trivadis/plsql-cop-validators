-- G-9114: Always prefix constants with 'co_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
declare
   maximum constant integer := 1000;
begin
   null;
end;
/

-- Good
declare
   co_maximum constant integer := 1000;
begin
   null;
end;
/
