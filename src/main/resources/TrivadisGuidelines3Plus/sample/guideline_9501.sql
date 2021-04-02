-- G-9501: Never use parameter in string expression of dynamic SQL. Use asserted local variable instead.

-- Reason
/*<p>The use of static SQL eliminates the risk of SQL injection. However, if you write
dynamic SQL you are responsible to ensure that the SQL cannot be injected with malicious
SQL statements.</p>
<p><p>*/

/*<p>This check looks for unasserted parameters used in <code>execute immediate</code> statements
and <code>open for</code> statements. All parameters used in these statements must be asserted with
one of the subprograms provided by <code>dbms_assert</code>.</p>*/

-- Bad
-- The input parameter in_table_name is copied to the local variable l_table_name and then used
-- without an assert to build the l_sql variable. Hence, the execute immediate statement is
-- considered vulnerable to SQL injection, e.g. by passing DEPT CASCADE CONSTRAINTS.
create or replace package body pkg is
    function f (in_table_name in varchar2) return boolean as
        co_templ     constant varchar2(4000 byte) := 'DROP TABLE #in_table_name# PURGE';
        l_table_name varchar2(128 byte);
        l_sql        varchar2(4000 byte);
    begin
        l_table_name := in_table_name;
        l_sql := replace(co_templ, '#in_table_name#', l_table_name);
        execute immediate l_sql;
        return true;
    end f;
end pkg;
/

-- Good
-- SQL injection is not possible, because the input parameter in_table_name is 
-- checked/modified with sys.dbms_assert.enquote_name.
create or replace package body pkg is
    function f (in_table_name in varchar2) return boolean as
        co_templ     constant varchar2(4000 byte) := 'DROP TABLE #in_table_name# PURGE';
        l_table_name varchar2(128 byte);
        l_sql        varchar2(4000 byte);
    begin
        l_table_name := sys.dbms_assert.enquote_name(in_table_name);
        l_sql := replace(co_templ, '#in_table_name#', l_table_name);
        execute immediate l_sql;
        return true;
    end f;
end pkg;
/
