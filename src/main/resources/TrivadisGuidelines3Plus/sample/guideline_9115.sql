-- G-9115: Always name subtypes to match '^.+_type$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_SUBTYPE_NAME.</p>*/

-- Bad
declare
   subtype short_text is varchar2(100 char);
begin
   null;
end;
/

-- Good
declare
   subtype short_text_type is varchar2(100 char);
begin
   null;
end;
/