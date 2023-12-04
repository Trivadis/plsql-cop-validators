/**
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

import com.google.common.base.Objects;
import com.trivadis.oracle.plsql.validation.PLSQLCopGuideline;
import com.trivadis.oracle.plsql.validation.PLSQLCopValidator;
import com.trivadis.oracle.plsql.validation.PLSQLValidator;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.ComposedChecks;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@ComposedChecks(validators = { OverrideTrivadisGuidelines.class, SQLInjection.class, TrivadisPlsqlNaming.class, Hint.class })
@SuppressWarnings("all")
public class TrivadisGuidelines3Plus extends PLSQLValidator implements PLSQLCopValidator {
  private final TrivadisGuidelines3 converter = new TrivadisGuidelines3();

  private HashMap<Integer, PLSQLCopGuideline> guidelines;

  @Override
  public String getGuidelineId(final Integer id) {
    if (((id).intValue() < 1000)) {
      return String.format("G-%04d", this.converter.getNewId(id));
    } else {
      return super.getGuidelineId(id);
    }
  }

  @Override
  public String getGuidelineMsg(final Integer id) {
    if (((id).intValue() < 1000)) {
      final Integer newId = this.converter.getNewId(id);
      final PLSQLCopGuideline guideline = this.getGuidelines().get(newId);
      String _msg = null;
      if (guideline!=null) {
        _msg=guideline.getMsg();
      }
      return String.format("G-%04d: %s", newId, _msg);
    } else {
      return super.getGuidelineMsg(id);
    }
  }

  @Override
  public HashMap<Integer, PLSQLCopGuideline> getGuidelines() {
    try {
      if ((this.guidelines == null)) {
        HashMap<Integer, PLSQLCopGuideline> _hashMap = new HashMap<Integer, PLSQLCopGuideline>();
        this.guidelines = _hashMap;
        final Function1<Annotation, Boolean> _function = (Annotation it) -> {
          Class<? extends Annotation> _annotationType = it.annotationType();
          return Boolean.valueOf(Objects.equal(_annotationType, ComposedChecks.class));
        };
        final Function1<Annotation, ComposedChecks> _function_1 = (Annotation it) -> {
          return ((ComposedChecks) it);
        };
        final List<ComposedChecks> annotations = ListExtensions.<Annotation, ComposedChecks>map(IterableExtensions.<Annotation>toList(IterableExtensions.<Annotation>filter(((Iterable<Annotation>)Conversions.doWrapArray(this.getClass().getAnnotations())), _function)), _function_1);
        for (final ComposedChecks annotation : annotations) {
          Class<? extends AbstractDeclarativeValidator>[] _validators = annotation.validators();
          for (final Class<? extends AbstractDeclarativeValidator> validator : _validators) {
            {
              final AbstractDeclarativeValidator validatorInstance = validator.newInstance();
              Collection<PLSQLCopGuideline> _values = ((PLSQLCopValidator) validatorInstance).getGuidelines().values();
              for (final PLSQLCopGuideline guideline : _values) {
                Integer _id = guideline.getId();
                boolean _lessThan = ((_id).intValue() < 1000);
                if (_lessThan) {
                  this.guidelines.put(this.converter.getNewId(guideline.getId()), guideline);
                } else {
                  this.guidelines.put(guideline.getId(), guideline);
                }
              }
            }
          }
        }
      }
      return this.guidelines;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
