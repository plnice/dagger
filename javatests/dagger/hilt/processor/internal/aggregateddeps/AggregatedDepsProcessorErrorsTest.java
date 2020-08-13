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

package dagger.hilt.processor.internal.aggregateddeps;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.common.base.Joiner;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import dagger.hilt.processor.internal.GeneratedImport;
import dagger.testing.compile.CompilerTests;
import javax.tools.JavaFileObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for errors generated by {@link AggregatedDepsProcessor}
 */
@RunWith(JUnit4.class)
public class AggregatedDepsProcessorErrorsTest {
  private static final Joiner LINES = Joiner.on("\n");

  @Test
  public void reportMultipleAnnotationTypeKindErrors() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "foo.bar.AnnotationsOnWrongTypeKind",
            LINES.join(
                "package foo.bar;",
                "",
                "import dagger.hilt.EntryPoint;",
                "import dagger.hilt.InstallIn;",
                "import dagger.Module;",
                "import dagger.hilt.android.components.ApplicationComponent;",
                "import dagger.hilt.internal.ComponentEntryPoint;",
                "import dagger.hilt.internal.GeneratedEntryPoint;",
                "",
                "@InstallIn(ApplicationComponent.class)",
                "@Module",
                "enum FooModule { VALUE }",
                "",
                "@InstallIn(ApplicationComponent.class)",
                "@EntryPoint",
                "final class BarEntryPoint {}",
                "",
                "@InstallIn(ApplicationComponent.class)",
                "@ComponentEntryPoint",
                "final class BazComponentEntryPoint {}",
                "",
                "@EntryPoint",
                "interface QuxEntryPoint {}",
                "",
                "@EntryPoint",
                "@Module",
                "interface DontMix{}",
                ""));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining("Only classes and interfaces can be annotated with @Module")
        .inFile(source)
        .onLine(12);
    assertThat(compilation)
        .hadErrorContaining("Only interfaces can be annotated with @EntryPoint")
        .inFile(source)
        .onLine(16);
    assertThat(compilation)
        .hadErrorContaining("Only interfaces can be annotated with @ComponentEntryPoint")
        .inFile(source)
        .onLine(20);
    assertThat(compilation)
        .hadErrorContaining(
            "@EntryPoint foo.bar.QuxEntryPoint must also be annotated with @InstallIn")
        .inFile(source)
        .onLine(23);
    assertThat(compilation)
        .hadErrorContaining("@Module and @EntryPoint cannot be used on the same interface")
        .inFile(source)
        .onLine(27);
  }

  @Test
  public void testInvalidComponentInInstallInAnnotation() {
    JavaFileObject module = JavaFileObjects.forSourceLines(
        "test.FooModule",
        "package test;",
        "",
        "import dagger.Module;",
        "import dagger.hilt.InstallIn;",
        "import dagger.hilt.android.qualifiers.ApplicationContext;",
        "",
        "@InstallIn(ApplicationContext.class)", // Error: Not a Hilt component
        "@Module",
        "final class FooModule {}");

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(module);

    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining(
            "@InstallIn, can only be used with @DefineComponent-annotated classes, but found: "
                + "[dagger.hilt.android.qualifiers.ApplicationContext]")
        .inFile(module)
        .onLine(9);
  }

  @Test
  public void testMissingInstallInAnnotation() {
    JavaFileObject source = JavaFileObjects.forSourceString(
        "foo.bar.AnnotationsOnWrongTypeKind",
        LINES.join(
            "package foo.bar;",
            "",
            "import dagger.Module;",
            "",
            "@Module",     // Error: Doesn't have InstallIn annotation
            "final class FooModule {}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation)
        .hadErrorContaining("foo.bar.FooModule is missing an @InstallIn annotation")
        .inFile(source)
        .onLine(6);
  }

  @Test
  public void testNoErrorOnDaggerGeneratedModules() {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "foo.bar",
            LINES.join(
                "package foo.bar;",
                "",
                GeneratedImport.IMPORT_GENERATED_ANNOTATION,
                "import dagger.Module;",
                "",
                "@Module",
                "@Generated(value = \"something\")", // Error: Isn't Dagger-generated but missing
                                                     // InstallIn
                "final class FooModule {}",
                "",
                "@Module",
                "@Generated(value = \"dagger\")", // No error because the module is dagger generated
                "final class BarModule {}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining("foo.bar.FooModule is missing an @InstallIn annotation")
        .inFile(source)
        .onLine(8);
  }

  @Test
  public void testModuleWithParams() {
    JavaFileObject source = JavaFileObjects.forSourceString("foo.bar", LINES.join(
        "package foo.bar;",
        "",
        "import dagger.Module;",
        "import dagger.hilt.InstallIn;",
        "import dagger.hilt.android.components.ApplicationComponent;",
        "",
        "@Module",
        "@InstallIn(ApplicationComponent.class)",
        "final class FooModule {",
        "  FooModule(String arg) {}",
        "}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining(
            "@InstallIn modules cannot have constructors with parameters. Found: "
                + "[FooModule(java.lang.String)]");
  }

  @Test
  public void testInnerModule() {
    JavaFileObject source = JavaFileObjects.forSourceString("foo.bar", LINES.join(
        "package foo.bar;",
        "",
        "import dagger.Module;",
        "import dagger.hilt.InstallIn;",
        "import dagger.hilt.android.components.ApplicationComponent;",
        "",
        "final class Outer {",
        "  @Module",
        "  @InstallIn(ApplicationComponent.class)",
        "  final class InnerModule {}",
        "}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining(
            "Nested @InstallIn modules must be static unless they are directly nested within a "
                + "test. Found: foo.bar.Outer.InnerModule");
  }

  @Test
  public void testInnerModuleInTest() {
    JavaFileObject source = JavaFileObjects.forSourceString("foo.bar", LINES.join(
        "package foo.bar;",
        "",
        "import dagger.Module;",
        "import dagger.hilt.InstallIn;",
        "import dagger.hilt.android.components.ApplicationComponent;",
        "import dagger.hilt.android.testing.HiltAndroidTest;",
        "",
        "@HiltAndroidTest",
        "final class Outer {",
        "  static class Nested {",
        "    @Module",
        "    @InstallIn(ApplicationComponent.class)",
        "    final class InnerModule {}",
        "  }",
        "}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).failed();
    assertThat(compilation).hadErrorCount(1);
    assertThat(compilation)
        .hadErrorContaining(
            "Nested @InstallIn modules must be static unless they are directly nested within a "
                + "test. Found: foo.bar.Outer.Nested.InnerModule");
  }

  @Test
  public void testInnerModuleInTest_succeeds() {
    JavaFileObject source = JavaFileObjects.forSourceString("foo.bar", LINES.join(
        "package foo.bar;",
        "",
        "import dagger.Module;",
        "import dagger.hilt.InstallIn;",
        "import dagger.hilt.android.components.ApplicationComponent;",
        "import dagger.hilt.android.testing.HiltAndroidTest;",
        "",
        "@HiltAndroidTest",
        "final class Outer {",
        "  @Module",
        "  @InstallIn(ApplicationComponent.class)",
        "  final class InnerModule {}",
        "}"));

    Compilation compilation =
        CompilerTests.compiler().withProcessors(new AggregatedDepsProcessor()).compile(source);

    assertThat(compilation).succeeded();
  }
}
