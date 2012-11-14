/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.napile.idea.thermit.config.execution;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.napile.idea.thermit.ThermitBundle;
import org.napile.idea.thermit.ThermitClasses;
import org.napile.idea.thermit.config.impl.AntBuildFileImpl;
import org.napile.idea.thermit.config.impl.AntInstallation;
import org.napile.idea.thermit.config.impl.BuildFileProperty;
import org.napile.idea.thermit.config.impl.GlobalThermitConfiguration;
import org.napile.idea.thermit.config.impl.ThermitConfigurationImpl;
import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.ide.macro.Macro;
import com.intellij.ide.macro.MacroManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.config.AbstractProperty;
import com.intellij.util.containers.ContainerUtil;

public class AntCommandLineBuilder
{
	private final List<String> myTargets = new ArrayList<String>();
	private final JavaParameters myCommandLine = new JavaParameters();
	private String myBuildFilePath;
	private List<BuildFileProperty> myProperties;
	private boolean myDone = false;
	@NonNls
	private final List<String> myExpandedProperties = new ArrayList<String>();
	@NonNls
	private static final String INPUT_HANDLER_PARAMETER = "-inputhandler";
	@NonNls
	private static final String LOGFILE_PARAMETER = "-logfile";
	@NonNls
	private static final String LOGFILE_SHORT_PARAMETER = "-l";

	public void calculateProperties(final DataContext dataContext, List<BuildFileProperty> additionalProperties) throws Macro.ExecutionCancelledException
	{
		for(BuildFileProperty property : myProperties)
		{
			expandProperty(dataContext, property);
		}
		for(BuildFileProperty property : additionalProperties)
		{
			expandProperty(dataContext, property);
		}
	}

	private void expandProperty(DataContext dataContext, BuildFileProperty property) throws Macro.ExecutionCancelledException
	{
		String value = property.getPropertyValue();
		final MacroManager macroManager = GlobalThermitConfiguration.getMacroManager();
		value = macroManager.expandMacrosInString(value, true, dataContext);
		value = macroManager.expandMacrosInString(value, false, dataContext);
		myExpandedProperties.add("-D" + property.getPropertyName() + "=" + value);
	}

	public void addTarget(String targetName)
	{
		myTargets.add(targetName);
	}

