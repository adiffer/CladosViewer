/**
 * <h2>Copyright</h2> © 2020 Alfred Differ.<br>
 * ------------------------------------------------------------------------ <br>
 * ---org.interworldtransport.cladosviewer.MOpsScaledIdempotentEvents<br>
 * -------------------------------------------------------------------- <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.<p>
 * 
 * Use of this code or executable objects derived from it by the Licensee 
 * states their willingness to accept the terms of the license. <p> 
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.<p> 
 * 
 * ------------------------------------------------------------------------ <br>
 * ---org.interworldtransport.cladosviewer.MOpsScaledIdempotentEvents<br>
 * ------------------------------------------------------------------------ <br>
 */

package org.interworldtransport.cladosviewerEvents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

import org.interworldtransport.cladosFExceptions.FieldBinaryException;
import org.interworldtransport.cladosFExceptions.FieldException;
import org.interworldtransport.cladosG.Monad;
import org.interworldtransport.cladosviewer.ErrorDialog;
import org.interworldtransport.cladosviewer.MonadPanel;
import org.interworldtransport.cladosviewer.NyadPanel;

/**
 * This class manages events relating to the answering of a boolean question. Is
 * the selected monad a multiple of an idempotent?
 *
 * @version 0.85
 * @author Dr Alfred W Differ
 */
public class MOpsScaledIdempotentEvents implements ActionListener {
	protected JMenuItem _control;
	protected MOpsParentEvents _parent;

	/**
	 * This is the default constructor.
	 * 
	 * @param pmniControlled JMenuItem This is a reference to the Menu Item for
	 *                       which this event acts.
	 * @param pParent        NOpsParentEvents This is a reference to the
	 *                       NOpsParentEvents parent event handler
	 */
	public MOpsScaledIdempotentEvents(JMenuItem pmniControlled, MOpsParentEvents pParent) {
		_control = pmniControlled;
		_control.addActionListener(this);
		_parent = pParent;
	}

	/**
	 * This is the actual action to be performed by this member of the menu.
	 */
	@Override
	public void actionPerformed(ActionEvent evt) {
		int indexNyadPanelSelected = _parent._GUI.appGeometryView.getPaneFocus();
		if (indexNyadPanelSelected < 0) {
			ErrorDialog.show("No nyad in the focus.\nNothing done.", "Need Nyad In Focus");
			return;
		}

		NyadPanel panelNyadSelected = _parent._GUI.appGeometryView.getNyadPanel(indexNyadPanelSelected);
		int indxMndPnlSlctd = panelNyadSelected.getPaneFocus();
		if (indxMndPnlSlctd < 0) {
			ErrorDialog.show("Scaled Idempotent Test needs one monad in focus.\nNothing done.", "Need Monad In Focus");
			return;
		}

		MonadPanel tSpot = panelNyadSelected.getMonadPanel(indxMndPnlSlctd);
		boolean test = false;
		try {
			test = Monad.isScaledIdempotent(tSpot.getMonad());
			if (test)
				_parent._GUI.appStatusBar.setStatusMsg("-->Selected monad is multiple of an idempotent.\n");
			else
				_parent._GUI.appStatusBar.setStatusMsg("-->Selected monad is NOT multiple of an idempotent.\n");
		} catch (FieldBinaryException eb) {
			ErrorDialog.show("Selected monad has an issue.\nNothing done.\n" + eb.getSourceMessage(),
					"Field Binary Exception");
		} catch (FieldException ef) {
			ErrorDialog.show("Selected monad has an issue.\nNothing done.\n" + ef.getSourceMessage(),
					"Field Exception");
		}
	}
}