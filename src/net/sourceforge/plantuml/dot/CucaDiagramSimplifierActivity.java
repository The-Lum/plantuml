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
package net.sourceforge.plantuml.dot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import net.atmp.CucaDiagram;
import net.sourceforge.plantuml.abel.Entity;
import net.sourceforge.plantuml.abel.LeafType;
import net.sourceforge.plantuml.klimt.font.StringBounder;
import net.sourceforge.plantuml.svek.DotMode;
import net.sourceforge.plantuml.svek.GroupMakerActivity;
import net.sourceforge.plantuml.svek.IEntityImage;

public final class CucaDiagramSimplifierActivity {

	public void simplify(CucaDiagram diagram, StringBounder stringBounder, DotMode dotMode)
			throws IOException, InterruptedException {
		boolean changed;
		do {
			changed = false;
			final Collection<Entity> groups = new ArrayList<>(diagram.groups());
			for (Entity g : groups)
				if (g.isAutarkic()) {
					final GroupMakerActivity maker = new GroupMakerActivity(diagram, g, stringBounder, dotMode);
					final IEntityImage img = maker.getImage();
					g.overrideImage(img, LeafType.ACTIVITY);

					changed = true;
				}

		} while (changed);
	}

}
