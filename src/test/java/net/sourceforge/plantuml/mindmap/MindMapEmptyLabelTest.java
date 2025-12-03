package net.sourceforge.plantuml.mindmap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

/*
 * Test for mindmap with empty labels
 * 
 * @startmindmap
 * * Main Idea
 * ** Not empty
 * ** 
 * @endmindmap
 */
public class MindMapEmptyLabelTest {

	protected File getJavaFile() {
		final String name = getClass().getName();
		final File f = new File("src/test/java/" + name.replace('.', '/') + ".java");
		return f;
	}

	@Test
	public void testEmptyLabel() throws IOException, InterruptedException {
		final File file = getJavaFile();
		final FileFormatOption options = new FileFormatOption(FileFormat.PNG);

		final File outputDirectory = new File("build/test-output").getAbsoluteFile();
		outputDirectory.mkdirs();

		final SourceFileReader reader = new SourceFileReader(Defines.createWithFileName(file), file, outputDirectory,
				Collections.<String>emptyList(), "UTF-8", options);
		final List<GeneratedImage> list = reader.getGeneratedImages();

		assertEquals(1, list.size());
	}

}
