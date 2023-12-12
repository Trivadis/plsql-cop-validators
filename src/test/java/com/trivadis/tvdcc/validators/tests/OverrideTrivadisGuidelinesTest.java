/**
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

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.OverrideTrivadisGuidelines;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class OverrideTrivadisGuidelinesTest extends AbstractValidatorTest {
    @BeforeClass
    public static void setupValidator() {
        PLSQLValidatorPreferences.INSTANCE.setValidatorClass(OverrideTrivadisGuidelines.class);
    }

    @Test
    public void literalInConstantDeclarationsIsOkay() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("CREATE PACKAGE pkg AS");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("co_templ CONSTANT VARCHAR2(4000 BYTE) := \'a text\' || \' more text\';");
        _builder.newLine();
        _builder.append("END pkg;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-1050"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void literalInLoggerCallIsOkay() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("logger.log(\'Hello World\');");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-1050"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void literalInFunctionOfLoggerCallIsOkay() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("logger.log(upper(\'Hello World\'));");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("logger.log(upper(\'Hello World\'));");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-1050"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void literalInPackageFunctionOfLoggerCallIsOkay() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("logger.log(x.y(\'Hello World\'));");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("logger.log(x.y(\'Hello World\'));");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-1050"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(0, IterableExtensions.size(issues));
    }

    @Test
    public void literalInDbmsOutputCallIsNotOkay() {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("BEGIN");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("dbms_output.put_line(\'Hello World\');");
        _builder.newLine();
        _builder.append("   ");
        _builder.append("dbms_output.put_line(\'Hello World\');");
        _builder.newLine();
        _builder.append("END;");
        _builder.newLine();
        final String stmt = _builder.toString();
        final Function1<Issue, Boolean> _function = (Issue it) -> {
            String _code = it.getCode();
            return Boolean.valueOf(Objects.equal(_code, "G-1050"));
        };
        final Iterable<Issue> issues = IterableExtensions.<Issue>filter(this.getIssues(stmt), _function);
        Assert.assertEquals(2, IterableExtensions.size(issues));
    }
}
