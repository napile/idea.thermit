package org.napile.idea.thermit.config.execution;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.extensions.ExtensionPointName;

public interface AntMessageCustomizer
{

	public static final ExtensionPointName<AntMessageCustomizer> EP_NAME = ExtensionPointName.create("org.napile.idea.thermit.messageCustomizer");

	@Nullable
	AntMessage createCustomizedMessage(String text, int priority);
}