	public void setBuildFile(AbstractProperty.AbstractPropertyContainer container, File buildFile) throws CantRunException
	{
		String jdkName = AntBuildFileImpl.CUSTOM_JDK_NAME.get(container);
		Sdk jdk;
		if(jdkName != null && jdkName.length() > 0)
		{
			jdk = GlobalThermitConfiguration.findJdk(jdkName);
		}
		else
		{
			jdkName = ThermitConfigurationImpl.DEFAULT_JDK_NAME.get(container);
			if(jdkName == null || jdkName.length() == 0)
			{
				throw new CantRunException(ThermitBundle.message("project.jdk.not.specified.error.message"));
			}
			jdk = GlobalThermitConfiguration.findJdk(jdkName);
		}
		if(jdk == null)
		{
			throw new CantRunException(ThermitBundle.message("jdk.with.name.not.configured.error.message", jdkName));
		}
		VirtualFile homeDirectory = jdk.getHomeDirectory();
		if(homeDirectory == null)
		{
			throw new CantRunException(ThermitBundle.message("jdk.with.name.bad.configured.error.message", jdkName));
		}
		myCommandLine.setJdk(jdk);

		final ParametersList vmParametersList = myCommandLine.getVMParametersList();
		vmParametersList.add("-Xmx" + AntBuildFileImpl.MAX_HEAP_SIZE.get(container) + "m");
		vmParametersList.add("-Xss" + AntBuildFileImpl.MAX_STACK_SIZE.get(container) + "m");

		final AntInstallation antInstallation = AntBuildFileImpl.ANT_INSTALLATION.get(container);
		if(antInstallation == null)
		{
			throw new CantRunException(ThermitBundle.message("ant.installation.not.configured.error.message"));
		}

		final String antHome = AntInstallation.HOME_DIR.get(antInstallation.getProperties());
		vmParametersList.add("-Dthermit.home=" + antHome);

		String[] urls = jdk.getRootProvider().getUrls(OrderRootType.CLASSES);
		final String jdkHome = homeDirectory.getPath().replace('/', File.separatorChar);
		@NonNls final String pathToJre = jdkHome + File.separator + "jre" + File.separator;
		for(String url : urls)
		{
			final String path = PathUtil.toPresentableUrl(url);
			if(!path.startsWith(pathToJre))
			{
				myCommandLine.getClassPath().add(path);
			}
		}

		myCommandLine.getClassPath().addAllFiles(AntBuildFileImpl.ALL_CLASS_PATH.get(container));

		myCommandLine.getClassPath().addAllFiles(AntBuildFileImpl.getUserHomeLibraries());

		final SdkTypeId sdkType = jdk.getSdkType();
		if(sdkType instanceof JavaSdkType)
		{
			final String toolsJar = ((JavaSdkType) sdkType).getToolsPath(jdk);
			if(toolsJar != null)
			{
				myCommandLine.getClassPath().add(toolsJar);
			}
		}

		PathUtilEx.addRtJar(myCommandLine.getClassPath());

		myCommandLine.getClassPath().add(findIdeaRunnerLib());

		myCommandLine.setMainClass(ThermitClasses.RUNNER_MAIN);
		final ParametersList programParameters = myCommandLine.getProgramParametersList();

		final String additionalParams = AntBuildFileImpl.ANT_COMMAND_LINE_PARAMETERS.get(container);
		if(additionalParams != null)
		{
			for(String param : ParametersList.parse(additionalParams))
			{
				if(param.startsWith("-J"))
				{
					final String cutParam = param.substring("-J".length());
					if(cutParam.length() > 0)
					{
						vmParametersList.add(cutParam);
					}
				}
				else
				{
					programParameters.add(param);
				}
			}
		}

		if(!(programParameters.getList().contains(LOGFILE_SHORT_PARAMETER) || programParameters.getList().contains(LOGFILE_PARAMETER)))
			programParameters.add("-logger", ThermitClasses.RUNNER_LOGGER);

		if(!programParameters.getList().contains(INPUT_HANDLER_PARAMETER))

			programParameters.add(INPUT_HANDLER_PARAMETER, ThermitClasses.RUNNER_INPUT_HANDLER);

		myProperties = AntBuildFileImpl.ANT_PROPERTIES.get(container);

		myBuildFilePath = buildFile.getAbsolutePath();
		myCommandLine.setWorkingDirectory(buildFile.getParent());
	}

	private File findIdeaRunnerLib()
	{
		File temp = null;
		File antHome = new File(PathManager.getPluginsPath() + "/idea.thermit/lib");
		if(antHome.exists() && (temp = new File(antHome, "idea.thermit.runner.jar")).exists())
			return temp;

		// search in bundled plugins
		antHome = new File(PathManager.getHomePath());
		if(antHome.exists())
		{
			antHome = new File(antHome, "plugins/idea.thermit/lib");

			if(antHome.exists() && (temp = new File(antHome, "idea.thermit.runner.jar")).exists())
				return temp;
		}

		throw new UnsupportedOperationException();
	}

	public JavaParameters getCommandLine()
	{
		if(myDone)
			return myCommandLine;
		ParametersList programParameters = myCommandLine.getProgramParametersList();
		for(final String property : myExpandedProperties)
		{
			if(property != null)
			{
				programParameters.add(property);
			}
		}
		programParameters.add("-buildfile", myBuildFilePath);
		for(final String target : myTargets)
		{
			if(target != null)
			{
				programParameters.add(target);
			}
		}
		myDone = true;
		return myCommandLine;
	}

	public void addTargets(String[] targets)
	{
		ContainerUtil.addAll(myTargets, targets);
	}

	public String[] getTargets()
	{
		return ArrayUtil.toStringArray(myTargets);
	}
}
