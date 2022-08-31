-- G-9108: Always prefix in parameters with 'in_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

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
