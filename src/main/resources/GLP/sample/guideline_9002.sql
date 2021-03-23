-- G-9002: Always prefix local variables with 'l_'.

-- Reason
/*<p>To easily distinguish between global and local variables.</p>*/
/*<p>Furthermore, the use of a prefix avoids naming conflicts with reserved words.</p>*/

-- Bad
create or replace procedure p as
   c integer;
begin
   null;
end p;
/

-- Good
create or replace procedure p as
   l_something integer;
begin
   null;
end p;
/
