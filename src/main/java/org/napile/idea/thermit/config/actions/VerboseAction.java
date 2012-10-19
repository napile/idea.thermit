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
package org.napile.idea.thermit.config.actions;

import org.napile.idea.thermit.AntBundle;
import org.napile.idea.thermit.config.execution.AntBuildMessageView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public final class VerboseAction extends ToggleAction
{
	private final AntBuildMessageView myAntBuildMessageView;

	public VerboseAction(AntBuildMessageView antBuildMessageView)
	{
		super(AntBundle.message("ant.verbose.show.all.messages.action.name"), AntBundle.message("ant.verbose.show.all.messages.action.description"), AllIcons.Ant.Verbose);
		myAntBuildMessageView = antBuildMessageView;
	}

	public boolean isSelected(AnActionEvent event)
	{
		return myAntBuildMessageView.isVerboseMode();
	}

	public void setSelected(AnActionEvent event, boolean flag)
	{
		myAntBuildMessageView.setVerboseMode(flag);
	}
}
