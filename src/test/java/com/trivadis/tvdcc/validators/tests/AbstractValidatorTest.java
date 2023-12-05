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

import com.google.inject.Injector;
import com.trivadis.oracle.plsql.PLSQLStandaloneSetup;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import com.trivadis.tvdcc.validators.TrivadisPlsqlNaming;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.StringStartsWith.startsWith;

public abstract class AbstractValidatorTest {
    private static final String PROPERTIES_FILE_NAME = TrivadisPlsqlNaming.PROPERTIES_FILE_NAME;
    public static final String FULL_PROPERTIES_FILE_NAME = ((System.getProperty("user.home") + File.separator)
            + PROPERTIES_FILE_NAME);
    private static final String FULL_PROPERTIES_FILE_NAME_BACKUP = (FULL_PROPERTIES_FILE_NAME + ".backup");

    private final Injector injector = new PLSQLStandaloneSetup().createInjectorAndDoEMFRegistration();

    @BeforeClass
    public static void commonSetup() {
        stashPropertiesFile();
    }

    public static void stashPropertiesFile() {
        try {
            final Path file = Path.of(FULL_PROPERTIES_FILE_NAME);
            if (Files.exists(file)) {
                Files.copy(file, Paths.get(FULL_PROPERTIES_FILE_NAME_BACKUP));
                Files.delete(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void restorePropertiesFile() {
        try {
            final Path original = Path.of(FULL_PROPERTIES_FILE_NAME_BACKUP);
            final Path file = Path.of(FULL_PROPERTIES_FILE_NAME);
            if (Files.exists(original)) {
                Files.copy(original, file, StandardCopyOption.REPLACE_EXISTING);
                Files.delete(original);
            } else {
                if (Files.exists(file)) {
                    Files.delete(file);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void guidelineTitleStartsWithKeyword() {
        var guidelines = getValidator().getGuidelines().values().stream().filter(it -> it.getId() >= 9000).toList();
        for (final PLSQLCopGuideline g : guidelines) {
            assertThat('"' + g.getMsg() + "' does not start with keyword", g.getMsg(),
                    anyOf(startsWith("Always"), startsWith("Never"), startsWith("Avoid"), startsWith("Try")));
        }
    }

    public XtextResource parse(String stmt) {
        final XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
        resourceSet.addLoadOption(XtextResource.OPTION_ENCODING, Charset.defaultCharset().name());
        final Resource resource = resourceSet.createResource(URI.createURI("dummy:/test.plsql"));
        final ByteArrayInputStream input = new ByteArrayInputStream(stmt.getBytes());
        try {
            resource.load(input, resourceSet.getLoadOptions());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ((XtextResource) resource);
    }

    public List<Issue> getIssues(String stmt) {
        final XtextResource resource = parse(stmt);
        final EList<Resource.Diagnostic> errors = resource.getErrors();
        if (errors.size() > 0) {
            final Resource.Diagnostic firstError = errors.get(0);
            throw new RuntimeException(
                    "Syntax error: " + firstError.getMessage() + " at line " + firstError.getLine() + ".");
        }
        return resource.getResourceServiceProvider().getResourceValidator().validate(resource, CheckMode.ALL, null);
    }

    public PLSQLValidator getValidator() {
        return injector.getInstance(PLSQLValidator.class);
    }
}
