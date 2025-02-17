/*
 * Copyright (C) 2020 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen.validation;

import static androidx.room.compiler.processing.compat.XConverters.toJavac;

import androidx.room.compiler.processing.XType;
import com.google.auto.common.MoreTypes;
import com.google.auto.common.SuperficialValidation;
import com.google.common.base.Equivalence;
import dagger.internal.codegen.langmodel.DaggerTypes;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import javax.lang.model.type.TypeMirror;

/** Utility methods for validating the type hierarchy of a given type. */
final class TypeHierarchyValidator {
  private TypeHierarchyValidator() {}

  /**
   * Validate the type hierarchy of the given type including all super classes, interfaces, and
   * type parameters.
   *
   * @throws TypeNotPresentException if an type in the hierarchy is not valid.
   */
  public static void validateTypeHierarchy(XType type, DaggerTypes types) {
    Queue<XType> queue = new ArrayDeque<>();
    Set<Equivalence.Wrapper<TypeMirror>> queued = new HashSet<>();
    queue.add(type);
    queued.add(MoreTypes.equivalence().wrap(toJavac(type)));
    while (!queue.isEmpty()) {
      XType currType = queue.remove();
      if (!SuperficialValidation.validateType(toJavac(currType))) {
        throw new TypeNotPresentException(currType.toString(), null);
      }
      for (XType superType : currType.getSuperTypes()) {
        if (queued.add(MoreTypes.equivalence().wrap(toJavac(superType)))) {
          queue.add(superType);
        }
      }
    }
  }
}
