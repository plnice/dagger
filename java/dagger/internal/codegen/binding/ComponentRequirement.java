/*
 * Copyright (C) 2016 The Dagger Authors.
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

package dagger.internal.codegen.binding;

import static androidx.room.compiler.processing.XElementKt.isConstructor;
import static androidx.room.compiler.processing.compat.XConverters.toJavac;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dagger.internal.codegen.binding.SourceFiles.simpleVariableName;
import static dagger.internal.codegen.xprocessing.XElements.asConstructor;
import static dagger.internal.codegen.xprocessing.XElements.hasAnyAnnotation;
import static dagger.internal.codegen.xprocessing.XTypeElements.isNested;
import static dagger.internal.codegen.xprocessing.XTypes.isDeclared;
import static kotlin.streams.jdk8.StreamsKt.asStream;

import androidx.room.compiler.processing.XElement;
import androidx.room.compiler.processing.XMethodElement;
import androidx.room.compiler.processing.XType;
import androidx.room.compiler.processing.XTypeElement;
import com.google.auto.common.MoreTypes;
import com.google.auto.value.AutoValue;
import com.google.common.base.Equivalence;
import com.squareup.javapoet.ParameterSpec;
import dagger.internal.codegen.javapoet.TypeNames;
import dagger.spi.model.BindingKind;
import dagger.spi.model.Key;
import java.util.Optional;
import javax.lang.model.type.TypeMirror;

/** A type that a component needs an instance of. */
@AutoValue
public abstract class ComponentRequirement {
  /** The kind of the {@link ComponentRequirement}. */
  public enum Kind {
    /** A type listed in the component's {@code dependencies} attribute. */
    DEPENDENCY,

    /** A type listed in the component or subcomponent's {@code modules} attribute. */
    MODULE,

    /**
     * An object that is passed to a builder's {@link dagger.BindsInstance @BindsInstance} method.
     */
    BOUND_INSTANCE,
    ;

    public boolean isBoundInstance() {
      return equals(BOUND_INSTANCE);
    }

    public boolean isModule() {
      return equals(MODULE);
    }
  }

  private XType type;

  /** The kind of requirement. */
  public abstract Kind kind();

  /** Returns true if this is a {@link Kind#BOUND_INSTANCE} requirement. */
  // TODO(ronshapiro): consider removing this and inlining the usages
  final boolean isBoundInstance() {
    return kind().isBoundInstance();
  }

  /**
   * The type of the instance the component must have, wrapped so that requirements can be used as
   * value types.
   */
  public abstract Equivalence.Wrapper<TypeMirror> wrappedType();

  /** The type of the instance the component must have. */
  public XType type() {
    return type;
  }

  /** The element associated with the type of this requirement. */
  public XTypeElement typeElement() {
    return type.getTypeElement();
  }

  /** The action a component builder should take if it {@code null} is passed. */
  public enum NullPolicy {
    /** Make a new instance. */
    NEW,
    /** Throw an exception. */
    THROW,
    /** Allow use of null values. */
    ALLOW,
  }

  /**
   * An override for the requirement's null policy. If set, this is used as the null policy instead
   * of the default behavior in {@link #nullPolicy}.
   *
   * <p>Some implementations' null policy can be determined upon construction (e.g., for binding
   * instances), but others' require Elements which must wait until {@link #nullPolicy} is called.
   */
  abstract Optional<NullPolicy> overrideNullPolicy();

  /** The requirement's null policy. */
  public NullPolicy nullPolicy() {
    if (overrideNullPolicy().isPresent()) {
      return overrideNullPolicy().get();
    }
    switch (kind()) {
      case MODULE:
        return componentCanMakeNewInstances(typeElement())
            ? NullPolicy.NEW
            : requiresAPassedInstance() ? NullPolicy.THROW : NullPolicy.ALLOW;
      case DEPENDENCY:
      case BOUND_INSTANCE:
        return NullPolicy.THROW;
    }
    throw new AssertionError();
  }

  /**
   * Returns true if the passed {@link ComponentRequirement} requires a passed instance in order to
   * be used within a component.
   */
  public boolean requiresAPassedInstance() {
    if (!kind().isModule()) {
      // Bound instances and dependencies always require the user to provide an instance.
      return true;
    }
    return requiresModuleInstance() && !componentCanMakeNewInstances(typeElement());
  }

