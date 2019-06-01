# PL/SQL Cop Validators

PL/SQL Cop supports custom validators. A validator must implement the PLSQLCopValidator Java interface and has to be a direct or indirect descendant of the PLSQLJavaValidator class.

You may use these validators as is or amend/extend them to suit your needs.

## Provided Validators

This project provides the following three custom validators:

Class | Description 
----- | -----------
com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus | Checks [Naming Conventions](https://trivadis.github.io/plsql-and-sql-coding-guidelines/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql) of the Trivadis PL/SQL & SQL Coding Guidelines
com.trivadis.tvdcc.validators.GLP | Checks naming of global and local variables and parameters 
com.trivadis.tvdcc.validators.SQLInjection | Looks SQL injection vulnerabilities, e.g. unasserted parameters in dynamic SQL

### TrivadisGuidelines3Plus

This validator implements 15 guidelines to cover the chapter [2.2 Naming Conventions](https://trivadis.github.io/plsql-and-sql-coding-guidelines/2-naming-conventions/naming-conventions/#naming-conventions-for-plsql) of the Trivadis PL/SQL & SQL Coding Guidelines.

Guideline | Message
--------- | -----------
G-9001    | Global variables should start with 'g_'.
G-9002    | Local variables should start with 'l_'.
G-9003    | Cursors should start with 'c_'
G-9004    | Records should start with 'r_'.
G-9005    | Collection types (arrays/tables) should start with 't_'.
G-9006    | Objects should start with 'o_'.
G-9007    | Cursor parameters should start with 'p_'.
G-9008    | In parameters should start with 'in_'.
G-9009    | Out parameters should start with 'out_'.
G-9010    | In/out parameters should start with 'io_'.
G-9011    | Record Type definitions should start with 'r_' and end with '_type'.
G-9012    | Collection Type definitions (arrays/tables) should start with 't_' and end with '_type'.
G-9013    | Exceptions should start with 'e_'.
G-9014    | Constants should start with 'co_'.
G-9015    | Subtypes should end with 'type'.

This validator is an [extension](https://github.com/Trivadis/cop-validators/blob/master/src/main/java/com/trivadis/tvdcc/validators/TrivadisGuidelines3Plus.xtend#L38) to the Trivadis PL/SQL & SQL Coding Guidelines. This means that the all guidelines defined in chapter [4. Language Usage are checked](https://trivadis.github.io/plsql-and-sql-coding-guidelines/4-language-usage/1-general/g-1010/) as well. 

### GLP

This is a simple validator to check the following naming convention guidelines:

Guideline | Message
--------- | -----------
G-9001    | Global variables should start with 'g_'.
G-9002    | Local variables should start with 'l_'.
G-9003    | Parameters should start with 'p_'.

This validator checks just these three guidelines. It does not extend the [Trivadis PL/SQL & SQL Coding Guidelines](https://trivadis.github.io/plsql-and-sql-coding-guidelines/).

### SQLInjection

This validator implements the following guideline:

Guideline | Message
--------- | -----------
G-9501    | Parameter used in string expression of dynamic SQL. Use asserted local variable instead.

It looks for unasserted parameters used in [`EXECUTE IMMEDIATE`](https://docs.oracle.com/en/database/oracle/oracle-database/19/lnpls/EXECUTE-IMMEDIATE-statement.html#GUID-C3245A95-B85B-4280-A01F-12307B108DC8) statements and [`OPEN FOR`](https://docs.oracle.com/en/database/oracle/oracle-database/19/lnpls/OPEN-FOR-statement.html#GUID-EB7AF439-FDD3-4461-9E3F-B621E8ABFB96) statements. All parameters used in these statements must be asserted with one of the subprograms provided by [`DBMS_ASSERT`](https://docs.oracle.com/en/database/oracle/oracle-database/19/arpls/DBMS_ASSERT.html#GUID-27B4B484-7CD7-48FE-89A3-B630ADE1CB50).

#### Example (bad)

The input parameter `in_table_name` is copied to the local variable `l_table_name` and then used without an assert to build the `l_sql` variable. Hence, the execute immediate statement is considered vulnerable to SQL injection, e.g. by passing `DEPT CASCADE CONSTRAINTS`.

```sql
CREATE OR REPLACE PACKAGE BODY pkg IS
    FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
        co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
        l_table_name VARCHAR2(128 BYTE);
        l_sql        VARCHAT2(4000 BYTE);
    BEGIN
        l_table_name := in_table_name;
        l_sql := replace(l_templ, '#in_table_name#', l_table_name);
        EXECUTE IMMEDIATE l_sql;
        RETURN true;
    END f;
END pkg;
```

#### Example (good)

SQL injection is not possible, because the input parameter `in_table_name` is checked/modified with [`sys.dbms_assert.enquote_name`](https://docs.oracle.com/en/database/oracle/oracle-database/19/arpls/DBMS_ASSERT.html#GUID-19E5AEEB-BB75-4B95-98C7-53921D2A9515).

```sql
CREATE OR REPLACE PACKAGE BODY pkg IS
    FUNCTION f (in_table_name IN VARCHAR2) RETURN BOOLEAN AS
        co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
        l_table_name VARCHAR2(128 BYTE);
        l_sql        VARCHAT2(4000 BYTE);
    BEGIN
        l_table_name := sys.dbms_assert.enquote_name(in_table_name);
        l_sql := replace(l_templ, '#in_table_name#', l_table_name);
        EXECUTE IMMEDIATE l_sql;
        RETURN true;
    END f;
END pkg;
```

## Use in PL/SQL Cop

1. Download PL/SQL Cop

   Download PL/SQL Cop from [here](https://www.salvis.com/blog/plsql-cop/). 

2. Install PL/SQL Cop

   - Uncompress the distributed PL/SQL Cop archive file (e.g. tvdcc-2.2.1.zip) into a folder of your choice (hereinafter referred to as `TVDCC_HOME`). I use `/usr/local/bin/tvdcc` for `TVDCC_HOME` on my MacBook Pro.

   - For Windows platforms only: Amend the settings for JAVA_HOME in the tvdcc.cmd file to meet your environment settings. Use at least a Java 7 runtime environment (JRE) or development kit (JDK).

   - Include `TVDCC_HOME` in your PATH environment variable for handy interactive usage.

   - Optionally copy your commercial license file into the `TVDCC_HOME` directory. For simplicity name the file tvdcc.lic.

3. Download Validator

   Download `validators.jar` from [here](https://github.com/Trivadis/cop-validators/releases).

4. Install Validator

   Copy the previously downloaded `validator.jar` into the `plugin` folder of your `TVDCC_HOME` folder.

5. Run PL/SQL Cop with Custom Validator

   Open a terminal window, change to the `TVDCC_HOME` directory and run the following command to all files in `$HOME/github/utPLSQL/source` with the custom validator `com.trivadis.tvdcc.validators.SQLInjection`:

   ```
   ./tvdcc.sh path=$HOME/github/utPLSQL/source validator=com.trivadis.tvdcc.validators.SQLInjection
   ```

   The `tvdcc_report.html` file contain the results. Here's an excerpt:

   ![PL/SQL Cop Report - File Issues](./images/cop-file-issues.png)

## Use in PL/SQL Cop for SQL Developer

1. Install PL/SQL Cop 

   As explained [above](README.md#use-in-plsql-cop).

2. Download PL/SQL Cop for SQL Developer

   Download PL/SQL Cop for SQL Developer from [here](https://www.salvis.com/blog/plsql-cop-for-sql-developer/).

3. Install PL/SQL Cop for SQL Developer

   - Start SQL Developer
   - Select `Check for Updates…` in the help menu.
   - Use the `Install From Local File(s)` option to install the previously downloaded `TVDCC_for_SQLDev-*.zip` file.
   - Restart SQL Developer

4. Configure Validator

   Configure the validator in SQL Developer as shown in the following screenshot:

   ![Preferences](./images/sqldev-preferences.png)
   
5. Check Code

   Open the code to be checked in an editor and select `Check` from the context menu.

   ![Check](./images/sqldev-check.png)

   The check result is shown by default at the bottom of your SQL Developer workspace.

   ![Check](./images/sqldev-check-result.png)

## How to Build

1. Install PL/SQL Cop 

   As explained [above](README.md#use-in-plsql-cop).

2. Install Maven

   [Download](https://maven.apache.org/download.cgi) and install Apache Maven 3.6.1

3. Clone the cop-validators repository

   Clone or download this repository. 

4. Build `validators.jar`

   Open a terminal window in the cop-validators root folder and maven build by the following command

		mvn -Dtvdcc.basedir=/usr/local/bin/tvdcc clean package

	Amend the parameter `tvdcc.basedir` to match your `TVDCC_HOME` directory. This folder is used to reference PL/SQL Cop  jar files which are not available in public Maven repositories

## License

The PL/SQL Cop Validators are licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. You may obtain a copy of the License at https://creativecommons.org/licenses/by-nc-nd/3.0/.