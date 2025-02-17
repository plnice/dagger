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

package(default_visibility = ["//:src"])

android_local_test(
    name = "BindValueTest",
    srcs = ["BindValueTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt:entry_point",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:bind_value",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_local_test(
    name = "BindValueIntoMapTest",
    size = "small",
    srcs = ["BindValueIntoMapTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//third_party/java/auto:value",
        "//:dagger_with_compiler",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt:entry_point",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:bind_value",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_local_test(
    name = "BindValueIntoSetTest",
    size = "small",
    srcs = ["BindValueIntoSetTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//:dagger_with_compiler",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt:entry_point",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:bind_value",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_local_test(
    name = "BindElementsIntoSetTest",
    size = "small",
    srcs = ["BindElementsIntoSetTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//third_party/java/guava/collect",
        "//:dagger_with_compiler",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt:entry_point",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:bind_value",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_local_test(
    name = "TestRootModulesTest",
    size = "small",
    srcs = ["TestRootModulesTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//:dagger_with_compiler",
        "//third_party/java/jsr330_inject",
        "//java/dagger/hilt:install_in",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_local_test(
    name = "HiltAndroidRuleTest",
    size = "small",
    srcs = ["HiltAndroidRuleTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        ":HiltAndroidRuleTestApp",
        "//:android_local_test_exports",
        "//third_party/java/guava/collect",
        "//:dagger_with_compiler",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt/android/qualifiers",
        "//java/dagger/hilt/android/testing:hilt_android_rule",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)

android_library(
    name = "HiltAndroidRuleTestApp",
    srcs = ["HiltAndroidRuleTestApp.java"],
    deps = [
        "//java/dagger/hilt/android:hilt_android_app",
    ],
)

android_local_test(
    name = "DelayComponentReadyTest",
    srcs = ["DelayComponentReadyTest.java"],
    manifest_values = {
        "minSdkVersion": "15",
        "targetSdkVersion": "27",
    },
    deps = [
        "//:android_local_test_exports",
        "//third_party/java/jsr330_inject",
        "//third_party/java/truth",
        "//java/dagger/hilt:entry_point",
        "//java/dagger/hilt/android:android_entry_point",
        "//java/dagger/hilt/android/testing:bind_value",
        "//java/dagger/hilt/android/testing:hilt_android_test",
    ],
)
