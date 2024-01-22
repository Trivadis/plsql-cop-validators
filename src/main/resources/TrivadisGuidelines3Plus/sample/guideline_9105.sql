-- G-9105: Always name collection types (arrays/tables) to match '^t_.+$'.

-- Reason
/*<p>See <a href="https://trivadis.github.io/plsql-and-sql-coding-guidelines/v4.3/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql" target="_blank">Naming Conventions for PL/SQL</a>.</p>
<p>You can override the default via system property REGEX_ARRAY_NAME.</p>*/

-- Bad
declare
   type t_varray_type is varray(10) of string;
   array1 t_varray_type;
   type t_nested_table_type is table of string;
   array2 t_nested_table_type;
   type t_assoc_array_type is table of string index by pls_integer;
   array3 t_assoc_array_type;
begin
   null;
end;
/

-- Good
declare
   type t_varray_type is varray(10) of string;
   t_array1 t_varray_type;
   type t_nested_table_type is table of string;
   t_array2 t_nested_table_type;
   type t_assoc_array_type is table of string index by pls_integer;
   t_array3 t_assoc_array_type;
begin
   null;
end;
/
