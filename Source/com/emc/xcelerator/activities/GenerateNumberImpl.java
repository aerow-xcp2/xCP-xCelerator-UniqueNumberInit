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

package com.emc.xcelerator.activities;

import com.documentum.fc.client.DfSingleDocbaseModule;
import com.documentum.fc.client.IDfModule;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.emc.xcelerator.activities.generatenumbers.GeneratedNumberFormat;
import com.emc.xcelerator.activities.generatenumbers.OutOfTransactionDecoratorSequenceGenerator;
import com.emc.xcelerator.activities.generatenumbers.RetrySequenceGenerator;
import com.emc.xcelerator.activities.generatenumbers.SequenceGenerator;
import com.emc.xcelerator.activities.generatenumbers.SequenceGeneratorFailedException;
import com.emc.xcelerator.activities.generatenumbers.SequenceObjectNotFoundException;

/**
 * The implementation part of the GenerateNumber module.
 * 
 * Copyright (c) 1994 - 2010. EMC Corporation. All Rights Reserved.
 */
public class GenerateNumberImpl extends DfSingleDocbaseModule implements IDfModule {

	/**
	 * @see com.emc.xcelerator.activities.GenerateNumber#generateNumber(java.lang.String,
	 *      java.lang.String, java.lang.String[], java.lang.String[])
	 */
	public String generateNumber(final String numberSequenceName, final String format, final String[] prefix, final String[] suffix) {
		if(DfLogger.isDebugEnabled(this)) {
			DfLogger.info(GenerateNumberImpl.class, "GenerateNumberModule, build {0}.",
					new Object[] { GenerateNumberImpl.class.getPackage().getImplementationVersion() }, null);			
		}
		
		try {
			final int number = getNextNumber(numberSequenceName);
			final String formattedNumber = formatNumber(number, format);
			final StringBuilder buffer = new StringBuilder();
			addStringsToBuffer(prefix, buffer);
			buffer.append(formattedNumber);
			addStringsToBuffer(suffix, buffer);
			return buffer.toString();
		} catch (final DfException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Formats a number according to the format specification.
	 * 
	 * @param number
	 *            the number to format
	 * @param format
	 *            the format specification
	 * @return The formatted number.
	 */
	protected String formatNumber(final int number, final String format) {
		if (format == null || format.length() == 0) {
			return String.valueOf(number);
		} else {
			final GeneratedNumberFormat fmt = new GeneratedNumberFormat(format);
			return fmt.apply(number);
		}
	}

	/**
	 * Retrieves the next number in a specified number sequence.
	 * 
	 * @param sequenceName
	 *            The name of the number sequence.
	 * @return The next value in the number sequence.
	 * @throws DfException
	 *             if an internal error occurred.
	 * @throws SequenceObjectNotFoundException
	 *             if the specified number sequence couldn't be found.
	 * @throws SequenceGeneratorFailedException
	 *             if the number generation failed.
	 */
	protected int getNextNumber(final String sequenceName) throws DfException, SequenceObjectNotFoundException,
			SequenceGeneratorFailedException {
		final IDfSession session = getSession();
		try {
			final SequenceGenerator seqGen = getSequenceGenerator();
			final String value = seqGen.getNextGeneratedNumber(session, sequenceName);
			return Integer.parseInt(value);
		} finally {
			if(session != null) {
				releaseSession(session);
			}
		}
	}


	private OutOfTransactionDecoratorSequenceGenerator getSequenceGenerator() {
		return new OutOfTransactionDecoratorSequenceGenerator(new RetrySequenceGenerator());
	}

	/**
	 * Adds an array of strings (potentially null) to a StringBuilder instance.
	 * 
	 * @param strings
	 *            The strings to add, if any.
	 * @param buffer
	 *            The buffer to add the strings to.
	 */
	protected void addStringsToBuffer(final String[] strings, final StringBuilder buffer) {
		if (strings == null || strings.length == 0) {
			return;
		} else {
			for (final String string : strings) {
				buffer.append(string);
			}
		}
	}
	

	/**
	 * @see com.emc.xcelerator.activities.GenerateNumber#generateNumber(java.lang.String)
	 */
	public String generateNumber(String numberSequenceName) {
		return generateNumber(numberSequenceName,null,null,null);
	}

	
	/**
	 * @see com.emc.xcelerator.activities.GenerateNumber#generateNumber(java.lang.String, java.lang.String[], java.lang.String[])
	 */
	public String generateNumber(String numberSequenceName, String[] prefix, String[] suffix) {
		return generateNumber(numberSequenceName,null,prefix,suffix);
	}

	/**
	 * @see com.emc.xcelerator.activities.GenerateNumber#generateNumber(java.lang.String, java.lang.String)
	 */
	public String generateNumber(String numberSequenceName, String numberFormatPattern) {
		return generateNumber(numberSequenceName,numberFormatPattern,null,null);
	}


}
