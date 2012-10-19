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
package org.napile.idea.thermit.config.impl;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.napile.idea.thermit.ThermitSupport;
import org.napile.idea.thermit.config.AntBuildFile;
import org.napile.idea.thermit.config.AntBuildFileBase;
import org.napile.idea.thermit.config.AntBuildListener;
import org.napile.idea.thermit.config.AntBuildModel;
import org.napile.idea.thermit.config.AntBuildModelBase;
import org.napile.idea.thermit.config.AntBuildTargetBase;
import org.napile.idea.thermit.config.ThermitConfiguration;
import org.napile.idea.thermit.config.execution.ExecutionHandler;
import org.napile.idea.thermit.dom.AntDomElement;
import org.napile.idea.thermit.dom.AntDomProject;
import org.napile.idea.thermit.dom.AntDomRecursiveVisitor;
import org.napile.idea.thermit.dom.AntDomTarget;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.ArrayUtil;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.util.xml.DomTarget;

public class AntBuildTargetImpl implements AntBuildTargetBase
{

	private final AntBuildModelBase myModel;
	private final VirtualFile myFile;
	private final boolean myIsDefault;
	private final int myHashCode;
	private final String myName;
	private final String myDisplayName;
	private final String myDescription;
	private final Project myProject;
	private final int myTextOffset;

	public AntBuildTargetImpl(final AntDomTarget target, final AntBuildModelBase buildModel, final VirtualFile sourceFile, final boolean isImported, final boolean isDefault)
	{
		myModel = buildModel;
		myFile = sourceFile;
		myIsDefault = isDefault;
		myHashCode = target.hashCode();
		myName = target.getName().getRawText();
		String name = target.getName().getRawText();
		if(isImported)
		{
			final String projectName = target.getAntProject().getName().getRawText();
			name = projectName + "." + name;
		}
		myDisplayName = name;
		myProject = target.getManager().getProject();
		final DomTarget domTarget = DomTarget.getTarget(target);
		if(domTarget != null)
		{
			myTextOffset = domTarget.getTextOffset();
		}
		else
		{
			myTextOffset = target.getXmlTag().getTextOffset();
		}

		final String desc = target.getDescription().getRawText();
		myDescription = (desc != null && desc.trim().length() > 0) ? desc : null;
	}

	public int hashCode()
	{
		return myHashCode;
	}

	public boolean equals(Object obj)
	{
		if(!(obj instanceof AntBuildTargetImpl))
		{
			return false;
		}
		final AntBuildTargetImpl that = (AntBuildTargetImpl) obj;
		return Comparing.equal(myName, that.myName) && Comparing.equal(myFile, that.myFile);
	}

	public Project getProject()
	{
		return myProject;
	}

	@Nullable
	public String getName()
	{
		return myName;
	}

	@Nullable
	public String getDisplayName()
	{
		return myDisplayName;
	}

	@Nullable
	public String getNotEmptyDescription()
	{
		return myDescription;
	}

	public boolean isDefault()
	{
		return myIsDefault;
	}

	public VirtualFile getContainingFile()
	{
		return myFile;
	}

	public AntBuildModelBase getModel()
	{
		return myModel;
	}

	@Nullable
	public String getActionId()
	{
		final String modelName = myModel.getName();
		if(modelName == null || modelName.length() == 0)
		{
			return null;
		}
		final StringBuilder name = StringBuilderSpinAllocator.alloc();
		try
		{
			name.append(ThermitConfiguration.getActionIdPrefix(myModel.getBuildFile().getProject()));
			name.append("_");
			name.append(modelName);
			name.append('_');
			name.append(getName());
			return name.toString();
		}
		finally
		{
			StringBuilderSpinAllocator.dispose(name);
		}
	}

	@Nullable
	public BuildTask findTask(final String taskName)
	{
		final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(myFile);
		final AntDomProject domProject = ThermitSupport.getAntDomProject(psiFile);
		if(domProject != null)
		{
			final AntDomTarget antTarget = domProject.findDeclaredTarget(myName);
			if(antTarget != null)
			{
				final Ref<AntDomElement> result = new Ref<AntDomElement>(null);
				antTarget.accept(new AntDomRecursiveVisitor()
				{
					public void visitAntDomElement(AntDomElement element)
					{
						if(result.get() != null)
						{
							return;
						}
						if(element.isTask() && taskName.equals(element.getXmlElementName()))
						{
							result.set(element);
							return;
						}
						super.visitAntDomElement(element);
					}
				});
				final AntDomElement task = result.get();
				if(task != null)
				{
					return new BuildTask(this, task);
				}
			}
		}
		return null;
	}

	public OpenFileDescriptor getOpenFileDescriptor()
	{
		return (myFile == null) ? null : new OpenFileDescriptor(myProject, myFile, myTextOffset);
	}

	public void run(DataContext dataContext, List<BuildFileProperty> additionalProperties, AntBuildListener buildListener)
	{
		AntBuildModel model = getModel();
		if(model == null)
		{
			buildListener.buildFinished(AntBuildListener.FAILED_TO_RUN, 0);
			throw new IllegalStateException("Target '" + getName() + "' is invalid: model is null");
		}
		AntBuildFile buildFile = model.getBuildFile();
		if(buildFile == null)
		{
			buildListener.buildFinished(AntBuildListener.FAILED_TO_RUN, 0);
			throw new IllegalStateException("Target '" + getName() + "' is invalid: build file is null");
		}

		String[] targets = isDefault() ? ArrayUtil.EMPTY_STRING_ARRAY : new String[]{getName()};
		ExecutionHandler.runBuild((AntBuildFileBase) buildFile, targets, null, dataContext, additionalProperties, buildListener);
	}
}
