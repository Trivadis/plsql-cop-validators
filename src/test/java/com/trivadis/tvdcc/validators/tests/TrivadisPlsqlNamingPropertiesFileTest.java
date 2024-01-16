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
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TrivadisPlsqlNamingPropertiesFileTest extends AbstractValidatorTest {

    @BeforeClass
    public static void commonSetup() {
        stashPropertiesFile();
        createTestPropertiesFile();
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisPlsqlNaming.class);
    }

    // create a simple properties file to test with 
    public static void createTestPropertiesFile() {
        try {
            final File file = new File(FULL_PROPERTIES_FILE_NAME);
            final FileWriter fileWriter = new FileWriter(file, true);
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("REGEX_LOCAL_VARIABLE_NAME = ^loc_.+$");
            bufferedWriter.newLine();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // check that old prefix is now not accepted
    @Test
    public void LocalVariableNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY example AS
                   PROCEDURE a IS
                      l_some_name INTEGER;
                   BEGIN
                      NULL;
                   END a;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    // check that new prefix from file is accepted
    @Test
    public void LocalVariableOk() {
        var stmt = """
                CREATE OR REPLACE PACKAGE BODY example AS
                   PROCEDURE a IS
                      loc_some_name INTEGER;
                   BEGIN
                      NULL;
                   END a;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9102")).toList().size());
    }

    // check that defaults are used if not specified in the properties-file
    @Test
    public void GlobalVariableNok() {
        var stmt = """
                CREATE OR REPLACE PACKAGE example AS
                   some_name INTEGER;
                END example;
                /
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(1, issues.stream().filter(it -> it.getCode().equals("G-9101")).toList().size());
    }

    // check that defaults are used if not specified in the properties-file
    @Test
    public void GlobalVariableOk() {
        var stmt = """
                CREATE OR REPLACE PACKAGE example AS
                   g_some_name INTEGER;
                END example;
                """;
        var issues = getIssues(stmt);
        Assert.assertEquals(0, issues.stream().filter(it -> it.getCode().equals("G-9101")).toList().size());
    }
}
