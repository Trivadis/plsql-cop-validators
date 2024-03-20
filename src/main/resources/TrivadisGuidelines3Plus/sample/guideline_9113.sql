-- G-9113: Always name exceptions to match '^e_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_EXCEPTION_NAME.</p>*/

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
