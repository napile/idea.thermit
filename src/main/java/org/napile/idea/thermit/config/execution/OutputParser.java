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
package org.napile.idea.thermit.config.execution;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.idea.thermit.ThermitBundle;
import com.intellij.compiler.impl.javaCompiler.FileObject;
import com.intellij.compiler.impl.javaCompiler.javac.JavacOutputParser;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.rt.ant.execution.IdeaAntLogger2;
import com.intellij.util.text.StringTokenizer;

public class OutputParser
{

	@NonNls
	private static final String JAVAC = "javac";
	@NonNls
	private static final String NAPILEC = "napilec";
	@NonNls
	private static final String ECHO = "echo";

	private static final Logger LOG = Logger.getInstance(OutputParser.class);
	private final Project myProject;
	private final AntBuildMessageView myMessageView;
	private final WeakReference<ProgressIndicator> myProgress;
	private final String myBuildName;
	private final OSProcessHandler myProcessHandler;
	private boolean isStopped;

	private List<String> myJavacMessages;
	private List<String> myNapileMessages;

	private boolean myFirstLineProcessed;
	private boolean myStartedSuccessfully;
	private boolean myIsEcho;

	public OutputParser(Project project, OSProcessHandler processHandler, AntBuildMessageView errorsView, ProgressIndicator progress, String buildName)
	{
		myProject = project;
		myProcessHandler = processHandler;
		myMessageView = errorsView;
		myProgress = new WeakReference<ProgressIndicator>(progress);
		myBuildName = buildName;
		myMessageView.setParsingThread(this);
	}

	public final void stopProcess()
	{
		myProcessHandler.destroyProcess();
	}

	public boolean isTerminateInvoked()
	{
		return myProcessHandler.isProcessTerminating();
	}

	protected Project getProject()
	{
		return myProject;
	}

	protected OSProcessHandler getProcessHandler()
	{
		return myProcessHandler;
	}

	public final boolean isStopped()
	{
		return isStopped;
	}

	public final void setStopped(boolean stopped)
	{
		isStopped = stopped;
	}

	private void setProgressStatistics(String s)
	{
		final ProgressIndicator progress = myProgress.get();
		if(progress != null)
		{
			progress.setText2(s);
		}
	}

	private void setProgressText(String s)
	{
		final ProgressIndicator progress = myProgress.get();
		if(progress != null)
		{
			progress.setText(s);
		}
	}

	private void printRawError(String text)
	{
		myMessageView.outputError(text, 0);
	}

	public final void readErrorOutput(String text)
	{
		if(!myFirstLineProcessed)
		{
			myFirstLineProcessed = true;
			myStartedSuccessfully = false;
			myMessageView.buildFailed(myBuildName);
		}
		if(!myStartedSuccessfully)
		{
			printRawError(text);
		}
	}


