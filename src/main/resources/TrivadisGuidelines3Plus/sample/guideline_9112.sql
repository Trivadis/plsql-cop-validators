-- G-9112: Always name collection type definitions (arrays/tables) to match '^t_.+_type$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.4/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_ARRAY_TYPE_NAME.</p>*/

-- Bad
declare
   type t_varray is varray(10) of string;
   type nested_table_type is table of string;
   type x_assoc_array_y is table of string index by pls_integer;
begin
   null;
end;
/

-- Good
declare
   type t_varray_type is varray(10) of string;
   type t_nested_table_type is table of string;
   type t_assoc_array_type is table of string index by pls_integer;
begin
   null;
end;
/
