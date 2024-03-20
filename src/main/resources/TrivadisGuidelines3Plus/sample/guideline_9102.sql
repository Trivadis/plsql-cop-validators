-- G-9102: Always name local variables to match '^l_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_LOCAL_VARIABLE_NAME.</p>*/

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