	protected final void processTag(char tagName, final String tagValue, final int priority)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug(String.valueOf(tagName) + priority + "=" + tagValue);
		}

		if(IdeaAntLogger2.TARGET == tagName)
		{
			setProgressStatistics(ThermitBundle.message("target.tag.name.status.text", tagValue));
		}
		else if(IdeaAntLogger2.TASK == tagName)
		{
			setProgressText(ThermitBundle.message("executing.task.tag.value.status.text", tagValue));
			if(JAVAC.equals(tagValue))
			{
				myJavacMessages = new ArrayList<String>();
			}
			else if(NAPILEC.equals(tagValue))
			{
				myNapileMessages = new ArrayList<String>();
			}
			else if(ECHO.equals(tagValue))
			{
				myIsEcho = true;
			}
		}

		if(myJavacMessages != null && (IdeaAntLogger2.MESSAGE == tagName || IdeaAntLogger2.ERROR == tagName))
		{
			myJavacMessages.add(tagValue);
			return;
		}
		if(myNapileMessages != null && (IdeaAntLogger2.MESSAGE == tagName || IdeaAntLogger2.ERROR == tagName))
		{
			myNapileMessages.add(tagValue);
			return;
		}

		if(IdeaAntLogger2.MESSAGE == tagName)
		{
			if(myIsEcho)
			{
				myMessageView.outputMessage(tagValue, AntBuildMessageView.PRIORITY_VERBOSE);
			}
			else
			{
				myMessageView.outputMessage(tagValue, priority);
			}
		}
		else if(IdeaAntLogger2.TARGET == tagName)
		{
			myMessageView.startTarget(tagValue);
		}
		else if(IdeaAntLogger2.TASK == tagName)
		{
			myMessageView.startTask(tagValue);
		}
		else if(IdeaAntLogger2.ERROR == tagName)
		{
			myMessageView.outputError(tagValue, priority);
		}
		else if(IdeaAntLogger2.EXCEPTION == tagName)
		{
			String exceptionText = tagValue.replace(IdeaAntLogger2.EXCEPTION_LINE_SEPARATOR, '\n');
			myMessageView.outputException(exceptionText);
		}
		else if(IdeaAntLogger2.BUILD == tagName)
		{
			myMessageView.startBuild(myBuildName);
		}
		else if(IdeaAntLogger2.TARGET_END == tagName || IdeaAntLogger2.TASK_END == tagName)
		{
			if(myJavacMessages != null)
			{
				processJavacMessages(myJavacMessages, myMessageView, myProject);
				myJavacMessages = null;
			}
			if(myNapileMessages != null)
			{
				processNapilecMessages(myNapileMessages, myMessageView, myProject);
				myNapileMessages = null;
			}
			myIsEcho = false;

			if(IdeaAntLogger2.TARGET_END == tagName)
			{
				myMessageView.finishTarget();
			}
			else
			{
				myMessageView.finishTask();
			}
		}
	}

	private static void processNapilecMessages(final List<String> napileMessages, final AntBuildMessageView messageView, Project project)
	{
		for(final String str : napileMessages)
		{
			try
			{
				AntBuildMessageView.MessageType category = AntBuildMessageView.MessageType.MESSAGE;
				String categoryOfMessage = str.substring(0, str.indexOf(":"));
				if(categoryOfMessage.equals("ERROR"))
					category = AntBuildMessageView.MessageType.ERROR;

				int fileEnd = str.indexOf(".ns: (");
				final String url = str.substring(str.indexOf(":") + 2, fileEnd + 3);
				String ranges = str.substring(fileEnd + 6, str.indexOf(")", fileEnd));
				final int line = Integer.parseInt(ranges.substring(0, ranges.indexOf(",")));
				final int column = Integer.parseInt(ranges.substring(ranges.indexOf(",") + 2, ranges.length()));
				final String message = str.substring(str.indexOf(")", fileEnd) + 2, str.length());

				final AntBuildMessageView.MessageType c = category;
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					public void run()
					{
						VirtualFile file = VirtualFileManager.getInstance().findFileByUrl("file://" + url);
						messageView.outputJavacMessage(c, new String[]{message}, file, null, line, column);
					}
				});
			}
			catch(Exception e)
			{
				messageView.outputMessage(str, AntBuildMessageView.PRIORITY_VERBOSE);
				//throw new UnsupportedOperationException("Unknown how parse string : " + str, e);
			}
		}
	}

	private static void processJavacMessages(@NotNull final List<String> javacMessages, final AntBuildMessageView messageView, Project project)
	{
		com.intellij.compiler.OutputParser outputParser = new JavacOutputParser(project);

		com.intellij.compiler.OutputParser.Callback callback = new com.intellij.compiler.OutputParser.Callback()
		{
			private int myIndex = -1;

			@Nullable
			public String getCurrentLine()
			{
				if(myIndex >= javacMessages.size())
				{
					return null;
				}
				return javacMessages.get(myIndex);
			}

			public String getNextLine()
			{
				final int size = javacMessages.size();
				final int next = Math.min(myIndex + 1, javacMessages.size());
				myIndex = next;
				if(next >= size)
				{
					return null;
				}
				return javacMessages.get(next);
			}

			@Override
			public void pushBack(String line)
			{
				myIndex--;
			}

			public void message(final CompilerMessageCategory category, final String message, final String url, final int lineNum, final int columnNum)
			{
				StringTokenizer tokenizer = new StringTokenizer(message, "\n", false);
				final String[] strings = new String[tokenizer.countTokens()];
				//noinspection ForLoopThatDoesntUseLoopVariable
				for(int idx = 0; tokenizer.hasMoreTokens(); idx++)
				{
					strings[idx] = tokenizer.nextToken();
				}
				ApplicationManager.getApplication().runReadAction(new Runnable()
				{
					public void run()
					{
						VirtualFile file = url == null ? null : VirtualFileManager.getInstance().findFileByUrl(url);
						messageView.outputJavacMessage(convertCategory(category), strings, file, url, lineNum, columnNum);
					}
				});
			}

			public void setProgressText(String text)
			{
			}

			public void fileProcessed(String path)
			{
			}

			public void fileGenerated(FileObject path)
			{
			}
		};
		try
		{
			while(true)
			{
				if(!outputParser.processMessageLine(callback))
				{
					break;
				}
			}
		}
		catch(Exception e)
		{
			//ignore
		}
	}

	private static AntBuildMessageView.MessageType convertCategory(CompilerMessageCategory category)
	{
		if(CompilerMessageCategory.ERROR.equals(category))
		{
			return AntBuildMessageView.MessageType.ERROR;
		}
		return AntBuildMessageView.MessageType.MESSAGE;
	}

}
