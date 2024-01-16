-- G-9101: Always name global variables to match '^g_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_GLOBAL_VARIABLE_NAME.</p>*/

-- Bad
create or replace package body example as
   some_name integer;
end example;
/

-- Good
create or replace package body example as
   g_some_name integer;
end example;
/
