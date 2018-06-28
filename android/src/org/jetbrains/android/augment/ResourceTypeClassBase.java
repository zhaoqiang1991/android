package org.jetbrains.android.augment;

import com.android.ide.common.rendering.api.AttrResourceValue;
import com.android.ide.common.rendering.api.StyleableResourceValue;
import com.android.ide.common.rendering.api.ResourceNamespace;
import com.android.ide.common.resources.AbstractResourceRepository;
import com.android.ide.common.resources.ResourceItem;
import com.android.resources.ResourceType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.android.resourceManagers.ResourceManager;
import org.jetbrains.android.util.AndroidResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for light implementations of inner classes of the R class, e.g. {@code R.string}.
 *
 * <p>Implementations need to implement {@link #doGetFields()}, most likely by calling one of the {@code buildResourceFields} methods.
 */
public abstract class ResourceTypeClassBase extends AndroidLightInnerClassBase {

  @NotNull
  protected final ResourceType myResourceType;

  @Nullable
  private CachedValue<PsiField[]> myFieldsCache;

  protected static PsiType INT_ARRAY = PsiType.INT.createArrayType();

  public ResourceTypeClassBase(@NotNull PsiClass context, @NotNull ResourceType resourceType) {
    super(context, resourceType.getName());
    myResourceType = resourceType;
  }

  @NotNull
  protected static PsiField[] buildResourceFields(@Nullable ResourceManager manager,
                                                  @NotNull AbstractResourceRepository repository,
                                                  @NotNull ResourceNamespace namespace,
                                                  @NotNull AndroidLightField.FieldModifier fieldModifier,
                                                  @NotNull ResourceType resourceType,
                                                  @NotNull PsiClass context) {
    Map<String, PsiType> fieldNames = new HashMap<>();
    PsiType basicType = ResourceType.STYLEABLE == resourceType ? INT_ARRAY : PsiType.INT;

    for (String resName : repository.getItemsOfType(namespace, resourceType)) {
      fieldNames.put(resName, basicType);
    }

    if (ResourceType.STYLEABLE == resourceType) {
      List<ResourceItem> items = repository.getResourceItems(namespace, ResourceType.STYLEABLE);
      for (ResourceItem item : items) {
        StyleableResourceValue value = (StyleableResourceValue)item.getResourceValue();
        if (value != null) {
          List<AttrResourceValue> attributes = value.getAllAttributes();
          for (AttrResourceValue attr : attributes) {
            if (manager == null || manager.isResourcePublic(attr.getResourceType().getName(), attr.getName())) {
              ResourceNamespace attrNamespace = attr.getNamespace();
              String packageName = attrNamespace.getPackageName();
              if (attrNamespace.equals(namespace) || StringUtil.isEmpty(packageName)) {
                fieldNames.put(item.getName() + '_' + attr.getName(), PsiType.INT);
              }
              else {
                fieldNames.put(item.getName() + '_' + packageName.replace('.', '_') + '_' + attr.getName(), PsiType.INT);
              }
            }
          }
        }
      }
    }

    return buildResourceFields(fieldNames, resourceType, context, fieldModifier);
  }

  @NotNull
  protected static PsiField[] buildResourceFields(@NotNull Map<String, PsiType> fieldNames,
                                                  @NotNull ResourceType resourceType,
                                                  @NotNull PsiClass context,
                                                  @NotNull AndroidLightField.FieldModifier fieldModifier) {
    PsiField[] result = new PsiField[fieldNames.size()];
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());

    int idIterator = resourceType.ordinal() * 100000;
    int i = 0;

    for (Map.Entry<String, PsiType> entry : fieldNames.entrySet()) {
      String fieldName = AndroidResourceUtil.getFieldNameByResourceName(entry.getKey());
      PsiType type = entry.getValue();
      int id = -(idIterator++);
      AndroidLightField field = new AndroidLightField(fieldName,
                                                      context,
                                                      type,
                                                      fieldModifier,
                                                      fieldModifier == AndroidLightField.FieldModifier.FINAL ? id : null);
      field.setInitializer(factory.createExpressionFromText(Integer.toString(id), field));
      result[i++] = field;
    }
    return result;
  }

  @NotNull
  @Override
  public PsiField[] getFields() {
    if (myFieldsCache == null) {
      myFieldsCache = CachedValuesManager.getManager(getProject()).createCachedValue(
        () -> CachedValueProvider.Result.create(doGetFields(), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
    }
    return myFieldsCache.getValue();
  }

  @NotNull
  protected abstract PsiField[] doGetFields();

  @NotNull
  public ResourceType getResourceType() {
    return myResourceType;
  }
}
