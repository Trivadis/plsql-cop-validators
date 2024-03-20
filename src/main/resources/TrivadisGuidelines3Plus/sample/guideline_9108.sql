-- G-9108: Always name in parameters to match '^in_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_IN_PARAMETER_NAME.</p>*/

-- Bad
create or replace package p is
   procedure p2(param in integer);
end p;
/

-- Good
create or replace package p is
   procedure p2(in_param in integer);
end p;
/
