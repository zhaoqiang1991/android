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
package com.android.tools.idea.uibuilder.handlers.motion.editor.adapters;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The interface to a simplified XML tag structure.
 * This allows support for implementation and code that can work with any implementation.
 */
public interface MTag {

  @Override
  public String toString();

  public String getTagName();

  MTag getParent();

  void deleteTag();

  public static class Attribute {
    public String mNamespace;
    public String mAttribute;
    public String mValue;
  }

  ArrayList<MTag> children = new ArrayList<>();

  public ArrayList<MTag> getChildren();

  public HashMap<String, Attribute> getAttrList();

  public MTag[] getChildTags();

  public MTag[] getChildTags(String type);

  /**
   * Get children who attribute == value
   */
  public MTag[] getChildTags(String attribute, String value);

  /**
   * Get children who attribute == value
   */
  public MTag[] getChildTags(String type, String attribute, String value);


  public String getAttributeValue(String attribute);

  public void print(String space);

  public String toXmlString();

  public String toFormalXmlString(String space);

  public void printFormal(String space, PrintStream out);

  public TagWriter getChildTagWriter(String name);

  interface TagWriter extends MTag {
    void setAttribute(String type, String attribute, String value);
    MTag commit();
  }
}
