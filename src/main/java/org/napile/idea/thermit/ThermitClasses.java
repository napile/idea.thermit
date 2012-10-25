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

package org.napile.idea.thermit;

/**
 * @author VISTALL
 * @date 17:00/25.10.12
 *
 * .nzip is not loaded into java class loader. need hardcode define
 */
public interface ThermitClasses
{
	String IntrospectionHelper = "org.napile.thermit.IntrospectionHelper";

	String Task = "org.napile.thermit.Task";

	String TaskContainer = "org.napile.thermit.TaskContainer";

	String Project = "org.napile.thermit.Project";

	//public static final String PathTokenizer = "org.napile.thermit.PathTokenizer";

	String MODULE_FILE_NAME = "@module@.xml";

	/*private static volatile UrlClassLoader bundledClassLoader;

	private static ClassLoader getBundledClassLoader()
	{
		if(bundledClassLoader == null)
		{
			File dir = new File(PathManager.getPluginsPath() + "/idea.thermit/lib");
			if(!dir.exists())
				throw new RuntimeException("Plugin dir is not found");

			bundledClassLoader = new UrlClassLoader(Collections.<URL>emptyList(), null);
			try
			{
				for(File f : dir.listFiles())
					if(f.getName().endsWith(".nzip"))
						bundledClassLoader.addURL(f.toURL());
			}
			catch(MalformedURLException e)
			{
				throw new RuntimeException(e);
			}
		}

		return bundledClassLoader;
	}  */
}