  /**
   * Returns {@code true} if an instance is needed for this (module) requirement.
   *
   * <p>An instance is only needed if there is a binding method on the module that is neither {@code
   * abstract} nor {@code static}; if all bindings are one of those, then there should be no
   * possible dependency on instance state in the module's bindings.
   *
   * <p>Alternatively, if the module is a Kotlin Object then the binding methods are considered
   * {@code static}, requiring no module instance.
   */
  private boolean requiresModuleInstance() {
    if (typeElement().isKotlinObject() || typeElement().isCompanionObject()) {
      return false;
    }
    return asStream(typeElement().getAllNonPrivateInstanceMethods())
        .filter(this::isBindingMethod)
        .anyMatch(method -> !method.isAbstract() && !method.isStatic());
  }

  private boolean isBindingMethod(XMethodElement method) {
    // TODO(cgdecker): At the very least, we should have utility methods to consolidate this stuff
    // in one place; listing individual annotations all over the place is brittle.
    return hasAnyAnnotation(
        method,
        TypeNames.PROVIDES,
        TypeNames.PRODUCES,
        // TODO(ronshapiro): it would be cool to have internal meta-annotations that could describe
        // these, like @AbstractBindingMethod
        TypeNames.BINDS,
        TypeNames.MULTIBINDS,
        TypeNames.BINDS_OPTIONAL_OF);
  }

  /** The key for this requirement, if one is available. */
  public abstract Optional<Key> key();

  /** Returns the name for this requirement that could be used as a variable. */
  public abstract String variableName();

  /** Returns a parameter spec for this requirement. */
  public ParameterSpec toParameterSpec() {
    return ParameterSpec.builder(type().getTypeName(), variableName()).build();
  }

  public static ComponentRequirement forDependency(XType type) {
    checkArgument(isDeclared(checkNotNull(type)));
    ComponentRequirement requirement =
        new AutoValue_ComponentRequirement(
            Kind.DEPENDENCY,
            MoreTypes.equivalence().wrap(toJavac(type)),
            Optional.empty(),
            Optional.empty(),
            simpleVariableName(type.getTypeElement().getClassName()));
    requirement.type = type;
    return requirement;
  }

  public static ComponentRequirement forModule(XType type) {
    checkArgument(isDeclared(checkNotNull(type)));
    ComponentRequirement requirement =
        new AutoValue_ComponentRequirement(
            Kind.MODULE,
            MoreTypes.equivalence().wrap(toJavac(type)),
            Optional.empty(),
            Optional.empty(),
            simpleVariableName(type.getTypeElement().getClassName()));
    requirement.type = type;
    return requirement;
  }

  static ComponentRequirement forBoundInstance(
      Key key, boolean nullable, XElement elementForVariableName) {
    ComponentRequirement requirement =
        new AutoValue_ComponentRequirement(
            Kind.BOUND_INSTANCE,
            MoreTypes.equivalence().wrap(key.type().java()),
            nullable ? Optional.of(NullPolicy.ALLOW) : Optional.empty(),
            Optional.of(key),
            toJavac(elementForVariableName).getSimpleName().toString());
    requirement.type = key.type().xprocessing();
    return requirement;
  }

  public static ComponentRequirement forBoundInstance(ContributionBinding binding) {
    checkArgument(binding.kind().equals(BindingKind.BOUND_INSTANCE));
    ComponentRequirement requirement =
        forBoundInstance(
            binding.key(),
            binding.nullableType().isPresent(),
            binding.bindingElement().get());
    requirement.type = binding.key().type().xprocessing();
    return requirement;
  }

  /**
   * Returns true if and only if a component can instantiate new instances (typically of a module)
   * rather than requiring that they be passed.
   */
  // TODO(bcorso): Should this method throw if its called knowing that an instance is not needed?
  public static boolean componentCanMakeNewInstances(XTypeElement typeElement) {
    // TODO(bcorso): Investigate how we should replace this in XProcessing. It's not clear what the
    // complete set of kinds are in XProcessing and if they're mutually exclusive. For example,
    // does XTypeElement#isClass() cover XTypeElement#isDataClass(), etc?
    switch (toJavac(typeElement).getKind()) {
      case CLASS:
        break;
      case ENUM:
      case ANNOTATION_TYPE:
      case INTERFACE:
        return false;
      default:
        throw new AssertionError("TypeElement cannot have kind: " + toJavac(typeElement).getKind());
    }

    if (typeElement.isAbstract()) {
      return false;
    }

    if (requiresEnclosingInstance(typeElement)) {
      return false;
    }

    for (XElement enclosed : typeElement.getEnclosedElements()) {
      if (isConstructor(enclosed)
          && asConstructor(enclosed).getParameters().isEmpty()
          && !asConstructor(enclosed).isPrivate()) {
        return true;
      }
    }

    // TODO(gak): still need checks for visibility

    return false;
  }

  private static boolean requiresEnclosingInstance(XTypeElement typeElement) {
    return isNested(typeElement) && !typeElement.isStatic();
  }
}
