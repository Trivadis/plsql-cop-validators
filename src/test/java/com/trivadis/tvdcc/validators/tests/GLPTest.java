/**
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

import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.GLP;
import java.util.HashMap;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class GLPTest extends AbstractValidatorTest {
    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(GLP.class);
    }

    @Test
    public void guidelines() {
        PLSQLValidator _validator = this.getValidator();
        final HashMap<Integer, PLSQLCopGuideline> guidelines = ((GLP) _validator).getGuidelines();
        final Function1<PLSQLCopGuideline, Boolean> _function = (PLSQLCopGuideline it) -> {
            Integer _id = it.getId();
            return Boolean.valueOf(((_id).intValue() >= 9000));
        };
        Assert.assertEquals(3,
                IterableExtensions.size(IterableExtensions.<PLSQLCopGuideline>filter(guidelines.values(), _function)));
    }

    @Test
    public void procedureOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (p_1 IN INTEGER, p_2 IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("l_something INTEGER;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(it.getCode().startsWith("G-9"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void procedureNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PROCEDURE p (a IN INTEGER, b IN VARCHAR2) AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("c INTEGER;");
        _builder.newLine();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("END p;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(it.getCode().startsWith("G-9"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(3, IterableExtensions.size(issues));
        final Issue issue1 = ((Issue[]) Conversions.unwrapArray(issues, Issue.class))[0];
        Assert.assertEquals(1, (issue1.getLineNumber()).intValue());
        Assert.assertEquals(32, (issue1.getColumn()).intValue());
        Assert.assertEquals(1, (issue1.getLength()).intValue());
        Assert.assertEquals("G-9003", issue1.getCode());
        Assert.assertEquals("G-9003: Always prefix parameters with \'p_\'.", issue1.getMessage());
        Assert.assertEquals("a IN INTEGER", issue1.getData()[0]);
        final Issue issue2 = ((Issue[]) Conversions.unwrapArray(issues, Issue.class))[1];
        Assert.assertEquals(1, (issue2.getLineNumber()).intValue());
        Assert.assertEquals(46, (issue2.getColumn()).intValue());
        Assert.assertEquals(1, (issue2.getLength()).intValue());
        Assert.assertEquals("G-9003", issue2.getCode());
        Assert.assertEquals("G-9003: Always prefix parameters with \'p_\'.", issue2.getMessage());
        Assert.assertEquals("b IN VARCHAR2", issue2.getData()[0]);
        final Issue issue3 = ((Issue[]) Conversions.unwrapArray(issues, Issue.class))[2];
        Assert.assertEquals(2, (issue3.getLineNumber()).intValue());
        Assert.assertEquals(4, (issue3.getColumn()).intValue());
        Assert.assertEquals(1, (issue3.getLength()).intValue());
        Assert.assertEquals("G-9002", issue3.getCode());
        Assert.assertEquals("G-9002: Always prefix local variables with \'l_\'.", issue3.getMessage());
        Assert.assertEquals("c INTEGER;", issue3.getData()[0]);
    }

    @Test
    public void packageBodyOk() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY pkg AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("g_global_variable INTEGER;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p (");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("p_parameter1 IN INTEGER,");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("p_parameter2 IN VARCHAR2");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_local_variable INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END p;");
        _builder.newLine();
        _builder.append(" ");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("FUNCTION f (");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("p_parameter1 IN INTEGER,");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("p_parameter2 IN INTEGER");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") RETURN INTEGER AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("l_local_variable INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("RETURN p_parameter1 * p_parameter2;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END f;");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(it.getCode().startsWith("G-9"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void packageBodyNok() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE OR REPLACE PACKAGE BODY pkg AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("global_variable INTEGER;");
        _builder.newLine();
        _builder.newLine();
        _builder.append("   ");
        _builder.append("PROCEDURE p (");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("parameter1 IN INTEGER,");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("parameter2 IN VARCHAR2");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("local_variable INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("NULL;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END p;");
        _builder.newLine();
        _builder.append(" ");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("FUNCTION f (");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("parameter1 IN INTEGER,");
        _builder.newLine();
        _builder.append("   \t   ");
        _builder.append("parameter2 IN INTEGER");
        _builder.newLine();
        _builder.append("   ");
        _builder.append(") RETURN INTEGER AS");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("local_variable INTEGER;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("      ");
        _builder.append("RETURN parameter1 * p_parameter2;");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("END f;");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            return Boolean.valueOf(it.getCode().startsWith("G-9"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(7, IterableExtensions.size(issues));
        final Issue issue1 = ((Issue[]) Conversions.unwrapArray(issues, Issue.class))[0];
        Assert.assertEquals(2, (issue1.getLineNumber()).intValue());
        Assert.assertEquals(4, (issue1.getColumn()).intValue());
        Assert.assertEquals(15, (issue1.getLength()).intValue());
        Assert.assertEquals("G-9001", issue1.getCode());
        Assert.assertEquals("G-9001: Always prefix global variables with \'g_\'.", issue1.getMessage());
        Assert.assertEquals("global_variable INTEGER;", issue1.getData()[0]);
    }
}
