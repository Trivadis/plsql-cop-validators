-- G-9001 Always prefix global variables with 'g_'.

-- Reason
/*<p>To easily distinguish between global and local variables.</p>*/
/*<p>Furthermore, the use of a prefix avoids naming conflicts with reserved words.</p>*/

-- Bad
create or replace package pkg as
   global_variable integer;
end pkg;
/

-- Good
create or replace package pkg as
   g_global_variable integer;
end pkg;
/
