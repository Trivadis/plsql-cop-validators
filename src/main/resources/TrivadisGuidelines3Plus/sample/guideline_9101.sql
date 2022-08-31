-- G-9101: Always prefix global variables with 'g_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

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
