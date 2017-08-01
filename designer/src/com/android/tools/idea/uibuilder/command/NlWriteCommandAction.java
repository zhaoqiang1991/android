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
package com.android.tools.idea.uibuilder.command;

import com.android.tools.idea.templates.TemplateUtils;
import com.android.tools.idea.uibuilder.model.NlComponent;
import com.android.tools.idea.uibuilder.model.NlModel;
import com.intellij.openapi.application.BaseActionRunnable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class NlWriteCommandAction implements Runnable {
  private final NlComponent myComponent;
  private final String myName;
  private final Runnable myRunnable;

  public NlWriteCommandAction(@NotNull NlComponent component, @NotNull String name, @NotNull Runnable runnable) {
    myComponent = component;
    myName = name;
    myRunnable = runnable;
  }

  @Override
  public void run() {
    NlModel model = myComponent.getModel();
    Project project = model.getProject();

    BaseActionRunnable<Void> action = new WriteCommandAction.Simple<Void>(project, myName, model.getFile()) {
      @Override
      protected void run() throws Throwable {
        myRunnable.run();
        TemplateUtils.reformatAndRearrange(project, myComponent.getTag());
      }
    };

    action.execute();
  }
}