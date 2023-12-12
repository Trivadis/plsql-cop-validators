package com.trivadis.tvdcc.validators.tests;

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.validation.PLSQLValidatorPreferences;
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public class TrivadisPlsqlNamingPropertiesFileTest extends AbstractValidatorTest {
  @BeforeClass
  public static void commonSetup() {
    AbstractValidatorTest.stashPropertiesFile();
    TrivadisPlsqlNamingPropertiesFileTest.createTestPropertiesFile();
    PLSQLValidatorPreferences.INSTANCE.setValidatorClass(TrivadisPlsqlNaming.class);
  }

  public static void createTestPropertiesFile() {
    try {
      final File file = new File(TrivadisPlsqlNamingTest.FULL_PROPERTIES_FILE_NAME);
      final FileWriter fileWriter = new FileWriter(file, true);
      final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
      bufferedWriter.write("PREFIX_LOCAL_VARIABLE_NAME = loc_");
      bufferedWriter.newLine();
      bufferedWriter.close();
      fileWriter.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void LocalVariableNok() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE BODY example AS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE a IS");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("l_some_name INTEGER;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("NULL;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END a;");
    _builder.newLine();
    _builder.append("END example;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9102"));
    };
    Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
  }

  @Test
  public void LocalVariableOk() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE BODY example AS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("PROCEDURE a IS");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("loc_some_name INTEGER;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("BEGIN");
    _builder.newLine();
    _builder.append("      ");
    _builder.append("NULL;");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("END a;");
    _builder.newLine();
    _builder.append("END example;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9102"));
    };
    Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
  }

  @Test
  public void GlobalVariableNok() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE example AS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("some_name INTEGER;");
    _builder.newLine();
    _builder.append("END example;");
    _builder.newLine();
    _builder.append("/");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9101"));
    };
    Assert.assertEquals(1, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
  }

  @Test
  public void GlobalVariableOk() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("CREATE OR REPLACE PACKAGE example AS");
    _builder.newLine();
    _builder.append("   ");
    _builder.append("g_some_name INTEGER;");
    _builder.newLine();
    _builder.append("END example;");
    _builder.newLine();
    final String stmt = _builder.toString();
    final List<Issue> issues = this.getIssues(stmt);
    final Function1<Issue, Boolean> _function = (Issue it) -> {
      String _code = it.getCode();
      return Boolean.valueOf(Objects.equal(_code, "G-9101"));
    };
    Assert.assertEquals(0, IterableExtensions.size(IterableExtensions.<Issue>filter(issues, _function)));
  }
}
