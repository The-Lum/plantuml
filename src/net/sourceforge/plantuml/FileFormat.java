/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2024, Arnaud Roques
 *
 * Project Info:  https://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * https://plantuml.com/patreon (only 1$ per month!)
 * https://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sourceforge.plantuml.braille.BrailleCharFactory;
import net.sourceforge.plantuml.braille.UGraphicBraille;
import net.sourceforge.plantuml.klimt.drawing.debug.StringBounderDebug;
import net.sourceforge.plantuml.klimt.drawing.svg.SvgGraphics;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.klimt.font.StringBounderRaw;
import net.sourceforge.plantuml.klimt.font.UFont;
import net.sourceforge.plantuml.klimt.font.UFontContext;
import net.sourceforge.plantuml.klimt.geom.XDimension2D;
import net.sourceforge.plantuml.log.Logme;
import net.sourceforge.plantuml.png.MetadataTag;
import net.sourceforge.plantuml.security.SFile;
import net.sourceforge.plantuml.text.SvgCharSizeHack;

/**
 * Format for output files generated by PlantUML.
 * 
 * @author Arnaud Roques
 * 
 */
public enum FileFormat {
	// ::remove file when __HAXE__
	// ::comment when __CORE__
	EPS("application/postscript"), //
	EPS_TEXT("application/postscript"), //
	ATXT("text/plain"), //
	UTXT("text/plain;charset=UTF-8"), //
	XMI_STANDARD("application/vnd.xmi+xml"), //
	XMI_STAR("application/vnd.xmi+xml"), //
	XMI_ARGO("application/vnd.xmi+xml"), //
	XMI_SCRIPT("application/vnd.xmi+xml"), //
	SCXML("application/scxml+xml"), //
	GRAPHML("application/graphml+xml"), //
	PDF("application/pdf"), //
	HTML("text/html"), //
	HTML5("text/html"), //
	VDX("application/vnd.visio.xml"), //
	LATEX("application/x-latex"), //
	LATEX_NO_PREAMBLE("application/x-latex"), //
	BASE64("text/plain; charset=x-user-defined"), //
	BRAILLE_PNG("image/png"), //
	PREPROC("text/plain"), //
	DEBUG("text/plain"), //
	// ::done
	PNG("image/png"), //
	RAW("image/raw"), //
	SVG("image/svg+xml"); //

	private final String mimeType;

