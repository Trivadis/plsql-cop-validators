-- G-9110: Always name in/out parameters to match '^io_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_IN_OUT_PARAMETER_NAME.</p>*/

-- Bad
create or replace package p is
   procedure p2(param in out integer);
end p;
/

-- Good
create or replace package p is
   procedure p2(io_param in out integer);
end p;
/
