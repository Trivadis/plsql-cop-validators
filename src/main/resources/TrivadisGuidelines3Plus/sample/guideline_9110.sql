-- G-9110: Always prefix in/out parameters with 'io_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

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
