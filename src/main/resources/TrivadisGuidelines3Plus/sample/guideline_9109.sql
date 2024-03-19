-- G-9109: Always name out parameters to match '^out_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_OUT_PARAMETER_NAME.</p>*/

-- Bad
create or replace package p is
   procedure p2(param out integer);
end p;
/

-- Good
create or replace package p is
   procedure p2(out_param out integer);
end p;
/
