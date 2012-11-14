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

package org.napile.thermit.idea.runner;

import java.lang.reflect.InvocationTargetException;

public class ThermitMain2
{
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		IdeaThermitLogger2.guardStreams();

		// first try to use the new way of launching ant
		try
		{
			final Class antLauncher = Class.forName("org.napile.thermit.launch.Launcher");
			//noinspection HardCodedStringLiteral
			antLauncher.getMethod("main", new Class[]{args.getClass()}).invoke(null, new Object[]{args});
			return;
		}
		catch(ClassNotFoundException e)
		{
			// ignore and try older variant
		}

		final Class antMain = Class.forName("org.napile.thermit.Main");
		//noinspection HardCodedStringLiteral
		antMain.getMethod("main", new Class[]{args.getClass()}).invoke(null, new Object[]{args});
	}
}
