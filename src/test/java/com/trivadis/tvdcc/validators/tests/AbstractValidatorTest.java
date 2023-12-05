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

import com.google.inject.Injector;
import com.trivadis.oracle.plsql.PLSQLStandaloneSetup;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.AnyOf;
import org.hamcrest.core.StringStartsWith;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

@SuppressWarnings("all")
public abstract class AbstractValidatorTest {
    private static final String PROPERTIES_FILE_NAME = TrivadisPlsqlNaming.PROPERTIES_FILE_NAME;

    public static final String FULL_PROPERTIES_FILE_NAME = ((System.getProperty("user.home") + File.separator)
            + AbstractValidatorTest.PROPERTIES_FILE_NAME);

    private static final String FULL_PROPERTIES_FILE_NAME_BACKUP = (AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME
            + ".backup");

    private Injector injector = new PLSQLStandaloneSetup().createInjectorAndDoEMFRegistration();

    @BeforeClass
    public static void commonSetup() {
        AbstractValidatorTest.stashPropertiesFile();
    }

    public static void stashPropertiesFile() {
        try {
            boolean _exists = Files.exists(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME));
            if (_exists) {
                Files.copy(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME),
                        Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME_BACKUP));
                Files.delete(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME));
            }
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    @AfterClass
    public static void restorePropertiesFile() {
        try {
            boolean _exists = Files.exists(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME_BACKUP));
            if (_exists) {
                Files.copy(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME_BACKUP),
                        Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.delete(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME_BACKUP));
            } else {
                boolean _exists_1 = Files.exists(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME));
                if (_exists_1) {
                    Files.delete(Paths.get(AbstractValidatorTest.FULL_PROPERTIES_FILE_NAME));
                }
            }
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    @Test
    public void guidelineTitleStartsWithKeyword() {
        final Function1<PLSQLCopGuideline, Boolean> _function = (PLSQLCopGuideline it) -> {
            Integer _id = it.getId();
            return Boolean.valueOf(((_id).intValue() >= 9000));
        };
        final Iterable<PLSQLCopGuideline> guidelines = IterableExtensions
                .<PLSQLCopGuideline>filter(this.getValidator().getGuidelines().values(), _function);
        for (final PLSQLCopGuideline g : guidelines) {
            String _msg = g.getMsg();
            String _plus = ("\"" + _msg);
            String _plus_1 = (_plus + "\' does not start with keyword");
            MatcherAssert.<String>assertThat(_plus_1, g.getMsg(),
                    AnyOf.<String>anyOf(StringStartsWith.startsWith("Always"), StringStartsWith.startsWith("Never"),
                            StringStartsWith.startsWith("Avoid"), StringStartsWith.startsWith("Try")));
        }
    }

    public XtextResource parse(final String stmt) {
        try {
            final XtextResourceSet resourceSet = this.injector.<XtextResourceSet>getInstance(XtextResourceSet.class);
            resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
            resourceSet.addLoadOption(XtextResource.OPTION_ENCODING, Charset.defaultCharset().name());
            final Resource resource = resourceSet.createResource(URI.createURI("dummy:/test.plsql"));
            byte[] _bytes = stmt.getBytes();
            final ByteArrayInputStream input = new ByteArrayInputStream(_bytes);
            resource.load(input, resourceSet.getLoadOptions());
            return ((XtextResource) resource);
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    public List<Issue> getIssues(final String stmt) {
        final XtextResource resource = this.parse(stmt);
        final EList<Resource.Diagnostic> errors = resource.getErrors();
        int _size = errors.size();
        boolean _greaterThan = (_size > 0);
        if (_greaterThan) {
            final Resource.Diagnostic firstError = errors.get(0);
            StringConcatenation _builder = new StringConcatenation();
            _builder.append("Syntax error: ");
            String _message = firstError.getMessage();
            _builder.append(_message);
            _builder.append(" at line ");
            int _line = firstError.getLine();
            _builder.append(_line);
            _builder.append(".");
            throw new RuntimeException(_builder.toString());
        }
        return resource.getResourceServiceProvider().getResourceValidator().validate(resource, CheckMode.ALL, null);
    }

    public PLSQLValidator getValidator() {
        return this.injector.<PLSQLValidator>getInstance(PLSQLValidator.class);
    }
}
