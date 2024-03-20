/*
 * Copyright 2020 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
 * 
 * Licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0
 * Unported License (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *     https://creativecommons.org/licenses/by-nc-nd/3.0/
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.trivadis.tvdcc.validators.tests;

import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.TrivadisGuidelines3Plus;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TrivadisGuidelines3PlusTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisGuidelines3Plus.class);
    }

    @Test
    public void guidelines() {
        var guidelines = getValidator().getGuidelines();
        Assert.assertEquals(22, guidelines.values().stream().filter(it -> it.getId() >= 9100).toList().size()); // last guideline in v4.2 is G-9040
        Assert.assertEquals(125, guidelines.values().stream().filter(it -> it.getId() < 9100).toList().size()); // added G-3182, G-3183 in v4.3, added G-3330, G-4387 in v4.4
        Assert.assertEquals(79, guidelines.values().stream().filter(it -> it.getId() < 1000).toList().size());
    }

    @Test
    public void getGuidelineId_mapped_via_Trivadis2() {
        Assert.assertEquals("G-1010", getValidator().getGuidelineId(1));
    }

    @Test
    public void getGuidelineId_of_Trivadis3() {
        Assert.assertEquals("G-2130", getValidator().getGuidelineId(2130));
    }

    @Test
    public void getGuidelineMsg_mapped_via_Trivadis2() {
        Assert.assertEquals("G-1010: Try to label your sub blocks.", getValidator().getGuidelineMsg(1));
    }

    @Test
    public void getGuidelineMsg_mapped_via_Trivadis3() {
        Assert.assertEquals("G-2130: Try to use subtypes for constructs used often in your code.",
                getValidator().getGuidelineMsg(2130));
    }

    // issue avoided by OverrideTrivadisGuidelines (would throw an error via TrivadisGuidelines3)
    @Test
    public void literalInLoggerCallIsOkay() {
        var stmt = """
                BEGIN
                   logger.log('Hello World');
                END;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1050")).toList();
        Assert.assertEquals(0, issues.size());
    }

    // issue thrown by OverrideTrivadisGuidelines (check in parent)
    @Test
    public void literalInDbmsOutputCallIsNotOkay() {
        var stmt = """
                BEGIN
                   dbms_output.put_line('Hello World');
                   dbms_output.put_line('Hello World');
                END;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1050")).toList();
        Assert.assertEquals(2, issues.size());
    }

    // issue thrown by TrivadisGuidelines3
    @Test
    public void guideline2230_na() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY constants_up IS
                   co_big_increase CONSTANT NUMBER(5,0) := 1;

                   FUNCTION big_increase RETURN NUMBER DETERMINISTIC IS
                   BEGIN
                      RETURN co_big_increase;
                   END big_increase;
                END constants_up;
                /
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-2230")).toList();
        Assert.assertEquals(1, issues.size());
    }

    // issue thrown by TrivadisGuidelines2
    @Test
    public void guideline1010_10() {
        var stmt = """
                BEGIN
                   BEGIN
                      NULL;
                   END;
                END;
                /
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1010")).toList();
        Assert.assertEquals(1, issues.size());
    }

    // issue thrown by SQLInjection
    @Test
    public void executeImmediateNotAssertedVariable() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (in_table_name IN VARCHAR2) AS
                   co_templ     CONSTANT VARCHAR2(4000 BYTE) := 'DROP TABLE #in_table_name# PURGE';
                   l_table_name VARCHAR2(128 BYTE);
                   l_sql        VARCHAR2(4000 BYTE);
                BEGIN
                   l_table_name := in_table_name;
                   l_sql := replace(l_templ, '#in_table_name#', l_table_name);
                   EXECUTE IMMEDIATE l_sql;
                END p;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-9501")).toList();
        Assert.assertEquals(1, issues.size());
    }

    // issue thrown by TrivadisPlsqlNaming
    @Test
    public void globalVariableNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE example AS
                   some_name INTEGER;
                END example;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9101")).toList().size());
    }

    // issue thrown by Hint
    @Test
    public void unknownHint() {
        var stmt = """
                INSERT /*+ NOLOGGING APPEND */ INTO sales_hist SELECT * FROM sales;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9601")).toList().size());
    }
}
