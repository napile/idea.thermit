/*
 * Copyright 2010-2012 napile.org
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

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

import com.intellij.ui.ListUtil;

/**
* @author VISTALL
* @date 19:16/20.12.12
*/
public class AntClasspathEditorForm
{
	public JButton myAddButton;
	public JButton myAddAllInDir;
	public JButton myRemoveButton;
	public JButton myMoveUpButton;
	public JButton myMoveDownButton;
	public JPanel myWholePanel;
	public JList myClasspathList;
	private final ArrayList<ListUtil.Updatable> myUpdatables = new ArrayList<ListUtil.Updatable>();

	public AntClasspathEditorForm()
	{
		myClasspathList.setCellRenderer(new AntUIUtil.ClasspathRenderer());

		myUpdatables.add(ListUtil.addRemoveListener(myRemoveButton, myClasspathList));
		myUpdatables.add(ListUtil.addMoveUpListener(myMoveUpButton, myClasspathList));
		myUpdatables.add(ListUtil.addMoveDownListener(myMoveDownButton, myClasspathList));
	}

	public void enableButtons(boolean enable)
	{
		for(ListUtil.Updatable updatable : myUpdatables)
			updatable.enable(enable);
	}
}
