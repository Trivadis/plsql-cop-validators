-- G-9109: Always prefix out parameters with 'out_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

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
