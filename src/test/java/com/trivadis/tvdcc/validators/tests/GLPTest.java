/*
 * Copyright 2017 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
import com.trivadis.tvdcc.validators.GLP;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class GLPTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(GLP.class);
    }

    @Test
    public void guidelines() {
        var guidelines = getValidator().getGuidelines();
        Assert.assertEquals(3, guidelines.values().stream().filter(it -> it.getId() >= 9000).count());
    }

    @Test
    public void procedureOk() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (p_1 IN INTEGER, p_2 IN VARCHAR2) AS
                   l_something INTEGER;
                BEGIN
                   NULL;
                END p;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().startsWith("G-9")).toList();
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void procedureNok() {
        var stmt = """
                CREATE OR REPLACE PROCEDURE p (a IN INTEGER, b IN VARCHAR2) AS
                   c INTEGER;
                BEGIN
                   NULL;
                END p;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().startsWith("G-9")).toList();
        Assert.assertEquals(3, issues.size());
        // a
        var issue1 = issues.get(0);
        Assert.assertEquals(1, issue1.getLineNumber().intValue());
        Assert.assertEquals(32, issue1.getColumn().intValue());
        Assert.assertEquals(1, issue1.getLength().intValue());
        Assert.assertEquals("G-9003", issue1.getCode());
        Assert.assertEquals("G-9003: Always prefix parameters with 'p_'.", issue1.getMessage());
        Assert.assertEquals("a IN INTEGER", Arrays.stream(issue1.getData()).toList().get(0));
        // b
        var issue2 = issues.get(1);
        Assert.assertEquals(1, issue2.getLineNumber().intValue());
        Assert.assertEquals(46, issue2.getColumn().intValue());
        Assert.assertEquals(1, issue2.getLength().intValue());
        Assert.assertEquals("G-9003", issue2.getCode());
        Assert.assertEquals("G-9003: Always prefix parameters with 'p_'.", issue2.getMessage());
        Assert.assertEquals("b IN VARCHAR2", Arrays.stream(issue2.getData()).toList().get(0));
        // c
        var issue3 = issues.get(2);
        Assert.assertEquals(2, issue3.getLineNumber().intValue());
        Assert.assertEquals(4, issue3.getColumn().intValue());
        Assert.assertEquals(1, issue3.getLength().intValue());
        Assert.assertEquals("G-9002", issue3.getCode());
        Assert.assertEquals("G-9002: Always prefix local variables with 'l_'.", issue3.getMessage());
        Assert.assertEquals("c INTEGER;", Arrays.stream(issue3.getData()).toList().get(0));
    }

    @Test
    public void packageBodyOk() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY pkg AS
                   g_global_variable INTEGER;

                   PROCEDURE p (
                       p_parameter1 IN INTEGER,
                       p_parameter2 IN VARCHAR2
                   ) AS
                      l_local_variable INTEGER;
                   BEGIN
                      NULL;
                   END p;

                   FUNCTION f (
                       p_parameter1 IN INTEGER,
                       p_parameter2 IN INTEGER
                   ) RETURN INTEGER AS
                      l_local_variable INTEGER;
                   BEGIN
                      RETURN p_parameter1 * p_parameter2;
                   END f;
                END pkg;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().startsWith("G-9")).toList();
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void packageBodyNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY pkg AS
                   global_variable INTEGER;

                   PROCEDURE p (
                       parameter1 IN INTEGER,
                       parameter2 IN VARCHAR2
                   ) AS
                      local_variable INTEGER;
                   BEGIN
                      NULL;
                   END p;

                   FUNCTION f (
                       parameter1 IN INTEGER,
                       parameter2 IN INTEGER
                   ) RETURN INTEGER AS
                      local_variable INTEGER;
                   BEGIN
                      RETURN parameter1 * p_parameter2;
                   END f;
                END pkg;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().startsWith("G-9")).toList();
        Assert.assertEquals(7, issues.size());
        // global_variable
        var issue1 = issues.get(0);
        Assert.assertEquals(2, issue1.getLineNumber().intValue());
        Assert.assertEquals(4, issue1.getColumn().intValue());
        Assert.assertEquals(15, issue1.getLength().intValue());
        Assert.assertEquals("G-9001", issue1.getCode());
        Assert.assertEquals("G-9001: Always prefix global variables with 'g_'.", issue1.getMessage());
        Assert.assertEquals("global_variable INTEGER;", Arrays.stream(issue1.getData()).toList().get(0));
    }
}
