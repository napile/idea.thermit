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
package org.napile.idea.thermit.config.impl.configuration;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.napile.idea.thermit.config.impl.AllNZipsUnderDirEntry;
import org.napile.idea.thermit.config.impl.AntClasspathEntry;
import org.napile.idea.thermit.config.impl.SinglePathEntry;
import com.intellij.util.config.AbstractProperty;
import com.intellij.util.config.ListProperty;

public class AntClasspathEditorPanel extends JPanel
{
	private ListProperty<AntClasspathEntry> myClasspathProperty;
	private final AntClasspathEditorForm myForm = new AntClasspathEditorForm();
	private UIPropertyBinding.Composite myBinding;

	public AntClasspathEditorPanel()
	{
		super(new BorderLayout());
		add(myForm.myWholePanel, BorderLayout.CENTER);
	}

	public UIPropertyBinding setClasspathProperty(ListProperty<AntClasspathEntry> classpathProperty)
	{
		myClasspathProperty = classpathProperty;
		myBinding = new UIPropertyBinding.Composite();
		UIPropertyBinding.OrderListBinding<AntClasspathEntry> classpathBinding = myBinding.bindList(myForm.myClasspathList, myClasspathProperty);
		classpathBinding.addAddManyFacility(myForm.myAddButton, new SinglePathEntry.AddEntriesFactory(myForm.myClasspathList));
		classpathBinding.addAddManyFacility(myForm.myAddAllInDir, new AllNZipsUnderDirEntry.AddEntriesFactory(myForm.myClasspathList));
		myBinding.addBinding(new UIPropertyBinding()
		{
			public void loadValues(AbstractProperty.AbstractPropertyContainer container)
			{
			}

			public void apply(AbstractProperty.AbstractPropertyContainer container)
			{
			}

			public void beDisabled()
			{
				myForm.enableButtons(false);
			}

			public void beEnabled()
			{
				myForm.enableButtons(true);
			}

			public void addAllPropertiesTo(Collection<AbstractProperty> properties)
			{
			}
		});
		return myBinding;
	}

	public JComponent getPreferedFocusComponent()
	{
		return myForm.myClasspathList;
	}
}
