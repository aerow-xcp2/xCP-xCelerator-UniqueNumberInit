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

package com.emc.xcp.xcelerator.activities.generatenumbers;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * Convenience class which abstracts away the details of the number sequence
 * object implementation.
 * 
 * Copyright (c) 1994 - 2010. EMC Corporation. All Rights Reserved.
 */
public class NumberSequenceObject {

	private static final String	CURRENT_VALUE		= "current_value";
	private static final String	INCREMENT_AMOUNT	= "increment_amount";
	private IDfPersistentObject	object;

	/**
	 * Creates a new NumberSequenceObject around the specified
	 * IDfPersistentObject which represents a number sequence.
	 * 
	 * @param object
	 *            the IDfPersistent object representing the number sequence.
	 */
	public NumberSequenceObject(final IDfPersistentObject object) {
		this.object = object;
	}

	/**
	 * Retrieves the increment amount.
	 * 
	 * @return the increment amount.
	 * @throws DfException
	 *             if an internal error occurred
	 */
	public int getIncrementAmount() throws DfException {
		return object.getInt(INCREMENT_AMOUNT);
	}

	/**
	 * Retrieves the current value.
	 * 
	 * @return the current value.
	 * @throws DfException
	 *             if an internal error occurred
	 */
	public int getCurrentValue() throws DfException {
		final String valueStr = object.getString(CURRENT_VALUE);
		final int value = Integer.parseInt(valueStr);
		return value;
	}

	/**
	 * Sets the current value to the specified amount.
	 * 
	 * <b>Note:</b>save must be called after setCurrentValue to persist the new
	 * value.
	 * 
	 * @param currentValue
	 *            the new currentValue.
	 * @throws DfException
	 *             if an internal error occurred.
	 */
	public void setCurrentValue(final int currentValue) throws DfException {
		final String value = String.valueOf(currentValue);
		object.setString(CURRENT_VALUE, value);
	}

	/**
	 * Persist the changes performed to the repository.
	 * 
	 * @throws DfException
	 *             if an internal error occurred.
	 */
	public void save() throws DfException {
		object.save();
	}

	/**
	 * Retrieves the underlying IDfPersistentObject representing the number
	 * sequence.
	 * 
	 * @return the IDFPersistentObject representing the number sequence.
	 */
	public IDfPersistentObject getObject() {
		return object;
	}

	/**
	 * Refreshes the underlying IDfPersistentObject.
	 * 
	 * @param session
	 *            the repository session
	 * @throws DfException
	 *             if an internal error occurred.
	 */
	public void refresh(final IDfSession session) throws DfException {
		object = session.getObject(object.getObjectId());
	}

	/**
	 * Retrieves the current value while also creating the next value by
	 * incrementing the current value and persisting it.
	 * 
	 * @return the next value
	 * @throws DfException if an internal error occurred. 
	 */
	public String getAndIncrement() throws DfException {
		final int incrementValue = getIncrementAmount();
		int currentValue = getCurrentValue();
		final String retVal = String.valueOf(currentValue);
		currentValue += incrementValue;
		setCurrentValue(currentValue);
		save();
		return retVal;
	}

	/**
	 * Sets the increment amount to the specified amount.
	 * 
	 * <b>Note:</b>save must be called after setIncrementAmount to persist the new
	 * value.
	 * 
	 * @param incrementAmount
	 *            the new incrementAmount.
	 * @throws DfException
	 *             if an internal error occurred.
	 */	
	public void setIncrementAmount(int incrementAmount) throws DfException {
		object.setInt(INCREMENT_AMOUNT, incrementAmount);
	}
}
