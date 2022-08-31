-- G-9107: Always prefix cursor parameters with 'p_'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.2/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>*/

-- Bad
declare
   cursor c_emp(in_ename in varchar2) is
      select *
        from emp
       where ename like in_ename;
begin
   null;
end;
/

-- Good
declare
   cursor c_emp(p_ename in varchar2) is
      select *
        from emp
       where ename like p_ename;
begin
   null;
end;
/
