/**
 * <h2>Copyright</h2> © 2018 Alfred Differ.<br>
 * ------------------------------------------------------------------------ <br>
 * ---com.interworldtransport.cladosviewerEvents.BOpsMIdempotentEvents<br>
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
 * ---com.interworldtransport.cladosviewerEvents.BOpsMIdempotentEvents<br>
 * ------------------------------------------------------------------------ <br>
 */

package com.interworldtransport.cladosviewerEvents;
import com.interworldtransport.cladosG.MonadRealF;
import com.interworldtransport.cladosGExceptions.*;
import com.interworldtransport.cladosviewer.NyadPanel;
import com.interworldtransport.cladosFExceptions.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 *  This class manages events relating to the answering of a boolean question.
 *  Is the selected monad a multiple of an idempotent?
 *
 * @version 0.85
 * @author Dr Alfred W Differ
 */
public class BOpsMIdempotentEvents implements ActionListener
 {
    protected JMenuItem 		_control;
    protected BOpsEvents 		_parent;

/** 
 * This is the default constructor.
 */
    public BOpsMIdempotentEvents(	JMenuItem pmniControlled,
									BOpsEvents pParent)
    {
		_control=pmniControlled;
		_control.addActionListener(this);
		_parent=pParent;
    }

/** 
 * This is the actual action to be performed by this member of the menu.
 */
    public void actionPerformed(ActionEvent evt)
    {
    	if (_parent._GUI._GeometryDisplay.getPaneFocus()<0) return;
    	NyadPanel panelNyadSelected=_parent._GUI._GeometryDisplay.getNyadPanel(_parent._GUI._GeometryDisplay.getPaneFocus());
    	MonadRealF monadSelected = panelNyadSelected.getMonadPanel(panelNyadSelected.getPaneFocus()).getMonad();
    	
		boolean test=false;
		try
		{
			test=MonadRealF.isIdempotentMultiple(monadSelected);
		}
		catch (CladosMonadException e)
		{
			_parent._GUI._StatusBar.setStatusMsg("\t\tselected monad created a CladosMonadException.\n");
			e.printStackTrace();
		}
		catch (FieldBinaryException eb)
		{
			_parent._GUI._StatusBar.setStatusMsg("\t\tselected monad created a FieldBinaryException.\n");
			eb.printStackTrace();
		}
		catch (FieldException ef)
		{
			_parent._GUI._StatusBar.setStatusMsg("\t\tselected monad created a FieldException.\n");
			ef.printStackTrace();
		}
		
		if (test)
			_parent._GUI._StatusBar.setStatusMsg("\tselected monad is idempotent multiple.\n");
		else
			_parent._GUI._StatusBar.setStatusMsg("\tselected monad is NOT idempotent multiple.\n");	
    }
}