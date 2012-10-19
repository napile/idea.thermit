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

import org.jetbrains.annotations.NonNls;
import org.napile.idea.thermit.AntBundle;
import org.napile.idea.thermit.config.ExecutionEvent;

public final class ExecuteBeforeCompilationEvent extends ExecutionEvent
{
	@NonNls
	public static final String TYPE_ID = "beforeCompilation";

	private static final ExecuteBeforeCompilationEvent ourInstance = new ExecuteBeforeCompilationEvent();

	private ExecuteBeforeCompilationEvent()
	{
	}

	public static ExecuteBeforeCompilationEvent getInstance()
	{
		return ourInstance;
	}

	public String getTypeId()
	{
		return TYPE_ID;
	}

	public String getPresentableName()
	{
		return AntBundle.message("ant.event.before.compilation.presentable.name");
	}
}

