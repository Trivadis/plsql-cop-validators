/*
 * Copyright 2019 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
import com.trivadis.tvdcc.validators.OverrideTrivadisGuidelines;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class OverrideTrivadisGuidelinesTest extends AbstractValidatorTest {

    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(OverrideTrivadisGuidelines.class);
    }

    @Test
    public void literalInConstantDeclarationsIsOkay() {
        var stmt = """
                CREATE PACKAGE pkg AS
                   co_templ CONSTANT VARCHAR2(4000 BYTE) := 'a text' || ' more text';
                END pkg;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1050")).toList();
        Assert.assertEquals(0, issues.size());
    }

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

    @Test
    public void literalInFunctionOfLoggerCallIsOkay() {
        var stmt = """
                BEGIN
                   logger.log(upper('Hello World'));
                   logger.log(upper('Hello World'));
                END;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1050")).toList();
        Assert.assertEquals(0, issues.size());
    }

    @Test
    public void literalInPackageFunctionOfLoggerCallIsOkay() {
        var stmt = """
                BEGIN
                   logger.log(x.y('Hello World'));
                   logger.log(x.y('Hello World'));
                END;
                """;
        var issues = getIssues(stmt).stream().filter(it -> it.getCode().equals("G-1050")).toList();
        Assert.assertEquals(0, issues.size());
    }

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
}
