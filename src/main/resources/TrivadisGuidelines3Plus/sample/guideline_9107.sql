-- G-9107: Always name cursor parameters to match '^p_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_CURSOR_PARAMETER_NAME.</p>*/

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
