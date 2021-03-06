/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.handlers.motion.timeline;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.ATTR_ID;
import static com.android.SdkConstants.AUTO_URI;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.AndroidNameSpace;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.ConstraintSetConstraint;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_attributeName;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customBooleanValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customColorValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customDimensionValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customFloatValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customIntegerValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.CustomAttributes_customStringValue;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyAttributesTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyAttributes_curveFit;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyAttributes_customAttribute;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyAttributes_framePosition;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyAttributes_target;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyCycleTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyCycle_target;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyCycle_waveOffset;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyCycle_wavePeriod;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyCycle_waveShape;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyPositionPath_path_percent;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyPositionPath_perpendicularPath_percent;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyPositionTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyPosition_transitionEasing;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyTypeAttribute;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyTypeCycle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyTypePosition;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.KeyTypeTimeCycle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.Key_framePosition;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionNameSpace;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionSceneConstraintSet;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionSceneKeyFrameSet;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionSceneOnClick;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionSceneOnSwipe;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.MotionSceneTransition;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnClickAttr_target;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnClickTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnSwipeAttr_dragDirection;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnSwipeAttr_touchAnchorId;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnSwipeAttr_touchAnchorSide;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.OnSwipeTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.TransitionConstraintSetEnd;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.TransitionConstraintSetStart;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.TransitionDuration;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.TransitionTitle;
import static com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString.ourStandardSet;

import com.android.SdkConstants;
import com.android.tools.idea.AndroidPsiUtils;
import com.android.tools.idea.common.model.NlComponent;
import com.android.tools.idea.common.model.NlModel;
import com.android.tools.idea.rendering.parsers.AttributeSnapshot;
import com.android.tools.idea.rendering.parsers.LayoutPullParsers;
import com.android.tools.idea.uibuilder.api.ViewHandler;
import com.android.tools.idea.uibuilder.handlers.ViewHandlerManager;
import com.android.tools.idea.uibuilder.handlers.motion.AttrName;
import com.android.tools.idea.uibuilder.handlers.motion.Debug;
import com.android.tools.idea.uibuilder.handlers.motion.MotionSceneString;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import icons.StudioIcons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The model of the Motion scene file.
 * This parses the file and provide hooks to write the file.
 * TODO split into a package with MotionSceneTransition, MotionSceneConstraintSet, MotionSceneKeyFrame etc.
 */
public class MotionSceneModel {
  public static final boolean BROKEN = true;
  public static final boolean DEBUG = false;
  ArrayList<ConstraintSet> myConstraintSets;
  ArrayList<TransitionTag> myTransition;
  TransitionTag myCurrentTransition;
  private VirtualFile myVirtualFile;
  private Project myProject;
  private NlModel myNlModel;
  private String myName;
  private String[] myTransitionNames = null;
  private List<NlComponent> myViews;

  public String getName() {
    return myName;
  }

  public Project getProject() {
    return myProject;
  }

  public TransitionTag getTransitionTag(int i) {
    return myTransition.get(i);
  }

  public OnSwipeTag getOnSwipeTag() {
    return (myCurrentTransition == null) ? null : myCurrentTransition.myOnSwipeTag;
  }

  public ArrayList<OnClickTag> getOnClickTags() {
    return (myCurrentTransition == null) ? null : myCurrentTransition.myOnClickTags;
  }

  public List<NlComponent> getViews() {
    return myViews;
  }

  private XmlFile motionSceneFile() {
    return (XmlFile)AndroidPsiUtils.getPsiFileSafely(myProject, myVirtualFile);
  }

  /**
   * Return the current Transition's start id
   *
   * @return
   */
  public String getStartId() {
    if (myCurrentTransition == null) {
      return null;
    }
    return myCurrentTransition.myConstraintSetStart;
  }

  /**
   * Get the list of Constraint Set the model has. Eventually it may return layout's that can be use as constraintSets
   *
   * @return
   */
  public String[] getKnownConstraintSetID() {
    ArrayList<String> str = new ArrayList<>();
    for (ConstraintSet set : myConstraintSets) {
      str.add(set.mId);
    }
    return str.toArray(new String[0]);
  }

  public static String stripID(String id) {
    int index = id.indexOf('/');
    if (index < 0) {
      return id;
    }
    return id.substring(index + 1);
  }

  /**
   * Returns the current Transition's end id or null.
   *
   * @return
   */
  public String getEndId() {
    if (myCurrentTransition == null) {
      return null;
    }
    return myCurrentTransition.myConstraintSetEnd;
  }

  public ConstraintSet getEndConstraintSet() {
    return myCurrentTransition.getConstraintSetEnd();
  }

  public ConstraintSet getStartConstraintSet() {
    return myCurrentTransition.getConstraintSetStart();
  }

  public ArrayList<TransitionTag> getTransitions() {
    return myTransition;
  }

  @SuppressWarnings("unused")
  public String[] getConstraintSetNames() {
    String[] names = new String[myConstraintSets.size()];
    for (int i = 0; i < names.length; i++) {
      names[i] = myConstraintSets.get(i).mId;
    }
    return names;
  }

  public void setCurrentTransition(int index) {
    myCurrentTransition = myTransition.get(index);
  }

  public String[] getTransitionsNames() {
    if (myTransitionNames != null) {
      return myTransitionNames;
    }
    String[] ret = new String[myTransition.size()];
    for (int i = 0; i < ret.length; i++) {
      TransitionTag transition = myTransition.get(i);
      String start = stripID(transition.myConstraintSetStart);
      String end = stripID(transition.myConstraintSetEnd);
      ret[i] = start + " - " + end;
    }

    return myTransitionNames = ret;
  }

