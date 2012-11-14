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
package org.napile.idea.thermit.validation;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.napile.idea.thermit.ThermitBundle;
import org.napile.idea.thermit.dom.AntDomProject;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;

public abstract class AntInspection extends BasicDomElementsInspection<AntDomProject>
{

	protected AntInspection()
	{
		super(AntDomProject.class);
	}

	@Nls
	@NotNull
	public String getGroupDisplayName()
	{
		return ThermitBundle.message("ant.inspections.display.name");
	}

	@NotNull
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.ERROR;
	}

	public boolean isEnabledByDefault()
	{
		return true;
	}
}
