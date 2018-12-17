/**
 * <h2>Copyright</h2> © 2018 Alfred Differ.<br>
 * ------------------------------------------------------------------------ <br>
 * ---com.interworldtransport.cladosviewerEvents.ToolsEvents<br>
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
 * ---com.interworldtransport.cladosviewerEvents.ToolsEvents<br>
 * ------------------------------------------------------------------------ <br>
 */

package com.interworldtransport.cladosviewerEvents;

import com.interworldtransport.cladosGExceptions.*;
import com.interworldtransport.cladosviewer.CladosCalculator;
import com.interworldtransport.cladosviewer.ViewerMenu;
import com.interworldtransport.cladosviewerExceptions.UtilitiesException;

import java.awt.event.*;

/** com.interworldtransport.cladosviewer.ToolsEvents
 * This class groups the event listeners associated with the Tools menu.
 * It may be used in the future to act on events associated with the entire Tools menu
 * by having it register as a Listeners with all of its controlled listeners.  The controlled
 * listeners will create an event or call their parent.  It could also register all the
 * components to which its listeners register....maybe....
 *
 * @version 0.85
 * @author Dr Alfred W Differ
 */
public class ToolsEvents implements ActionListener
{
    protected ToolsCreate			cr;
    protected ToolsOptions			op;
    protected ViewerMenu 			_GUIMenu;
    protected CladosCalculator		_GUI;

/** 
 * This is the default constructor.  The event structure of the Tools
 * menu starts here and finishes with the child menu items.
 */
    public ToolsEvents(ViewerMenu pTheGUIMenu)
        	throws 		UtilitiesException, BadSignatureException
    {
    	_GUIMenu=pTheGUIMenu;
		_GUI=_GUIMenu._parentGUI;
		cr = new ToolsCreate(	_GUIMenu.mniCreate, this);
		op = new ToolsOptions(	_GUIMenu.mniOptions, this);
    }

/** 
 * This is the default action to be performed by all members of the Tools menu.
 * It will be overridden by specific members of the menu.
 */
    public void actionPerformed(ActionEvent evt)
    {
    	;
    }
}