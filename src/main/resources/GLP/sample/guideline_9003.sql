-- G-9003: Always prefix parameters with 'p_'.

-- Reason
/*<p>To easily distinguish between parameters and variables.</p>*/
/*<p>Furthermore, the use of a prefix avoids naming conflicts with reserved words.</p>*/

-- Bad
create or replace procedure p (a in integer, b in varchar2) as
begin
   null;
end p;
/

-- Good
create or replace procedure p (p_1 in integer, p_2 in varchar2) as
begin
   null;
end p;
/
