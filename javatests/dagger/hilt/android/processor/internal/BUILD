# Copyright (C) 2020 The Dagger Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# Description:
#   Tests for internal code for implementing Hilt processors.

load("//java/dagger/testing/compile:macros.bzl", "compiler_test")

package(default_visibility = ["//:src"])

compiler_test(
    name = "GeneratorsTest",
    srcs = ["GeneratorsTest.java"],
    compiler_deps = [
        "//java/dagger/hilt/android:hilt_android_app",
        "//java/dagger/hilt/android:android_entry_point",
        "@androidsdk//:platforms/android-30/android.jar",
        "@maven//:androidx_annotation_annotation",
    ],
    deps = [
        "//third_party/java/compile_testing",
        "//third_party/java/junit",
        "//third_party/java/truth",
        "//java/dagger/hilt/android/testing/compile",
    ],
)