	FileFormat(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Returns the file format to be used for that format.
	 * 
	 * @return a string starting by a point.
	 */
	public String getFileSuffix() {
		// ::comment when __CORE__
		if (name().startsWith("XMI"))
			return ".xmi";

		if (this == LATEX || this == LATEX_NO_PREAMBLE)
			return ".tex";

		if (this == BRAILLE_PNG)
			return ".braille.png";

		if (this == EPS_TEXT)
			return EPS.getFileSuffix();
		// ::done

		return "." + StringUtils.goLowerCase(name());
	}

	final static private BufferedImage imDummy = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
	final static public Graphics2D gg = imDummy.createGraphics();
	static {
		gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		gg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
	}

	public StringBounder getDefaultStringBounder() {
		return getDefaultStringBounder(TikzFontDistortion.getDefault(), SvgCharSizeHack.NO_HACK);
	}

	public StringBounder getDefaultStringBounder(TikzFontDistortion tikzFontDistortion, SvgCharSizeHack charSizeHack) {
		// ::comment when __CORE__
		if (this == LATEX || this == LATEX_NO_PREAMBLE)
			return getTikzStringBounder(tikzFontDistortion);

		if (this == BRAILLE_PNG)
			return getBrailleStringBounder();

		if (this == DEBUG)
			return new StringBounderDebug();
		// ::done

		if (this == SVG)
			return getSvgStringBounder(charSizeHack);

		return getNormalStringBounder();
	}

	private StringBounder getSvgStringBounder(final SvgCharSizeHack charSizeHack) {
		return new StringBounderRaw(FileFormat.gg.getFontRenderContext()) {
			public String toString() {
				return "FileFormat::getSvgStringBounder";
			}

			protected XDimension2D calculateDimensionInternal(UFont font, String text) {
				text = charSizeHack.transformStringForSizeHack(text);
				return getJavaDimension(font, text);
			}

			public boolean matchesProperty(String propertyName) {
				return "SVG".equalsIgnoreCase(propertyName);
			}

		};
	}

	private StringBounder getNormalStringBounder() {
		return new StringBounderRaw(FileFormat.gg.getFontRenderContext()) {
			public String toString() {
				return "FileFormat::getNormalStringBounder";
			}

			protected XDimension2D calculateDimensionInternal(UFont font, String text) {
				return getJavaDimension(font, text);
			}

			public boolean matchesProperty(String propertyName) {
				return false;
			}

		};
	}

	static private XDimension2D getJavaDimension(UFont font, String text) {
		final Font javaFont = font.getUnderlayingFont(UFontContext.G2D);
		final FontMetrics fm = gg.getFontMetrics(javaFont);
		final Rectangle2D rect = fm.getStringBounds(text, gg);
		return new XDimension2D(rect.getWidth(), rect.getHeight());
	}

	// ::comment when __CORE__
	private StringBounder getBrailleStringBounder() {
		return new StringBounderRaw(FileFormat.gg.getFontRenderContext()) {
			public String toString() {
				return "FileFormat::getBrailleStringBounder";
			}

			protected XDimension2D calculateDimensionInternal(UFont font, String text) {
				final int nb = BrailleCharFactory.build(text).size();
				final double quanta = UGraphicBraille.QUANTA;
				final double height = 5 * quanta;
				final double width = 3 * nb * quanta + 1;
				return new XDimension2D(width, height);
			}

			@Override
			public double getDescent(UFont font, String text) {
				return UGraphicBraille.QUANTA;
			}

			public boolean matchesProperty(String propertyName) {
				return false;
			}

		};
	}

	private StringBounder getTikzStringBounder(final TikzFontDistortion tikzFontDistortion) {
		return new StringBounderRaw(FileFormat.gg.getFontRenderContext()) {

			private final LatexManager latexManager = new LatexManager(tikzFontDistortion.getTexSystem(), tikzFontDistortion.getTexPreamble());

			public String toString() {
				return "FileFormat::getTikzStringBounder";
			}

			protected XDimension2D calculateDimensionInternal(UFont font, String text) {
				double[] widthHeightDepth = latexManager.getWidthHeightDepth(styleText(font, text));
				return new XDimension2D(widthHeightDepth[0], widthHeightDepth[1] + widthHeightDepth[2]);
			}

			public boolean matchesProperty(String propertyName) {
				return "TIKZ".equalsIgnoreCase(propertyName);
			}

			public double getDescent(UFont font, String text) {
				double[] widthHeightDepth = latexManager.getWidthHeightDepth(styleText(font, text));
				return widthHeightDepth[2];
			}

			protected String styleText(UFont font, String text) {
				if (font == null) {
					return "$" + text + "$";
				}
				StringBuilder sb = new StringBuilder();
				final boolean italic = font.isItalic();
				final boolean bold = font.isBold();

				if (italic)
					sb.append("\\textit{");

				if (bold)
					sb.append("\\textbf{");

				sb.append(LatexManager.protectText(text));
				if (bold)
					sb.append("}");

				if (italic)
					sb.append("}");

				return sb.toString();
			}
		};
	}

	/**
	 * Check if this file format is Encapsulated PostScript.
	 * 
	 * @return <code>true</code> for EPS.
	 */
	public boolean isEps() {
		if (this == EPS)
			return true;

		if (this == EPS_TEXT)
			return true;

		return false;
	}

	public String changeName(String fileName, int cpt) {
		if (cpt == 0)
			return changeName(fileName, getFileSuffix());

		return changeName(fileName,
				OptionFlags.getInstance().getFileSeparator() + String.format("%03d", cpt) + getFileSuffix());
	}

//	private SFile computeFilename(SFile pngFile, int i) {
//		if (i == 0)
//			return pngFile;
//
//		final SFile dir = pngFile.getParentFile();
//		return dir.file(computeFilenameInternal(pngFile.getName(), i));
//	}

	private String changeName(String fileName, String replacement) {
		String result = fileName.replaceAll("\\.\\w+$", replacement);
		if (result.equals(fileName))
			result = fileName + replacement;

		return result;
	}

//	private String computeFilenameInternal(String name, int i) {
//		if (i == 0)
//			return name;
//
//		return name.replaceAll("\\" + getFileSuffix() + "$",
//				OptionFlags.getInstance().getFileSeparator() + String.format("%03d", i) + getFileSuffix());
//	}

	public boolean doesSupportMetadata() {
		return this == PNG || this == SVG;
	}

	public boolean equalsMetadata(String currentMetadata, SFile existingFile) {
		try {
			if (this == PNG) {
				final MetadataTag tag = new MetadataTag(existingFile, "plantuml");
				final String previousMetadata = tag.getData();
				final boolean sameMetadata = currentMetadata.equals(previousMetadata);
				return sameMetadata;
			}
			if (this == SVG) {
				final String svg = FileUtils.readSvg(existingFile);
				if (svg == null)
					return false;

				final String currentSignature = SvgGraphics.getMetadataHex(currentMetadata);
				final int idx = svg.lastIndexOf(SvgGraphics.META_HEADER);
				if (idx != -1) {
					final String part = svg.substring(idx + SvgGraphics.META_HEADER.length());
					return part.startsWith(currentSignature + "]");
				}

			}
		} catch (IOException e) {
			Logme.error(e);
		}
		return false;
	}
	// ::done

}
