/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.databinding.integration

import com.android.testutils.TestUtils
import com.android.tools.idea.testing.AndroidGradleProjectRule
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.EdtRule
import com.intellij.testFramework.RunsInEdt
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.fixtures.impl.CodeInsightTestFixtureImpl
import junit.framework.Assert.fail
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * This test class loads the giant Gradle project that is maintained in the data binding compiler
 * codebase that tests all possible edge-cases and is guaranteed to be error-free, so we run it
 * here as well and verify that we don't show any errors in the IDE.
 */
class DataBindingInspectionVerificationTest {
  private val projectRule = AndroidGradleProjectRule()

  @get:Rule
  val ruleChain = RuleChain.outerRule(projectRule).around(EdtRule())!!

  @get:Rule
  val temporaryFolder = TemporaryFolder()

  private val fixture
    get() = projectRule.fixture as JavaCodeInsightTestFixture

  private val fileRoot = "app"
  private val rootsToCheck = listOf(
    "app/src/main/res/layout",
    "app/src/main/res/layout-land",
    "app/src/java/android/databinding/testapp",
    "app/src/androidTest"
  )

  // TODO(b/122983052): These are currently failing. Get these blacklisted files down to zero!
  private val excludedFiles = setOf(
    "app/src/main/res/layout-land/multi_res_layout.xml",
    "app/src/main/res/layout/live_data.xml",
    "app/src/main/res/layout/included_layout.xml",
    "app/src/main/res/layout/two_way.xml",
    "app/src/main/res/layout/multi_res_layout.xml",
    "app/src/main/res/layout/conditional_binding.xml",
    "app/src/main/res/layout/listeners_with_dot.xml",
    "app/src/main/res/layout/leak_test.xml",
    "app/src/main/res/layout/bind_to_final.xml",
    "app/src/main/res/layout/live_data_included.xml",
    "app/src/main/res/layout/auto_context.xml",
    "app/src/main/res/layout/use_default.xml",
    "app/src/main/res/layout/bracket_test.xml",
    "app/src/main/res/layout/bind_to_final_observable.xml",
    "app/src/main/res/layout/static_access_import_on_demand_with_conflict.xml",
    "app/src/main/res/layout/find_method_test.xml",
    "app/src/main/res/layout/fragment_main.xml",
    "app/src/main/res/layout/resource_test.xml",
    "app/src/main/res/layout/observable_field_test.xml",
    "app/src/androidTest/java/android/databinding/testapp/InstanceAdapterTest.java",
    "app/src/androidTest/java/android/databinding/testapp/TwoWayBindingAdapterTest.java",
    "app/src/androidTest/java/android/databinding/testapp/ResourceTest.java",
    "app/src/androidTest/java/android/databinding/testapp/LiveDataTest.java",
    "app/src/androidTest/java/android/databinding/testapp/IncludeTagTest.java",
    "app/src/androidTest/java/android/databinding/testapp/NoVariableIncludeTest.java",
    "app/src/androidTest/java/android/databinding/testapp/CustomBindingTest.java",
    "app/src/androidTest/java/android/databinding/testapp/BasicBindingTest.java",
    "app/src/androidTest/java/androidx/databinding/DataBindingMapperTest.java"
  )

  @Before
  fun setUp() {
    // We want to use the TestApp from the databinding compiler integration-test suite, but it
    // has a really specialized build.gradle file that makes a lot of assumptions about the
    // directory it is in, so we copy over all other files instead and provide a minimally
    // useful build.gradle that allows the project to sync.
    val root = TestUtils.getWorkspaceFile("tools/data-binding/integration-tests")
    val testAppSrc = File(root, "TestApp")
    val testAppDst = File(temporaryFolder.root, "TestApp").also { it.mkdir() }
    testAppSrc.copyRecursively(testAppDst)

    val buildGradle = File(testAppDst, "build.gradle")
    buildGradle.writeText(
      """
        rootProject.ext.latestCompileSdk = 28

        buildscript {
          repositories {
            // This will be populated by AndroidGradleProjectRule
          }
          dependencies {
            // This will be overridden by AndroidGradleProjectRule
            classpath 'com.android.tools.build:gradle:1.5.0'
          }
        }

        allprojects {
          repositories {
            // This will be populated by AndroidGradleProjectRule
          }
        }
      """.trimIndent())

    fixture.testDataPath = temporaryFolder.root.absolutePath
    projectRule.load("TestApp")
    projectRule.requestSyncAndWait()

    // Need to do this or else highlighting will fail with a
    // "Access to tree elements not allowed" error
    (fixture as CodeInsightTestFixtureImpl).allowTreeAccessForAllFiles()
  }

  private data class FileEntry(val virtualFile: VirtualFile, val relativePath: String)
  private val allFileEntriesUnderRoots: Sequence<FileEntry>
    get() {
      val testAppRoot = projectRule.project.guessProjectDir()!!.path
      val rootPrefix = "${testAppRoot}/"

      return File(testAppRoot, fileRoot).walkTopDown()
        .mapNotNull { file -> VfsUtil.findFileByIoFile(file, true) }
        .filter { virtualFile -> !virtualFile.isDirectory }
        .map { virtualFile -> FileEntry(virtualFile, virtualFile.path.removePrefix(rootPrefix)) }
        .filter { entry -> rootsToCheck.any { entry.relativePath.startsWith(it) } }
    }

  @Test
  @RunsInEdt
  fun verifyNoFilesShowInspectionErrors() {
    allFileEntriesUnderRoots
      .filter { entry -> !excludedFiles.contains(entry.relativePath) }
      .forEach { entry ->
        fixture.configureFromExistingVirtualFile(entry.virtualFile)
        fixture.checkHighlighting(false, false, false)
      }
  }

  @Ignore // Run this test manually if you want to generate a new blacklist
  @Test
  @RunsInEdt
  fun printBlacklistToConsole() {
    val excludedPaths = mutableListOf<String>()
    for (entry in allFileEntriesUnderRoots) {
      try {
        fixture.configureFromExistingVirtualFile(entry.virtualFile)
        fixture.checkHighlighting(false, false, false)
      }
      catch (_: Throwable) {
        excludedPaths.add(entry.relativePath)
      }
    }

    if (excludedPaths.isNotEmpty()) {
      // Copy/paste console this output over the "excludedFiles" property above and reformat code
      println("private val excludedFiles = setOf(")
      println(excludedPaths.joinToString(",\n") { "\"$it\"" })
      println(")")
    }
    else {
      // If here, delete the excludedFiles field and close b/122983052
      println("Congrats - there are no more invalid paths!")
    }

    fail("This test should only be enabled temporarily and not run under normal conditions.")
  }
}
