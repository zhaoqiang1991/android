/*
 * Copyright (C) 2017 The Android Open Source Project
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

// ATTENTION: This file has been automatically generated from Proguard.bnf. Do not edit it manually.

package com.android.tools.idea.lang.proguard.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ProguardVisitor extends PsiElementVisitor {

  public void visitComment(@NotNull ProguardComment o) {
    visitPsiElement(o);
  }

  public void visitFilenameArg(@NotNull ProguardFilenameArg o) {
    visitPsiElement(o);
  }

  public void visitFilenameFlag(@NotNull ProguardFilenameFlag o) {
    visitPsiElement(o);
  }

  public void visitJavaSection(@NotNull ProguardJavaSection o) {
    visitPsiElement(o);
  }

  public void visitMultiLineFlag(@NotNull ProguardMultiLineFlag o) {
    visitPsiElement(o);
  }

  public void visitSingleLineFlag(@NotNull ProguardSingleLineFlag o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
