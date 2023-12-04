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
package com.trivadis.tvdcc.validators;

import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.ComposedChecks;

@ComposedChecks(validators = { OverrideTrivadisGuidelines.class, SQLInjection.class, TrivadisPlsqlNaming.class,
        Hint.class })
public class TrivadisGuidelines3Plus extends PLSQLValidator implements PLSQLCopValidator {
    private final TrivadisGuidelines3 converter = new TrivadisGuidelines3();
    private HashMap<Integer, PLSQLCopGuideline> guidelines;

    // TODO: remove when TrivadisGuidelines2 uses new IDs
    @Override
    public String getGuidelineId(Integer id) {
        if (id < 1000) {
            return String.format("G-%04d", converter.getNewId(id));
        } else {
            return super.getGuidelineId(id);
        }
    }

    // TODO: remove when TrivadisGuidelines2 uses new IDs
    @Override
    public String getGuidelineMsg(Integer id) {
        if (id < 1000) {
            final Integer newId = converter.getNewId(id);
            final PLSQLCopGuideline guideline = getGuidelines().get(newId);
            String msg = null;
            if (guideline != null) {
                msg = guideline.getMsg();
            }
            return String.format("G-%04d: %s", newId, msg);
        } else {
            return super.getGuidelineMsg(id);
        }
    }

    @Override
    public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
        if (guidelines == null) {
            guidelines = new HashMap<>();
            final List<ComposedChecks> annotations = Arrays.stream(getClass().getAnnotations())
                    .filter(it -> it.annotationType() == ComposedChecks.class).collect(Collectors.toList()).stream()
                    .map(it -> (ComposedChecks) it).collect(Collectors.toList());
            for (final ComposedChecks annotation : annotations) {
                Class<? extends AbstractDeclarativeValidator>[] _validators = annotation.validators();
                for (final Class<? extends AbstractDeclarativeValidator> validator : _validators) {
                    final AbstractDeclarativeValidator validatorInstance;
                    try {
                        validatorInstance = validator.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                            | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    for (final PLSQLCopGuideline guideline : ((PLSQLCopValidator) validatorInstance).getGuidelines()
                            .values()) {
                        if (guideline.getId() < 1000) {
                            // TODO: remove when TrivadisGuidelines2 uses new IDs
                            guidelines.put(converter.getNewId(guideline.getId()), guideline);
                        } else {
                            guidelines.put(guideline.getId(), guideline);
                        }
                    }
                }
            }
        }
        return guidelines;
    }
}
