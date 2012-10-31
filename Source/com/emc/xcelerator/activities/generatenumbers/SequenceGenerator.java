// ***************************************************************************
// - - - - - - - - - - - - - D I S C L A I M E R - - - - - - - - - - - - - - -
// By accepting this software ("Software"), you ("Customer") agree that
// use of the Software is subject to the terms and conditions of the
// software license agreement entered into between you and EMC,
// except that (i) the Software is supplied on a strict "AS IS" basis,
// without warranty of any kind or nature.  EMC DISCLAIMS ANY AND
// ALL EXPRESS OR IMPLIED WARRANTIES RELATIVE TO THE SOFTWARE, INCLUDING,
// WITHOUT LIMITATION, ANY IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
// FOR A PARTICULAR PURPOSE, and (ii) in no event shall EMC be liable
// to Customer for any damages, whether direct, indirect, special,
// incidental, consequential or punitive, which in any way arise out of or
// relate to the Software, and (iii) EMC shall not provide support of
// any kind for the Software. EMC retains title to all portions of
// the Software and any copies thereof.
//
// This Software is not covered or supported under your software maintenance agreement.  
// Do not contact EMC Technical Support or Consulting with
// questions regarding the use or operation of this code.  
// ****************************************************************************


package com.emc.xcelerator.activities.generatenumbers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * The SequenceGenerator interface enables different generation strategies to be implemented.  
 *
 * Copyright (c) 1994 - 2010. EMC Corporation. All Rights Reserved.
 */
public interface SequenceGenerator {

	/**
	 * Generates a new number in a specified sequence. 
	 * 
	 * @param session the repository session
	 * @param numberSequenceName The name of the number sequence
	 * @return a new number in the specified sequence
	 * @throws DfException if an internal error occurred. 
	 * @throws SequenceObjectNotFoundException if the number sequence couldn't be found.
	 * @throws SequenceGeneratorFailedException if a new number couldn't be generated.
	 */
	public abstract String getNextGeneratedNumber(final IDfSession session, final String numberSequenceName) throws DfException,
			SequenceObjectNotFoundException, SequenceGeneratorFailedException;

}