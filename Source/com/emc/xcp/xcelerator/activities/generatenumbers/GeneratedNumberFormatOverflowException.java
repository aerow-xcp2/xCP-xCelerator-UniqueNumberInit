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


/**
 * Signals that the GeneratedNumberFormat couldn't handle the number it was going to format. 
 * 
 * Copyright (c) 1994 - 2010. EMC Corporation. All Rights Reserved.
 */
public class GeneratedNumberFormatOverflowException extends RuntimeException {

	private static final long	serialVersionUID	= -7613465958180061255L;

	/**
	 * Constructs an instance of GeneratedNumberFormatOverflowException with null as
	 * its detail message and no cause.
	 */
	public GeneratedNumberFormatOverflowException() {
		// NOP.
	}

	/**
	 * Constructs an instance of GeneratedNumberFormatOverflowException with the
	 * specified detail message. A detail message is an instance of String that
	 * describes this particular exception.
	 * 
	 * @param message
	 *            the detail message
	 */
	public GeneratedNumberFormatOverflowException(final String message) {
		super(message);
	}

	/**
	 * Constructs an instance of GeneratedNumberFormatOverflowException with the
	 * specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause
	 */
	public GeneratedNumberFormatOverflowException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an instance of GeneratedNumberFormatOverflowException with the
	 * specified cause.
	 * 
	 * @param cause
	 *            the cause
	 */
	public GeneratedNumberFormatOverflowException(final Throwable cause) {
		super(cause);
	}
}
