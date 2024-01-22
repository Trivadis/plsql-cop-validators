-- G-9114: Always name constants to match '^co_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_CONSTANT_NAME.</p>*/

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
