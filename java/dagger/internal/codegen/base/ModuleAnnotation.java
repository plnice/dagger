/*
 * Copyright (C) 2019 The Dagger Authors.
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

package dagger.internal.codegen.base;

import static com.google.common.base.Preconditions.checkArgument;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.xprocessing.XAnnotations.getClassName;
import static dagger.internal.codegen.xprocessing.XElements.getAnyAnnotation;

import androidx.room.compiler.processing.XAnnotation;
import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XType;
import androidx.room.compiler.processing.XTypeElement;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import dagger.internal.codegen.javapoet.TypeNames;
import java.util.Optional;

/** A {@code @Module} or {@code @ProducerModule} annotation. */
@AutoValue
public abstract class ModuleAnnotation {
  private static final ImmutableSet<ClassName> MODULE_ANNOTATIONS =
      ImmutableSet.of(TypeNames.MODULE, TypeNames.PRODUCER_MODULE);

  private XAnnotation annotation;

  /** The annotation itself. */
  public final XAnnotation annotation() {
    return annotation;
  }

  /** Returns the {@link ClassName} name of the annotation. */
  public abstract ClassName className();

  /** The simple name of the annotation. */
  public String simpleName() {
    return className().simpleName();
  }

  /**
   * The types specified in the {@code includes} attribute.
   *
   * @throws IllegalArgumentException if any of the values are error types
   */
  @Memoized
  public ImmutableList<XTypeElement> includes() {
    return annotation.getAsTypeList("includes").stream()
        .map(XType::getTypeElement)
        .collect(toImmutableList());
  }

  /**
   * The types specified in the {@code subcomponents} attribute.
   *
   * @throws IllegalArgumentException if any of the values are error types
   */
  @Memoized
  public ImmutableList<XTypeElement> subcomponents() {
    return annotation.getAsTypeList("subcomponents").stream()
        .map(XType::getTypeElement)
        .collect(toImmutableList());
  }

  /** Returns {@code true} if the argument is a {@code @Module} or {@code @ProducerModule}. */
  public static boolean isModuleAnnotation(XAnnotation annotation) {
    return MODULE_ANNOTATIONS.contains(getClassName(annotation));
  }

  /** The module annotation types. */
  public static ImmutableSet<ClassName> moduleAnnotations() {
    return MODULE_ANNOTATIONS;
  }

  /**
   * Creates an object that represents a {@code @Module} or {@code @ProducerModule}.
   *
   * @throws IllegalArgumentException if {@link #isModuleAnnotation(XAnnotation)} returns {@code
   *     false}
   */
  public static ModuleAnnotation moduleAnnotation(XAnnotation annotation) {
    checkArgument(
        isModuleAnnotation(annotation),
        "%s is not a Module or ProducerModule annotation",
        annotation);
    ModuleAnnotation moduleAnnotation = new AutoValue_ModuleAnnotation(getClassName(annotation));
    moduleAnnotation.annotation = annotation;
    return moduleAnnotation;
  }

  /**
   * Returns an object representing the {@code @Module} or {@code @ProducerModule} annotation if one
   * annotates {@code typeElement}.
   */
  public static Optional<ModuleAnnotation> moduleAnnotation(XElement element) {
    return getAnyAnnotation(element, TypeNames.MODULE, TypeNames.PRODUCER_MODULE)
        .map(ModuleAnnotation::moduleAnnotation);
  }
}