  public String[] getSceneViewsNames() {
    String[] ret = new String[myViews.size()];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = myViews.get(i).getId();
    }
    return ret;
  }

  public MotionSceneView getSceneView(String s) {
    if (myCurrentTransition == null) {
      return null;
    }
    return myCurrentTransition.mViewsMap.get(s);
  }

  // Represents a single view in the motion scene
  public static class MotionSceneView {
    String mid;
    Icon myIcon;
    MotionSceneModel myModel;
    public ArrayList<KeyPos> myKeyPositions = new ArrayList<>();
    public ArrayList<KeyAttributes> myKeyAttributes = new ArrayList<>();
    public ArrayList<KeyCycle> myKeyCycles = new ArrayList<>();

    @NotNull
    public Icon getIcon() {
      if (myIcon == null) {
        myIcon = findIcon();
      }
      return myIcon;
    }

    @NotNull
    private Icon findIcon() {
      NlComponent component = myModel.myNlModel.find(mid);
      if (component == null) {
        return StudioIcons.LayoutEditor.Palette.VIEW;
      }
      ViewHandlerManager manager = ViewHandlerManager.get(myModel.myProject);
      ViewHandler handler = manager.getHandler(component);
      if (handler == null) {
        return StudioIcons.LayoutEditor.Palette.VIEW;
      }
      return handler.getIcon(component);
    }
  }

  public MotionSceneView getMotionSceneView(@NotNull String viewId) {
    return myCurrentTransition.mViewsMap.get(viewId);
  }

  /**
   * Called by createKeyFrame(), so will be in a read lock
   *
   * @param xmlFile
   * @param transitionTag
   * @return
   */
  @Nullable
  private XmlTag getKeyFrameForTransition(XmlFile xmlFile, TransitionTag transitionTag) {
    XmlTag transition = null;
    XmlTag keyFrame = null;
    XmlTag[] tags = xmlFile.getRootTag().getSubTags();
    for (int i = 0; i < tags.length; i++) {
      XmlTag tag = tags[i];
      String keyNodeName = tag.getName();
      if (keyNodeName.equals(MotionSceneTransition)) {
        transition = tag;
        break;
      }
    }
    if (transition == null) {
      return null;
    }
    tags = transition.getSubTags();
    for (int i = 0; i < tags.length; i++) {
      XmlTag tag = tags[i];
      String keyNodeName = tag.getName();
      if (keyNodeName.equals(MotionSceneKeyFrameSet)) {
        keyFrame = tag;
        break;
      }
    }
    if (keyFrame == null) { // no keyframes need to create
      keyFrame =
        transition.createChildTag(MotionSceneKeyFrameSet, null, null, false);
      keyFrame = transition.addSubTag(keyFrame, false);
    }
    return keyFrame;
  }

  private static String trimId(String name) { return name.substring(name.indexOf('/') + 1); }

  @Nullable
  private XmlTag getConstraintSetTag(XmlFile xmlFile, String name) {
    XmlTag constraintSet = null;
    XmlTag[] tags = xmlFile.getRootTag().getSubTags();
    for (int i = 0; i < tags.length; i++) {
      XmlTag tag = tags[i];
      String keyNodeName = tag.getName();
      if (keyNodeName.equals(MotionSceneConstraintSet)) {
        String constraintSetId = tag.getAttribute("android:id").getValue();
        if (name.equals(trimId(constraintSetId))) {
          constraintSet = tag;
          break;
        }
      }
    }
    return constraintSet;
  }

  public void createConstraint(String type, boolean startSet) {
    XmlFile xmlFile = (XmlFile)AndroidPsiUtils.getPsiFileSafely(myProject, myVirtualFile);
    final NlComponent component = myNlModel.find(type);
    TransactionGuard.submitTransaction(myProject, () ->
    {
      WriteCommandAction.runWriteCommandAction(myProject, new Runnable() {
      @Override
      public void run() {
        String name;
        if (startSet) {
          name = myCurrentTransition.myConstraintSetStart;
        }
        else {
          name = myCurrentTransition.myConstraintSetEnd;
        }
        name = trimId(name);
        XmlTag constraintSet = getConstraintSetTag(xmlFile, name);
        if (constraintSet != null) {
          XmlTag constraint = constraintSet.createChildTag(ConstraintSetConstraint, null, null, false);
          constraint = constraintSet.addSubTag(constraint, false);
          constraint.setAttribute("android:id", "@+id/" + type);
          transferAttributes(component, constraint);
        }
      }
    });
    if (myNlModel != null) {
      // TODO: we may want to do live edits instead, but LayoutLib needs
      // anyway to save the file to disk, so...
      saveAndNotify(xmlFile, myNlModel);
    }
    });
  }

  /**
   * copy constraint attributes across to
   *
   * @param component
   * @param constraint
   */
  private void transferAttributes(NlComponent component, XmlTag constraint) {
    HashSet<String> android = new HashSet<>(Arrays.asList(
      SdkConstants.ATTR_LAYOUT_WIDTH,
      SdkConstants.ATTR_LAYOUT_HEIGHT,
      SdkConstants.ATTR_LAYOUT_MARGIN_LEFT,
      SdkConstants.ATTR_LAYOUT_MARGIN_RIGHT,
      SdkConstants.ATTR_LAYOUT_MARGIN_START,
      SdkConstants.ATTR_LAYOUT_MARGIN_END,
      SdkConstants.ATTR_LAYOUT_MARGIN_TOP,
      SdkConstants.ATTR_LAYOUT_MARGIN_BOTTOM));

    HashSet<String> sherpa = new HashSet<>(Arrays.asList(
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_LEFT,
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_RIGHT,
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_START,
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_END,
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_TOP,
      SdkConstants.ATTR_LAYOUT_GONE_MARGIN_BOTTOM,
      SdkConstants.ATTR_LAYOUT_BASELINE_TO_BASELINE_OF,
      SdkConstants.ATTR_LAYOUT_TOP_TO_BOTTOM_OF,
      SdkConstants.ATTR_LAYOUT_TOP_TO_TOP_OF,
      SdkConstants.ATTR_LAYOUT_BOTTOM_TO_BOTTOM_OF,
      SdkConstants.ATTR_LAYOUT_BOTTOM_TO_TOP_OF,
      SdkConstants.ATTR_LAYOUT_LEFT_TO_LEFT_OF,
      SdkConstants.ATTR_LAYOUT_LEFT_TO_RIGHT_OF,
      SdkConstants.ATTR_LAYOUT_RIGHT_TO_LEFT_OF,
      SdkConstants.ATTR_LAYOUT_RIGHT_TO_RIGHT_OF,
      SdkConstants.ATTR_LAYOUT_START_TO_START_OF,
      SdkConstants.ATTR_LAYOUT_START_TO_END_OF,
      SdkConstants.ATTR_LAYOUT_END_TO_START_OF,
      SdkConstants.ATTR_LAYOUT_END_TO_END_OF,
      SdkConstants.ATTR_LAYOUT_HORIZONTAL_BIAS,
      SdkConstants.ATTR_LAYOUT_VERTICAL_BIAS,
      SdkConstants.ATTR_LAYOUT_WIDTH_DEFAULT,
      SdkConstants.ATTR_LAYOUT_HEIGHT_DEFAULT,
      SdkConstants.ATTR_LAYOUT_WIDTH_MIN,
      SdkConstants.ATTR_LAYOUT_WIDTH_MAX,
      SdkConstants.ATTR_LAYOUT_HEIGHT_MIN,
      SdkConstants.ATTR_LAYOUT_HEIGHT_MAX,
      SdkConstants.ATTR_LAYOUT_DIMENSION_RATIO,
      SdkConstants.ATTR_LAYOUT_VERTICAL_CHAIN_STYLE,
      SdkConstants.ATTR_LAYOUT_HORIZONTAL_CHAIN_STYLE,
      SdkConstants.ATTR_LAYOUT_VERTICAL_WEIGHT,
      SdkConstants.ATTR_LAYOUT_HORIZONTAL_WEIGHT));

    List<AttributeSnapshot> attr = component.getAttributesImpl(); // get the original attributes
    for (AttributeSnapshot at : attr) {
      if (sherpa.contains(at.name)) {
        constraint.setAttribute(MotionNameSpace + at.name, at.value);
      }
      if (android.contains(at.name)) {
        constraint.setAttribute(AndroidNameSpace + at.name, at.value);
      }
    }
  }

  /**
   * Create a new Keyframe tag
   *
   * @param nlModel
   * @param type
   * @param framePosition
   * @param id
   */
  @Nullable
  public SmartPsiElementPointer<XmlTag> createKeyFrame(String type, int framePosition, String id) {
    return createKeyFrame(type, framePosition, id, null);
  }

  /**
   * Create a new Keyframe tag
   *
   * @param nlModel
   * @param type
   * @param framePosition
   * @param id
   */
  @Nullable
  public SmartPsiElementPointer<XmlTag> createKeyFrame(String type, int framePosition, String id, KeyFrame copyFrom) {
    XmlFile xmlFile = (XmlFile)AndroidPsiUtils.getPsiFileSafely(myProject, myVirtualFile);
    switch (type) {
      case KeyTypePosition:
        for (KeyPos keys : myCurrentTransition.mViewsMap.get(id).myKeyPositions) {
          if (keys.framePosition == framePosition) {
            return null;
          }
        }
        break;
      case KeyTypeAttribute:
        for (KeyAttributes keys : myCurrentTransition.mViewsMap.get(id).myKeyAttributes) {
          if (keys.framePosition == framePosition) {
            return null;
          }
        }
        break;
    }
    XmlTag createdTag = WriteCommandAction.<XmlTag>runWriteCommandAction(myProject, () -> {
      if (myTransition.isEmpty()) {
        return null;
      }
      XmlTag transition = myCurrentTransition.getTag().getElement();
      if (transition == null) {
        return null;
      }
      XmlTag keyFrame = null;
      XmlTag[] tags = transition.getSubTags();
      for (int i = 0; i < tags.length; i++) {
        XmlTag tag = tags[i];
        String keyNodeName = tag.getName();
        if (keyNodeName.equals(MotionSceneKeyFrameSet)) {
          keyFrame = tag;
          break;
        }
      }
      if (keyFrame == null) { // no keyframes need to create
        keyFrame =
          xmlFile.getRootTag().createChildTag(MotionSceneKeyFrameSet, null, null, false);
        keyFrame = xmlFile.getRootTag().addSubTag(keyFrame, false);
      }

      XmlTag createdTag1 = keyFrame.createChildTag(type, null, null, false);
      createdTag1 = keyFrame.addSubTag(createdTag1, false);
      if (copyFrom != null) {
        for (AttrName attribute : copyFrom.myAttributes.keySet()) {
          Object value = copyFrom.myAttributes.get(attribute);
          createdTag1.setAttribute(attribute.getName(), attribute.getNamespace(), value.toString());
          if (DEBUG) {
            System.out.println(attribute.getNamespace() + " " + attribute.getName() + " = " + value.toString());
          }
        }
      }
      createdTag1.setAttribute(KeyAttributes_framePosition, AUTO_URI, Integer.toString(framePosition));
      createdTag1.setAttribute(KeyAttributes_target, AUTO_URI, "@id/" + id);
      return createdTag1;
    });
    if (myNlModel != null) {
      // TODO: we may want to do live edits instead, but LayoutLib needs
      // anyway to save the file to disk, so...
      saveAndNotify(xmlFile, myNlModel);
    }
    return SmartPointerManager.getInstance(myProject).createSmartPsiElementPointer(createdTag);
  }

  /**
   * Create a new onSwipe tag
   */
  @Nullable
  public OnSwipeTag createOnSwipe() {
    if (myCurrentTransition.myOnSwipeTag != null) {
      if (DEBUG) {
        Debug.println(" You can only have one on swipe tag");
      }
      return myCurrentTransition.myOnSwipeTag;
    }
    XmlFile xmlFile = (XmlFile)AndroidPsiUtils.getPsiFileSafely(myProject, myVirtualFile);

    XmlTag createdTag = WriteCommandAction.<XmlTag>runWriteCommandAction(myProject, () -> {
      if (DEBUG) {
        Debug.println("WriteCommandAction  createOnSwipe");
      }
      XmlTag transitionTag = myCurrentTransition.getTag().getElement();
      if (DEBUG) {
        Debug.println("myCurrentTransition  XmlTag " + transitionTag);
      }

      XmlTag onSwipeTag = transitionTag.createChildTag(OnSwipeTitle, null, null, false);
      onSwipeTag = transitionTag.addSubTag(onSwipeTag, false);

      onSwipeTag.setAttribute(OnSwipeAttr_dragDirection, AUTO_URI, "dragRight");
      onSwipeTag.setAttribute(OnSwipeAttr_touchAnchorId, AUTO_URI, "@+id/" + myViews.get(0).getId());
      onSwipeTag.setAttribute(OnSwipeAttr_touchAnchorSide, AUTO_URI, "top");
      if (DEBUG) {
        Debug.println("WriteCommandAction  createOnSwipe " + onSwipeTag);
      }

      return onSwipeTag;
    });

    if (myNlModel != null) {
      if (DEBUG) {
        Debug.println("saveAndNotify  myNlModel " + xmlFile);
      }

      // TODO: we may want to do live edits instead, but LayoutLib needs
      // anyway to save the file to disk, so...
      saveAndNotify(xmlFile, myNlModel);
    }
    else {
      if (DEBUG) {
        Debug.println("myNlModel  null?");
      }
    }

    if (DEBUG) {
      Debug.println("createdTag " + createdTag);
    }

    OnSwipeTag onSwipeTag = new OnSwipeTag(myCurrentTransition.myMotionSceneModel, createdTag);

    myCurrentTransition.myOnSwipeTag = onSwipeTag;
    return onSwipeTag;
  }

  /**
   * Create a new  OnClick tag
   */
  @Nullable
  public OnClickTag createOnClick() {

    XmlFile xmlFile = (XmlFile)AndroidPsiUtils.getPsiFileSafely(myProject, myVirtualFile);

    XmlTag createdTag = WriteCommandAction.<XmlTag>runWriteCommandAction(myProject, () -> {
      if (DEBUG) {
        Debug.println("WriteCommandAction  onClick");
      }

      XmlTag transitionTag = myCurrentTransition.getTag().getElement();
      if (DEBUG) {
        Debug.println("myCurrentTransition  XmlTag " + transitionTag);
      }

      XmlTag onClickTag = transitionTag.createChildTag(OnClickTitle, null, null, false);
      onClickTag = transitionTag.addSubTag(onClickTag, false);
      onClickTag.setAttribute(OnClickAttr_target, AUTO_URI, "@+id/" + myViews.get(0).getId());
      if (DEBUG) {
        Debug.println("WriteCommandAction  createOnSwipe " + onClickTag);
      }

      return onClickTag;
    });

    if (myNlModel != null) {
      if (DEBUG) {
        Debug.println("saveAndNotify  myNlModel " + xmlFile);
      }

      // TODO: we may want to do live edits instead, but LayoutLib needs
      // anyway to save the file to disk, so...
      saveAndNotify(xmlFile, myNlModel);
    }
    else {
      if (DEBUG) {
        Debug.println("myNlModel  null?");
      }
    }

    if (DEBUG) {
      Debug.println("createdTag " + createdTag);
    }

    OnClickTag onSwipeTag = new OnClickTag(myCurrentTransition.myMotionSceneModel, createdTag);

    myCurrentTransition.myOnClickTags.add(onSwipeTag);
    return onSwipeTag;
  }

  /**
   * Save file if necessary
   *
   * @param xmlFile
   * @param nlModel
   */
  public static void saveAndNotify(PsiFile xmlFile, NlModel nlModel) {
    LayoutPullParsers.saveFileIfNecessary(xmlFile);
    nlModel.notifyModified(NlModel.ChangeType.EDIT);
  }

  interface TagAccess {
    public SmartPsiElementPointer<XmlTag> getTag();
  }

  /* ===========================BaseTag===================================*/

  public static abstract class BaseTag implements TagAccess {
    protected final MotionSceneModel myMotionSceneModel;
    protected final String myTitle;
    protected final SmartPsiElementPointer<XmlTag> myTag;

    public BaseTag(@NotNull MotionSceneModel model, @NotNull XmlTag tag, @NotNull String title) {
      myMotionSceneModel = model;
      myTitle = title;
      myTag = SmartPointerManager.getInstance(model.getProject()).createSmartPsiElementPointer(tag);
    }

    @NotNull
    public String getTitle() {
      return myTitle;
    }

    @NotNull
    public MotionSceneModel getModel() {
      return myMotionSceneModel;
    }

    public XmlTag findMyTag() {
      return myTag.getElement();
    }

    @Override
    public SmartPsiElementPointer<XmlTag> getTag() {
      return myTag;
    }

    protected void completeSceneModelUpdate() {
      // Temporary for LayoutLib:
      saveAndNotify(myMotionSceneModel.motionSceneFile(), myMotionSceneModel.myNlModel);
    }

    public boolean deleteTag(@NotNull String command) {
      XmlTag tag = findMyTag();
      if (tag == null) {
        return false;
      }
      Runnable operation = () -> {
        tag.delete();
      };
      XmlFile xmlFile = myMotionSceneModel.motionSceneFile();
      WriteCommandAction.runWriteCommandAction(myMotionSceneModel.myProject, command, null, operation, xmlFile);
      completeSceneModelUpdate();
      return true;
    }

    public boolean setValues(@NotNull HashMap<AttrName, String> values) {
      XmlTag tag = findMyTag();
      if (tag == null) {
        return false;
      }
      String command = "Set attributes";
      Runnable operation = () -> {
        for (AttrName key : values.keySet()) {
          String value = values.get(key);
          tag.setAttribute(key.getName(), key.getNamespace(), value);
        }
      };

      XmlFile xmlFile = myMotionSceneModel.motionSceneFile();
      WriteCommandAction.runWriteCommandAction(myMotionSceneModel.myProject, command, null, operation, xmlFile);
      completeSceneModelUpdate();
      return true;
    }

    protected boolean setValue(@NotNull XmlTag tag, @NotNull AttrName attr, @NotNull String value) {
      String command = "Set " + attr + " attribute";
      Runnable operation = () -> tag.setAttribute(attr.getName(), attr.getNamespace(), value);

      XmlFile xmlFile = myMotionSceneModel.motionSceneFile();
      WriteCommandAction.runWriteCommandAction(myMotionSceneModel.myProject, command, null, operation, xmlFile);
      completeSceneModelUpdate();
      return true;
    }

    public boolean setValue(@NotNull AttrName attr, @NotNull String value) {
      XmlTag tag = findMyTag();
      if (tag == null) {
        return false;
      }
      return setValue(tag, attr, value);
    }

    @NotNull
    public abstract Set<AttrName> getAttributeNames();

    @NotNull
    public List<CustomAttributes> getCustomAttributes() {
      return Collections.emptyList();
    }

    @Nullable
    public abstract String getValue(@NotNull AttrName key);

    public boolean deleteAttribute(@NotNull AttrName attr) {
      XmlTag tag = findMyTag();
      if (tag == null) {
        return false;
      }
      String command = "Delete " + attr + " attribute";
      Runnable operation = () -> tag.setAttribute(attr.getName(), attr.getNamespace(), null);

      XmlFile xmlFile = myMotionSceneModel.motionSceneFile();
      WriteCommandAction.runWriteCommandAction(myMotionSceneModel.myProject, command, null, operation, xmlFile);
      completeSceneModelUpdate();
      return true;
    }
  }

  /* ===========================KeyFrame===================================*/

  public static abstract class KeyFrame extends BaseTag {
    protected String mType;

    int framePosition;
    String target;
    HashMap<AttrName, Object> myAttributes = new HashMap<>();
    protected AttrName[] myPossibleAttr;

    public KeyFrame(@NotNull MotionSceneModel motionSceneModel, @NotNull XmlTag tag, @NotNull String title) {
      super(motionSceneModel, tag, title);
    }

    @Override
    @Nullable
    public String getValue(@NotNull AttrName key) {
      Object value = myAttributes.get(key);
      return value != null ? value.toString() : null;
    }

    @Override
    @NotNull
    public Set<AttrName> getAttributeNames() {
      return myAttributes.keySet();
    }

    public abstract String[] getDefault(AttrName key);

    public CustomAttributes createCustomAttribute(@NotNull String key, @NotNull CustomAttributes.Type type, @NotNull String value) {
      // TODO: Do we need to support this for other tags than KeyAttributes ?
      throw new UnsupportedOperationException();
    }

    public String getString(String type) {
      if ("target".equals(type)) {
        return target;
      }
      return null;
    }

    public AttrName[] getPossibleAttr() {
      return myPossibleAttr;
    }

    public int getFramePosition() { return framePosition; }

    public float getFloat(String type) {
      String val = myAttributes.get(AttrName.motionAttr(type)).toString();
      if (val.endsWith("dp")) { // TODO check for px etc.
        val = val.substring(0, val.length() - 2);
      }
      return Float.parseFloat(val);
    }

    public void fill(HashMap<AttrName, Object> attributes) {
      attributes.put(AttrName.motionAttr(Key_framePosition), framePosition);
      attributes.put(AttrName.motionAttr(KeyCycle_target), target);
      attributes.putAll(myAttributes);
    }

    void parse(NamedNodeMap att) {
      int attCount = att.getLength();
      for (int i = 0; i < attCount; i++) {
        Node node = att.item(i);
        parse(new AttrName(node.getNamespaceURI(), node.getNodeName()), att.item(i).getNodeValue());
      }
    }

    float fparse(String v) {
      if (v.endsWith("dp")) {
        return Float.parseFloat(v.substring(0, v.length() - 2));
      }
      return Float.parseFloat(v);
    }

    void parse(AttrName name, String value) {
      if (value == null) {
        myAttributes.remove(name);
        return;
      }
      myAttributes.put(name, value);
      if (name.isMotionAttr(Key_framePosition)) {
        framePosition = Integer.parseInt(value);
      }
      else if (name.isMotionAttr(KeyAttributes_target)) {
        target = value.substring(value.indexOf('/') + 1);
      }
    }

    private String trim(String node) {
      return node.substring(node.indexOf(':') + 1);
    }

    public void parse(XmlAttribute[] attributes) {
      for (int i = 0; i < attributes.length; i++) {
        XmlAttribute attribute = attributes[i];
        parse(new AttrName(attribute), attribute.getValue());
      }
    }

    /**
     * Delete an attribute from a KeyFrame.
     */
    @Override
    public boolean deleteAttribute(@NotNull AttrName name) {
      // Never delete these required attributes:
      if (name.isMotionAttr(KeyAttributes_target) ||
          name.isMotionAttr(KeyAttributes_framePosition)) {
        // TODO: Find out why these are called in the first place...
        return false;
      }
      if (!super.deleteAttribute(name)) {
        return false;
      }
      myAttributes.remove(name);
      return true;
    }

    public boolean deleteTag() {
      String command = "Delete key attributes for: " + target;
      return super.deleteTag(command);
    }

    /**
     * Set the value of a KeyFrame attribute.
     */
    @Override
    public boolean setValue(@NotNull AttrName attr, @NotNull String value) {
      if (!super.setValue(attr, value)) {
        return false;
      }
      parse(attr, value);
      return true;
    }

    /**
     * Set multiple values of a Keyframe in one go
     */
    @Override
    public boolean setValues(@NotNull HashMap<AttrName, String> values) {
      if (!super.setValues(values)) {
        return false;
      }
      for (AttrName key : values.keySet()) {
        parse(key, values.get(key));
      }
      return true;
    }

    /**
     * @param xmlTag
     * @return
     */
    boolean match(XmlTag xmlTag) {
      String keyNodeName = xmlTag.getName();

      if (!keyNodeName.equals(mType)) return false;
      XmlAttribute[] attr = xmlTag.getAttributes();
      for (int k = 0; k < attr.length; k++) {
        XmlAttribute attribute = attr[k];
        if (attribute.getName().endsWith("framePosition")) {
          if (Integer.parseInt(attribute.getValue()) != framePosition) {
            return false;
          }
        }
        if (attribute.getName().endsWith("target")) {
          if (!attribute.getValue().endsWith(target)) {
            return false;
          }
        }
      }

      return true;
    }

    public String getName() {
      return mType;
    }

    public String getEasingCurve() {
      return (String)myAttributes.get(AttrName.motionAttr(KeyPosition_transitionEasing));
    }

    public abstract KeyFrame cloneKeyFrame();
  }

  /* ============================KeyPos==================================*/

  public static abstract class KeyPos extends KeyFrame {
    String transitionEasing = null;

    public KeyPos(MotionSceneModel motionSceneModel, XmlTag tag) {
      super(motionSceneModel, tag, KeyPositionTitle);
    }

    @Override
    public float getFloat(String type) {
      return super.getFloat(type);
    }

    @Override
    void parse(AttrName name, String value) {
      if (name.isMotionAttr(KeyPosition_transitionEasing)) {
        transitionEasing = value;
      }
      super.parse(name, value);
    }

    @Override
    public void fill(HashMap<AttrName, Object> attributes) {
      super.fill(attributes);
      if (transitionEasing != null) {
        attributes.put(AttrName.motionAttr(Key_framePosition), framePosition);
      }
    }
  }

  /* ==========================KeyPosition====================================*/
  public static class KeyPosition extends KeyPos {
    static final String TYPE = KeyTypePosition;
    float percentX = Float.NaN;
    float percentY = Float.NaN;
    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr("framePosition"),
      AttrName.motionAttr("target"),
      AttrName.motionAttr("transitionEasing"),
      AttrName.motionAttr("curveFit"),
      AttrName.motionAttr("drawPath"),
      AttrName.motionAttr("sizePercent"),
      AttrName.motionAttr("sizePercent"),
      AttrName.motionAttr("percentY"),
      AttrName.motionAttr("percentX"),
      AttrName.motionAttr("type"),
    };
    public static String[][] ourDefaults = {
      {},
      {},
      {"cubic(0.5,0,0.5,1)"},
      {"spline", "linear"},
      {"true", "false"},
      {"0.5"},
      {"0.0"},
      {"0.5"}
    };

    @Override
    public String[] getDefault(AttrName key) {
      for (int i = 0; i < ourPossibleAttr.length; i++) {
        if (key.equals(ourPossibleAttr[i])) {
          return (ourDefaults.length > i) ? ourDefaults[i] : ourDefaults[0];
        }
      }
      return ourDefaults[0];
    }

    public KeyPosition(MotionSceneModel motionSceneModel, XmlTag tag) {
      super(motionSceneModel, tag);
      myPossibleAttr = ourPossibleAttr;
      mType = KeyTypePosition;
    }

    @Override
    public void fill(HashMap<AttrName, Object> attributes) {
      super.fill(attributes);
      if (!Float.isNaN(percentX)) {
        attributes.put(AttrName.motionAttr(KeyPositionPath_path_percent), percentX);
      }
      if (!Float.isNaN(percentY)) {
        attributes.put(AttrName.motionAttr(KeyPositionPath_perpendicularPath_percent), percentY);
      }
    }

    @Override
    public float getFloat(String type) {
      if ("perpendicularPath_percent".equals(type)) {
        return percentY;
      }
      if ("path_percent".equals(type)) {
        return percentY;
      }
      return super.getFloat(type);
    }

    @Override
    void parse(AttrName node, String value) {
      super.parse(node, value);
      if (node.isMotionAttr(KeyPositionPath_path_percent)) {
        percentX = Float.parseFloat(value);
      }
      else if (node.isMotionAttr(KeyPositionPath_perpendicularPath_percent)) {
        percentY = Float.parseFloat(value);
      }
    }

    @Override
    public KeyFrame cloneKeyFrame() {
      KeyPosition ret = new KeyPosition(myMotionSceneModel, myTag.getElement());
      ret.myAttributes.putAll(myAttributes);
      return ret;
    }
  }

  /* ===========================KeyAttributes===================================*/
  public static class KeyAttributes extends KeyFrame {
    static final String TYPE = KeyTypeAttribute;
    String curveFit = null;
    ArrayList<CustomAttributes> myCustomAttributes = new ArrayList<>();
    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr("framePosition"),
      AttrName.motionAttr("target"),
      AttrName.motionAttr("transitionEasing"),
      AttrName.motionAttr("curveFit"),
      AttrName.motionAttr("sizePercent"),
      AttrName.motionAttr("progress"),
      AttrName.motionAttr("orientation"),
      AttrName.androidAttr("alpha"),
      AttrName.androidAttr("elevation"),
      AttrName.androidAttr("rotation"),
      AttrName.androidAttr("rotationX"),
      AttrName.androidAttr("rotationY"),
      AttrName.motionAttr("transitionPathRotate"),
      AttrName.androidAttr("scaleX"),
      AttrName.androidAttr("scaleY"),
      AttrName.androidAttr("translationX"),
      AttrName.androidAttr("translationY"),
      AttrName.androidAttr("translationZ"),
      AttrName.customAttr(MotionSceneString.getCustomLabel()),
    };
    public static String[][] ourDefaults = {
      {},
      {},
      {"curve=(0.5,0,0.5,1)"},
      {"spline", "linear"},
      {"0.5"},
      {"0.5"},
      {"90"},
      {"0.5"},
      {"5dp"},
      {"45"},
      {"10"},
      {"10"},
      {"90"},
      {"1.5"},
      {"1.5"},
      {"10dp"},
      {"10dp"},
      {"10dp"},
    };

    public static String[] ourPossibleStandardAttr = {
      "orientation",
      "alpha",
      "elevation",
      "rotation",
      "rotationX",
      "rotationY",
      "scaleX",
      "scaleY",
      "translationX",
      "translationY",
      "translationZ"
    };
    HashSet<String> myAndroidAttributes = null;

    @Override
    @NotNull
    public List<CustomAttributes> getCustomAttributes() {
      return myCustomAttributes;
    }

    @Override
    public String[] getDefault(AttrName key) {
      for (int i = 0; i < ourPossibleAttr.length; i++) {
        if (key.equals(ourPossibleAttr[i])) {
          return (ourDefaults.length > i) ? ourDefaults[i] : ourDefaults[0];
        }
      }
      return ourDefaults[0];
    }

    public KeyAttributes(MotionSceneModel motionSceneModel, XmlTag tag) {
      super(motionSceneModel, tag, KeyAttributesTitle);
      mType = KeyTypeAttribute;
      myPossibleAttr = ourPossibleAttr;
    }

    @Override
    public void fill(HashMap<AttrName, Object> attributes) {
      attributes.putAll(myAttributes);
      if (curveFit != null) {
        attributes.put(AttrName.motionAttr(KeyAttributes_curveFit), curveFit);
      }
      super.fill(attributes);
    }

    @Override
    void parse(AttrName node, String value) {
      if (node.isMotionAttr(KeyAttributes_curveFit)) {
        curveFit = value;
        myAttributes.put(node, value);
      }
      else if (ourStandardSet.contains(node)) {
        myAttributes.put(node, value);
      }
      else {
        super.parse(node, value);
      }
    }

    @Override
    public KeyFrame cloneKeyFrame() {
      KeyAttributes ret = new KeyAttributes(myMotionSceneModel, myTag.getElement());
      ret.myAttributes.putAll(myAttributes);
      return ret;
    }

    @Override
    @Nullable
    public CustomAttributes createCustomAttribute(@NotNull String key, @NotNull CustomAttributes.Type type, @NotNull String value) {
      XmlTag keyFrame = findMyTag();
      if (keyFrame == null) {
        return null;
      }
      List<CustomAttributes> existing =
        myCustomAttributes.stream().filter(attr -> key.equals(attr.getAttributeName())).collect(Collectors.toList());
      for (CustomAttributes attr : existing) {
        attr.deleteTag();
      }
      Computable<CustomAttributes> operation = () -> {
        XmlTag createdTag = keyFrame.createChildTag(KeyAttributes_customAttribute, null, null, false);
        createdTag = keyFrame.addSubTag(createdTag, false);
        createdTag.setAttribute(CustomAttributes_attributeName, AUTO_URI, key);
        createdTag.setAttribute(type.getTagName(), AUTO_URI, StringUtil.isNotEmpty(value) ? value : type.getDefaultValue());
        CustomAttributes custom = new CustomAttributes(this, createdTag);
        Arrays.stream(createdTag.getAttributes()).forEach(attr -> custom.parse(AttrName.motionAttr(attr.getLocalName()), attr.getValue()));
        myCustomAttributes.add(custom);
        return custom;
      };
      CustomAttributes newAttribute = WriteCommandAction.runWriteCommandAction(myMotionSceneModel.myProject, operation);
      completeSceneModelUpdate();
      return newAttribute;
    }
  }

  /* ===========================KeyCycle===================================*/
  public static class KeyCycle extends KeyFrame {
    static final String TYPE = KeyTypeCycle;
    float waveOffset = Float.NaN;
    float wavePeriod = Float.NaN;
    ArrayList<CustomCycleAttributes> myCustomAttributes = new ArrayList<>();
    String waveShape;
    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr("target"),
      AttrName.motionAttr("framePosition"),
      AttrName.motionAttr("transitionEasing"),
      AttrName.motionAttr("curveFit"),
      AttrName.motionAttr("progress"),
      AttrName.motionAttr("waveShape"),
      AttrName.motionAttr("wavePeriod"),
      AttrName.motionAttr("waveOffset"),
      //TODO "waveVariesBy",
      AttrName.motionAttr("transitionPathRotate"),
      AttrName.motionAttr("alpha"),
      AttrName.motionAttr("elevation"),
      AttrName.motionAttr("rotation"),
      AttrName.motionAttr("rotationX"),
      AttrName.motionAttr("rotationY"),
      AttrName.motionAttr("scaleX"),
      AttrName.motionAttr("scaleY"),
      AttrName.motionAttr("translationX"),
      AttrName.motionAttr("translationY"),
      AttrName.motionAttr("translationZ"),
    };
    public static String[][] ourDefaults = {
      {},
      {},
      {"curve=(0.5,0,0.5,1)"},
      {"spline", "linear"},
      {"0.5"},
      {"sin", "square", "triangle", "sawtooth", "reverseSawtooth", "cos", "bounce"},
      {"1"},
      {"0"},
      {"90"},
      {"0.5"},
      {"20dp"},
      {"45"},
      {"10"},
      {"10"},
      {"1.5"},
      {"1.5"},
      {"20dp"},
      {"20dp"},
      {"20dp"},
    };

    @Override
    public String[] getDefault(AttrName key) {
      for (int i = 0; i < ourPossibleAttr.length; i++) {
        if (key.equals(ourPossibleAttr[i])) {
          return (ourDefaults.length > i) ? ourDefaults[i] : ourDefaults[0];
        }
      }
      return ourDefaults[0];
    }

    @Override
    public String toString() {
      return getName() + Arrays.toString(myAttributes.keySet().toArray());
    }

    public KeyCycle(MotionSceneModel motionSceneModel, XmlTag tag) {
      super(motionSceneModel, tag, KeyCycleTitle);
      super.mType = KeyTypeCycle;
      myPossibleAttr = ourPossibleAttr;
    }

    @Override
    public void fill(HashMap<AttrName, Object> attributes) {
      attributes.putAll(myAttributes);
      if (!Float.isNaN(waveOffset)) {
        attributes.put(AttrName.motionAttr(KeyCycle_waveOffset), waveOffset);
      }
      if (!Float.isNaN(wavePeriod)) {
        attributes.put(AttrName.motionAttr(KeyCycle_wavePeriod), wavePeriod);
      }
      if (waveShape != null) {
        attributes.put(AttrName.motionAttr(KeyCycle_waveOffset), waveShape);
      }
      super.fill(attributes);
    }

    @Override
    void parse(AttrName node, String value) {
      if (node.isMotionAttr(KeyCycle_waveOffset)) {
        waveOffset = fparse(value);
      }
      else if (node.isMotionAttr(KeyCycle_wavePeriod)) {
        wavePeriod = Float.parseFloat(value);
      }
      else if (node.isMotionAttr(KeyCycle_waveShape)) {
        waveShape = value;
      }
      super.parse(node, value);
    }

    @Override
    public KeyFrame cloneKeyFrame() {
      KeyCycle ret = new KeyCycle(myMotionSceneModel, myTag.getElement());
      ret.myAttributes.putAll(myAttributes);
      return ret;
    }
  }

  public static class KeyTimeCycle extends KeyCycle {
    static final String TYPE = KeyTypeTimeCycle;

    public KeyTimeCycle(MotionSceneModel motionSceneModel, XmlTag tag) {
      super(motionSceneModel, tag);
    }
  }

  /* ===========================CustomCycleAttributes===================================*/

  public static class CustomCycleAttributes implements AttributeParse {
    KeyCycle parentKeyCycle;
    HashMap<AttrName, Object> myAttributes = new HashMap<>();
    public static AttrName[] ourPossibleAttr = {
      AttrName.customAttr("attributeName"),
      AttrName.customAttr("customIntegerValue"),
      AttrName.customAttr("customFloatValue"),
      AttrName.customAttr("customDimension"),
    };
    public static String[][] ourDefaults = {
      {},
      {"1"},
      {"1.0"},
      {"20dp"}
    };

    public String[] getDefault(AttrName key) {
      for (int i = 0; i < ourPossibleAttr.length; i++) {
        if (key.equals(ourPossibleAttr[i])) {
          return (ourDefaults.length > i) ? ourDefaults[i] : ourDefaults[0];
        }
      }
      return ourDefaults[0];
    }

    public CustomCycleAttributes(KeyCycle frame) {
      parentKeyCycle = frame;
    }

    @Override
    public void parse(AttrName name, String value) {
      myAttributes.put(name, value);
    }
  }

  /* ===========================CustomAttributes===================================*/

  public static class CustomAttributes extends BaseTag implements AttributeParse {
    private final KeyAttributes parentKeyAttributes;
    private final HashMap<AttrName, Object> myAttributes = new HashMap<>();

    @Nullable
    public String getAttributeName() {
      return (String)myAttributes.get(AttrName.motionAttr(CustomAttributes_attributeName));
    }

    @Nullable
    public String getValueTagName() {
      return Arrays.stream(Type.values()).map(key -> key.getTagName())
        .filter(tag -> myAttributes.containsKey(AttrName.customAttr(tag)))
        .findFirst()
        .orElse(null);
    }

    @Override
    public String toString() {
      return Arrays.toString(myAttributes.keySet().toArray());
    }

    public boolean deleteTag() {
      if (!super.deleteTag("Remove custom attribute")) {
        return false;
      }
      parentKeyAttributes.getCustomAttributes().remove(this);
      return true;
    }

    @Override
    @NotNull
    public Set<AttrName> getAttributeNames() {
      return Collections.singleton(AttrName.customAttr(getAttributeName()));
    }

    @Override
    @Nullable
    public String getValue(@NotNull AttrName key) {
      Object value = myAttributes.get(key);
      return value != null ? value.toString() : null;
    }

    @Override
    public boolean setValue(@NotNull AttrName key, @NotNull String value) {
      if (!super.setValue(key, value)) {
        return false;
      }
      parse(key, value);
      return true;
    }

    @Override
    public boolean deleteAttribute(@NotNull AttrName attributeName) {
      throw new UnsupportedOperationException();
    }

    public enum Type {
      CUSTOM_COLOR("Color", CustomAttributes_customColorValue, "#FFF"),
      CUSTOM_INTEGER("Integer", CustomAttributes_customIntegerValue, "2"),
      CUSTOM_FLOAT("Float", CustomAttributes_customFloatValue, "1.0"),
      CUSTOM_STRING("String", CustomAttributes_customStringValue, "Example"),
      CUSTOM_DIMENSION("Dimension", CustomAttributes_customDimensionValue, "20dp"),
      CUSTOM_BOOLEAN("Boolean", CustomAttributes_customBooleanValue, "true");

      private final String myStringValue;
      private final String myTagName;
      private final String myDefaultValue;

      @NotNull
      public String getTagName() {
        return myTagName;
      }

      @NotNull
      public String getDefaultValue() {
        return myDefaultValue;
      }

      @Override
      public String toString() {
        return myStringValue;
      }

      Type(@NotNull String stringValue, @NotNull String tagName, @NotNull String defaultValue) {
        myStringValue = stringValue;
        myTagName = tagName;
        myDefaultValue = defaultValue;
      }
    }

    public CustomAttributes(@NotNull KeyAttributes frame, @NotNull XmlTag tag) {
      super(frame.getModel(), tag, "");
      parentKeyAttributes = frame;
    }

    public HashMap<AttrName, Object> getAttributes() {
      return myAttributes;
    }

    @Override
    public void parse(AttrName name, String value) {
      myAttributes.put(name, value);
    }
  }

  // =================================AttributeParse====================================== //

  interface AttributeParse {
    void parse(AttrName name, String value);
  }

  // =================================ConstraintSet====================================== //

  public static class ConstraintSet implements AttributeParse {
    String mId;
    SmartPsiElementPointer<XmlTag> mTag;

    public ConstraintSet(@NotNull XmlTag tag) {
      mId = tag.getAttributeValue(ATTR_ID, ANDROID_URI);
      mTag = SmartPointerManager.getInstance(tag.getProject()).createSmartPsiElementPointer(tag);
    }

    public String getId() {
      return mId;
    }

    public SmartPsiElementPointer<XmlTag> getTag() {
      return mTag;
    }

    void setId(String id) {
      mId = stripID(id);
    }

    HashMap<String, ConstraintView> myConstraintViews = new HashMap<>();

    @Nullable
    public ConstraintView getConstraintView(@NotNull String id) {
      return myConstraintViews.get(id);
    }

    @Override
    public void parse(AttrName name, String value) {
      if (name.isId()) {
        mId = stripID(value);
      }
    }
  }

  // =================================TransitionTag====================================== //
  public static class TransitionTag extends BaseTag implements AttributeParse {
    private final AttrName[] myPossibleAttr;
    String myConstraintSetEnd;
    String myConstraintSetStart;
    OnSwipeTag myOnSwipeTag = null;
    ArrayList<OnClickTag> myOnClickTags = new ArrayList<>();
    int duration;
    public ArrayList<KeyPos> myKeyPositions = new ArrayList<>();
    public ArrayList<KeyAttributes> myKeyAttributes = new ArrayList<>();
    public ArrayList<KeyCycle> myKeyCycles = new ArrayList<>();
    public HashMap<String, MotionSceneView> mViewsMap = new HashMap<>();
    HashMap<AttrName, Object> myAllAttributes = new HashMap<>();

    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr("constraintSetStart"),
      AttrName.motionAttr("constraintSetEnd"),
      AttrName.motionAttr("duration"),
      AttrName.motionAttr("staggered"),
    };

    void addKeyFrame(MotionSceneModel model, KeyFrame frame) {
      String id = frame.target;
      MotionSceneView motionView = mViewsMap.get(id);
      if (motionView == null) {
        motionView = new MotionSceneView();
        motionView.myModel = model;
        motionView.mid = id;
        mViewsMap.put(id, motionView);
      }
      if (frame instanceof KeyAttributes) {
        motionView.myKeyAttributes.add((KeyAttributes)frame);
      }
      else if (frame instanceof KeyPos) {
        motionView.myKeyPositions.add((KeyPos)frame);
      }
      else if (frame instanceof KeyCycle) {
        motionView.myKeyCycles.add((KeyCycle)frame);
      }
    }

    public AttrName[] getPossibleAttr() {
      return myPossibleAttr;
    }

    public HashMap<AttrName, Object> getAttributes() {
      return myAllAttributes;
    }

    TransitionTag(MotionSceneModel model, XmlTag tag) {
      super(model, tag, TransitionTitle);
      myPossibleAttr = ourPossibleAttr;
    }

    public ConstraintSet getConstraintSetEnd() {
      if (myConstraintSetEnd == null) {
        return null;
      }
      for (ConstraintSet set : myMotionSceneModel.myConstraintSets) {
        if (myConstraintSetEnd.equals(set.mId)) {
          return set;
        }
      }
      return null;
    }

    public ConstraintSet getConstraintSetStart() {
      if (myConstraintSetStart == null) {
        return null;
      }
      for (ConstraintSet set : myMotionSceneModel.myConstraintSets) {
        if (myConstraintSetStart.equals(set.mId)) {
          return set;
        }
      }
      return null;
    }

    @Override
    public void parse(AttrName name, String value) {
      myAllAttributes.put(name, value);
      // TODO: Handle namespace case i.e. replace AUTO_URI with conditional namespace of ConstraintLayout
      if (name.isMotionAttr(TransitionConstraintSetEnd)) {
        if (DEBUG) {
          System.out.println("  TransitionConstraintSetEnd " + value);
        }
        myConstraintSetEnd = stripID(value);
      }
      else if (name.isMotionAttr(TransitionConstraintSetStart)) {
        myConstraintSetStart = stripID(value);
      }
      else if (name.isMotionAttr(TransitionDuration)) {
        duration = Integer.parseInt(value);
      }
    }

    @Override
    public boolean deleteTag(@NotNull String command) {
      throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public Set<AttrName> getAttributeNames() {
      return myAllAttributes.keySet();
    }

    @Override
    @Nullable
    public String getValue(@NotNull AttrName key) {
      Object value = myAllAttributes.get(key);
      return value != null ? value.toString() : null;
    }

    @Override
    public boolean setValue(@NotNull AttrName key, @NotNull String value) {
      if (!super.setValue(key, value)) {
        return false;
      }
      parse(key, value);
      return true;
    }

    @Override
    public boolean deleteAttribute(@NotNull AttrName attributeName) {
      if (!super.deleteAttribute(attributeName)) {
        return false;
      }
      myAllAttributes.remove(attributeName);
      return true;
    }
  }
  // =================================OnClick====================================== //

  public static class OnClickTag extends BaseTag implements AttributeParse {
    HashMap<AttrName, Object> myAllAttributes = new HashMap<>();
    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr(OnClickAttr_target),
      AttrName.motionAttr(OnSwipeAttr_touchAnchorSide),
    };
    public AttrName[] myPossibleAttr = ourPossibleAttr;

    public AttrName[] getPossibleAttr() {
      return myPossibleAttr;
    }

    public HashMap<AttrName, Object> getAttributes() {
      return myAllAttributes;
    }

    OnClickTag(@NotNull MotionSceneModel model, XmlTag tag) {
      super(model, tag, OnClickTitle);
      myPossibleAttr = ourPossibleAttr;
    }

    @Override
    public void parse(AttrName name, String value) {
      if (DEBUG) {
        System.out.println("=================");
        System.out.println("parse (" + name + "  ,  " + value + " )");
      }
      myAllAttributes.put(name, value);
    }

    @Override
    @Nullable
    public String getValue(@NotNull AttrName key) {
      Object value = myAllAttributes.get(key);
      return value != null ? value.toString() : null;
    }

    @Override
    @NotNull
    public Set<AttrName> getAttributeNames() {
      return myAllAttributes.keySet();
    }

    @Override
    public boolean deleteAttribute(@NotNull AttrName attributeName) {
      if (!super.deleteAttribute(attributeName)) {
        return false;
      }
      myAllAttributes.remove(attributeName);
      return true;
    }

    public boolean deleteTag() {
      return deleteTag("Delete OnClick");
    }
  }

  // =================================OnSwipe====================================== //

  public static class OnSwipeTag extends BaseTag implements AttributeParse {
    HashMap<AttrName, Object> myAllAttributes = new HashMap<>();
    public static AttrName[] ourPossibleAttr = {
      AttrName.motionAttr("maxVelocity"),
      AttrName.motionAttr("maxAcceleration"),
      AttrName.motionAttr("dragDirection"),
      AttrName.motionAttr("touchAnchorId"),
      AttrName.motionAttr("touchAnchorSide"),
    };
    public AttrName[] myPossibleAttr = ourPossibleAttr;

    public AttrName[] getPossibleAttr() {
      return myPossibleAttr;
    }

    public HashMap<AttrName, Object> getAttributes() {
      return myAllAttributes;
    }

    OnSwipeTag(@NotNull MotionSceneModel model, XmlTag tag) {
      super(model, tag, OnSwipeTitle);
      myPossibleAttr = ourPossibleAttr;
    }

    @Override
    public void parse(AttrName name, String value) {
      if (DEBUG) {
        System.out.println("====================================================================================");
        System.out.println("parse (" + name + "  ,  " + value + " )");
      }
      myAllAttributes.put(name, value);
    }

    @Override
    @Nullable
    public String getValue(@NotNull AttrName key) {
      Object value = myAllAttributes.get(key);
      return value != null ? value.toString() : null;
    }

    @Override
    @NotNull
    public Set<AttrName> getAttributeNames() {
      return myAllAttributes.keySet();
    }

    @Override
    public boolean deleteAttribute(@NotNull AttrName attributeName) {
      if (!super.deleteAttribute(attributeName)) {
        return false;
      }
      myAllAttributes.remove(attributeName);
      return true;
    }

    public boolean deleteTag() {
      return deleteTag("Delete OnSwing");
    }
  }

  // =================================ConstraintView====================================== //

  /**
   * This contains <MotionScene> <ConstraintSet><Constraint\></ConstraintSet></MotionScene>
   */
  public static class ConstraintView implements AttributeParse, TagAccess {
    String mId;
    SmartPsiElementPointer<XmlTag> mTag;

    public ConstraintView(@NotNull XmlTag tag) {
      mTag = SmartPointerManager.getInstance(tag.getProject()).createSmartPsiElementPointer(tag);
    }

    HashMap<AttrName, Object> myAllAttributes = new HashMap<>();

    void setId(String id) {
      mId = stripID(id);
    }

    HashMap<String, String> myConstraintViews = new HashMap<>();

    @Nullable
    public String getAttribute(@NotNull AttrName name) {
      Object value = myAllAttributes.get(name);
      return value != null ? value.toString() : null;
    }

    public void setAttribute(@NotNull AttrName name, @Nullable String value) {
      if (value == null) {
        myAllAttributes.remove(name);
      }
      else {
        myAllAttributes.put(name, value);
      }
    }

    @Override
    public void parse(AttrName name, String value) {
      myAllAttributes.put(name, value);
    }

    @Override
    public SmartPsiElementPointer<XmlTag> getTag() {
      return mTag;
    }
  }

  private static void parse(AttributeParse a, XmlAttribute[] attributes) {
    if (DEBUG) {
      System.out.println("====================================================================================");
      System.out.println(" parse(AttributeParse a, XmlAttribute[] attributes)");
    }
    for (int i = 0; i < attributes.length; i++) {
      XmlAttribute attribute = attributes[i];
      parse(a, new AttrName(attribute), attribute.getValue());
    }
  }

  private static void parse(AttributeParse a, AttrName name, String value) {
    a.parse(name, value);
  }

  /**
   * Entry point to build the model
   *
   * @param model
   * @param project
   * @param virtualFile
   * @param file
   * @return
   */
  public static MotionSceneModel parse(NlComponent component,
                                       Project project,
                                       VirtualFile virtualFile,
                                       XmlFile file) {
    NlModel model = component.getModel();
    MotionSceneModel motionSceneModel = new MotionSceneModel();
    motionSceneModel.myName = virtualFile.getName();
    ArrayList<ConstraintSet> constraintSet = new ArrayList<>();
    if (DEBUG) {
      System.out.println("====================================================================================");
      System.out.println(" parse ... VirtualFile " + virtualFile.getCanonicalPath());
    }
    motionSceneModel.myNlModel = model;
    motionSceneModel.myVirtualFile = virtualFile;
    motionSceneModel.myProject = project;
    motionSceneModel.myViews = component.getChildren();

    // Process all the constraint sets
    XmlTag[] tagKeyFrames = file.getRootTag().findSubTags(MotionSceneConstraintSet);
    for (int i = 0; i < tagKeyFrames.length; i++) {
      XmlTag frame = tagKeyFrames[i];
      ConstraintSet set = new ConstraintSet(frame);
      parse(set, frame.getAttributes());
      constraintSet.add(set);
      XmlTag[] subTags = frame.getSubTags();
      for (int j = 0; j < subTags.length; j++) {
        XmlTag subtag = subTags[j];
        if (ConstraintSetConstraint.equals(subtag.getName())) {
          ConstraintView view = new ConstraintView(subtag);
          view.setId(subtag.getAttributeValue("android:id"));
          parse(view, subtag.getAttributes());
          set.myConstraintViews.put(view.mId, view);
        }
      }
    }
    motionSceneModel.myConstraintSets = constraintSet;

    // process the Transition

    XmlTag[] transitionTags = file.getRootTag().findSubTags(MotionSceneTransition);
    if (transitionTags.length > 0) {
      motionSceneModel.myTransition = new ArrayList<>();
    }
    for (XmlTag transitionTag : transitionTags) {
      TransitionTag transition = new TransitionTag(motionSceneModel, transitionTag);

      /**
       * Populate transition with views
       */
      for (NlComponent view : motionSceneModel.myViews) {
        String id = stripID(view.getId());
        MotionSceneView motionView = transition.mViewsMap.get(id);
        if (motionView == null) {
          motionView = new MotionSceneView();
        }
        motionView.myModel = motionSceneModel;
        motionView.mid = id;
        transition.mViewsMap.put(motionView.mid, motionView);
      }

      XmlTag tag = transitionTag;
      parse(transition, tag.getAttributes());
      motionSceneModel.myTransition.add(transition);
      motionSceneModel.myCurrentTransition = transition;
      {
        XmlTag[] onSwipeTags = tag.findSubTags(MotionSceneOnSwipe);
        if (onSwipeTags.length > 1) {
          System.err.println("Should only have one tag");
        }
        for (XmlTag onSwipeTag1 : onSwipeTags) {
          OnSwipeTag onSwipeTag = new OnSwipeTag(motionSceneModel, onSwipeTag1);
          transition.myOnSwipeTag = onSwipeTag;
          XmlTag swipeTag = onSwipeTag1;
          parse(onSwipeTag, swipeTag.getAttributes());
          transition.myOnSwipeTag = onSwipeTag;
        }
      }

      XmlTag[] tags = tag.findSubTags(MotionSceneOnClick);

      for (XmlTag onClickTag : tags) {
        OnClickTag onClick = new OnClickTag(motionSceneModel, onClickTag);
        transition.myOnClickTags.add(onClick);

        parse(onClick, onClickTag.getAttributes());
      }

      // process all the key frames
      tagKeyFrames = tag.findSubTags(MotionSceneKeyFrameSet);

      for (XmlTag tagKeyFrame : tagKeyFrames) {
        XmlTag[] tagkey = tagKeyFrame.getSubTags();

        for (XmlTag xmlTag : tagkey) {
          XmlTag[] customTags = xmlTag.getSubTags();
          String keyNodeName = xmlTag.getName();

          KeyFrame frame = null;
          switch (keyNodeName) {
            case KeyPosition.TYPE:
              frame = new KeyPosition(motionSceneModel, xmlTag);
              break;
            case KeyAttributes.TYPE:
              frame = new KeyAttributes(motionSceneModel, xmlTag);
              for (XmlTag ctag : customTags) {
                CustomAttributes custom = new CustomAttributes((KeyAttributes)frame, ctag);
                parse(custom, ctag.getAttributes());
                ((KeyAttributes)frame).myCustomAttributes.add(custom);
              }
              break;
            case KeyCycle.TYPE:
              frame = new KeyCycle(motionSceneModel, xmlTag);
              for (XmlTag ctag : customTags) {
                CustomCycleAttributes custom = new CustomCycleAttributes((KeyCycle)frame);
                parse(custom, ctag.getAttributes());
                ((KeyCycle)frame).myCustomAttributes.add(custom);
              }
              break;
            case KeyTimeCycle.TYPE:
              frame = new KeyTimeCycle(motionSceneModel, xmlTag);
              for (XmlTag ctag : customTags) {
                CustomCycleAttributes custom = new CustomCycleAttributes((KeyTimeCycle)frame);
                parse(custom, ctag.getAttributes());
                ((KeyTimeCycle)frame).myCustomAttributes.add(custom);
              }
              break;

            default:
              System.err.println("Unknown name :" + keyNodeName);
          }
          if (frame != null) {
            frame.parse(xmlTag.getAttributes());
            motionSceneModel.addKeyFrame(frame);
          }
          transition.addKeyFrame(motionSceneModel, frame);
        }
      }
    }
    // process the OnSwipe

    return motionSceneModel;
  }

  public static ArrayList<MotionSceneModel.KeyFrame> filterList(ArrayList<? extends MotionSceneModel.KeyFrame> keyList,
                                                                AttrName name) {
    ArrayList<MotionSceneModel.KeyFrame> ret = new ArrayList<>();
    for (KeyFrame keyFrame : keyList) {
      if (keyFrame.myAttributes.containsKey(name)) {
        ret.add(keyFrame);
      }
    }
    return ret;
  }

  public static AttrName[] getGraphAttributes(ArrayList<? extends MotionSceneModel.KeyFrame> keyList) {
    HashSet<AttrName> set = new HashSet<>();
    for (KeyFrame frame : keyList) {
      if (frame instanceof KeyAttributes) {
        set.addAll(frame.myAttributes.keySet());
      }
      else if (frame instanceof KeyCycle) {
        set.addAll(frame.myAttributes.keySet());
      }
    }
    return set.toArray(new AttrName[0]);
  }

  void addKeyFrame(String id) {
    MotionSceneView motionView = myCurrentTransition.mViewsMap.get(id);
    if (motionView == null) {
      motionView = new MotionSceneView();
      motionView.myModel = this;
      motionView.mid = id;
      myCurrentTransition.mViewsMap.put(id, motionView);
    }
  }

  void addKeyFrame(KeyFrame frame) {
    String id = frame.target;
    MotionSceneView motionView = myCurrentTransition.mViewsMap.get(id);
    if (motionView == null) {
      motionView = new MotionSceneView();
      motionView.myModel = this;
      motionView.mid = id;
      myCurrentTransition.mViewsMap.put(id, motionView);
    }
    if (frame instanceof KeyAttributes) {
      motionView.myKeyAttributes.add((KeyAttributes)frame);
    }
    else if (frame instanceof KeyPos) {
      motionView.myKeyPositions.add((KeyPos)frame);
    }
    else if (frame instanceof KeyCycle) {
      motionView.myKeyCycles.add((KeyCycle)frame);
    }
  }
}
