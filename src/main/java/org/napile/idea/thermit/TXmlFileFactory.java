package org.napile.idea.thermit;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;

/**
 * @author VISTALL
 * @date 11:30/19.10.12
 */
public class TXmlFileFactory extends FileTypeFactory
{
	@Override
	public void createFileTypes(@NotNull FileTypeConsumer consumer)
	{
		consumer.consume(XmlFileType.INSTANCE, "txml");
	}
}
