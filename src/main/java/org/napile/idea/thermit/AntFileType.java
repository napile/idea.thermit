/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.napile.idea.thermit;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AntFileType extends LanguageFileType {

  @NonNls public static final String DEFAULT_EXTENSION = "thermit";
  @NonNls public static final String DOT_DEFAULT_EXTENSION = ".thermit";

  public AntFileType() {
    super(new AntLanguage());
  }

  @NotNull
  @NonNls
  public String getName() {
    return "ANT";
  }

  @NotNull
  public String getDescription() {
    return IdeBundle.message("filetype.description.ant");
  }

  @NotNull
  @NonNls
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nullable
  public Icon getIcon() {
    return AllIcons.FileTypes.Xml;
  }
}
