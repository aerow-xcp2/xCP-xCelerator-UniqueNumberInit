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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A CaseNumberFormat is specified by:
 * <ul>
 * <li>A # (hash) character is replaced by the digit in that position or nothing
 * if no such digit exists.</li>
 * <li>A 0 (zero) character is replaced by the digit in that position or a 0 if
 * no such digit exists.</li>
 * <li>A \ (backslash) character is used to escape any of the above characters,
 * so \# will yield # etc.</li>
 * <li>A ? (question mark) character followed by any character is equivalent to
 * 0 but instead of producing a 0 the second character is produced. I.e. ?0 is
 * the same as 0.
 * <li>Any other characters is replaced with itself unless it occurs after a #
 * character and before a # character which would be replaced with nothing.</li>
 * </ul>
 * In the table below are a few examples:
 * <table>
 * <tr>
 * <th>Number</th>
 * <th>Pattern</th>
 * <th>Result</th>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>###-###-#</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>000-000-0</td>
 * <td>000-000-1</td>
 * </tr>
 * <tr>
 * <td>12</td>
 * <td>###-###-#</td>
 * <td>1-2</td>
 * </tr>
 * <tr>
 * <td>12</td>
 * <td>000-000-0</td>
 * <td>000-001-2</td>
 * </tr>
 * <tr>
 * <td>1234</td>
 * <td>###-###-#</td>
 * <td>123-4</td>
 * </tr>
 * <tr>
 * <td>1234</td>
 * <td>000-000-0</td>
 * <td>000-123-4</td>
 * </tr>
 * <tr>
 * <td>1234</td>
 * <td>The number is ###-###-#</td>
 * <td>The number is 123-4</td>
 * </tr>
 * <tr>
 * <td>1234</td>
 * <td>The number is 000-000-0</td>
 * <td>The number is 000-123-4</td>
 * </tr>
 * </table>
 * 
 * Copyright (c) 1994 - 2010. EMC Corporation. All Rights Reserved.
 **/
public class GeneratedNumberFormat {

	private static final char			ESCAPE					= '\\';
	private static final char			NUMBER_OR_ZERO			= '0';
	private static final char			NUMBER_OR_PAD			= '?';
	private static final char			NUMBER_OR_NOTHING		= '#';
	private static final MessageFormat	ERROR_FORMAT_OVERFLOW	= new MessageFormat(
																		"The CaseNumberFormat ''{0}'' can''t handle the number ''{1}''.");

	private Collection<FormatPart>		formatSpecification;
	private int							digitPositions;
	private final String				formatPattern;

	/**
	 * Creates a new CaseNumberFormat object, as specified by the format pattern.  
	 * 
	 * @param formatPattern the format pattern
	 */
	public GeneratedNumberFormat(final String formatPattern) {
		this.formatPattern = formatPattern;
		parseFormat(formatPattern);
	}

	/**
	 * Applies this CaseNumberFormat to the specified number producing a formatted number. 
	 * 
	 * @param number the number to format
	 * @return the formatted number
	 * @throws GeneratedNumberFormatOverflowException if the pattern cannot cope with the number (i.e. too many digits). 
	 */
	public String apply(final int number) throws GeneratedNumberFormatOverflowException {
		final FormatContext ctx = new FormatContext(number);

		if (ctx.numberLength > getDigitPositions()) {
			throw new GeneratedNumberFormatOverflowException(getErrorFormatOverflow(getFormatPattern(), number));
		}

		for (final FormatPart opt : formatSpecification) {
			opt.apply(ctx);
		}
		return ctx.buffer.toString();
	}

	/**
	 * Parses the specified format pattern and initializes this CaseNumberFormat object accordingly. 
	 * 
	 * @param format the format pattern
	 */
	private void parseFormat(final String format) {
		formatSpecification = new ArrayList<FormatPart>();
		digitPositions = 0;
		final int len = format.length();
		final StringBuilder buffer = new StringBuilder();
		boolean escape = false;
		boolean nextIsPadding = false;
		boolean hasSeenNumberOrNothing = false;
		for (int i = 0; i < len; ++i) {
			final char c = format.charAt(i);
			if (escape) {
				buffer.append(c);
				escape = false;
			} else if (nextIsPadding) {
				addConstantPart(buffer, hasSeenNumberOrNothing);
				formatSpecification.add(new DigitFormatPart(digitPositions, true, c));
				digitPositions += 1;
				nextIsPadding = false;
			} else {
				switch (c) {
					case ESCAPE:
						escape = true;
						break;
					case NUMBER_OR_PAD:
						nextIsPadding = true;
						break;
					case NUMBER_OR_NOTHING:
					case NUMBER_OR_ZERO:
						addConstantPart(buffer, hasSeenNumberOrNothing);
						if (c == NUMBER_OR_NOTHING) {
							formatSpecification.add(new DigitFormatPart(digitPositions));
							hasSeenNumberOrNothing = true;
						} else {
							formatSpecification.add(new DigitFormatPart(digitPositions, true, '0'));
						}
						digitPositions += 1;
						break;
					default:
						buffer.append(c);
				}
			}
		}

		if (buffer.length() > 0) {
			formatSpecification.add(new ConstantFormatPart(hasSeenNumberOrNothing, buffer.toString()));
		}
	}

	
	/**
	 * Adds a constant format part to the formatSpecification.
	 * 
	 * @param buffer the buffer containing the constant part
	 * @param occursInsideOption whether an optional digit position has preceeded this pattern 
	 */
	private void addConstantPart(final StringBuilder buffer, final boolean occursInsideOption) {
		if (buffer.length() > 0) {
			formatSpecification.add(new ConstantFormatPart(occursInsideOption, buffer.toString()));
			buffer.setLength(0);
		}
	}

	/**
	 * Retrieves the number of digit positions this pattern contains. 
	 * 
	 * @return the number of digit positions
	 */
	protected int getDigitPositions() {
		return digitPositions;
	}

	/**
	 * Retrieves the format pattern. 
	 * 
	 * @return the format pattern.
	 */
	protected String getFormatPattern() {
		return formatPattern;
	}

	/**
	 * Creates the format overflow error message. 
	 * 
	 * @param formatPattern the format pattern.
	 * @param number the number being formatted. 
	 * @return The error message. 
	 */	
	protected String getErrorFormatOverflow(final String formatPattern, final int number) {
		return ERROR_FORMAT_OVERFLOW.format(new Object[] { formatPattern, String.valueOf(number) });
	}

	/**
	 * Represents the formatting context. The formatting context contains the
	 * buffer that eventually will contain the formatted value, the number being
	 * formatted and some contextual information used by the formatting
	 * algorithm.
	 */
	class FormatContext {
		int				atDigit	= 0;
		StringBuilder	buffer	= new StringBuilder();
		String			number;
		int				numberLength;

		
		/**
		 * Creates a new FormatContext for the specified number. 
		 * 
		 * @param number the number to format.
		 */
		FormatContext(final int number) {
			this.number = String.valueOf(number);
			this.numberLength = this.number.length();
		}
	}

	/**
	 * Represents a segment in a format pattern.
	 */
	interface FormatPart {
		/**
		 * @param ctx
		 *            the format context
		 */
		void apply(FormatContext ctx);
	}

	/**
	 * Represents a constant position in the format pattern.
	 */
	class ConstantFormatPart implements FormatPart {
		private final boolean	occursInsideOptionals;
		private final String	value;

		/**
		 * Creates a new ConstantFormatPart.
		 * 
		 * A constant part, which occurs between optional parts will only be
		 * included if the previous optional part was included.
		 * 
		 * @param occursInsideOptionals
		 *            whether this constant part is between option parts.
		 * @param value
		 *            the string constant
		 */
		ConstantFormatPart(final boolean occursInsideOptionals, final String value) {
			this.occursInsideOptionals = occursInsideOptionals;
			this.value = value;
		}

		/**
		 * @see com.emc.xcelerator.activities.GeneratedNumberFormat.CaseNumberFormat.FormatPart#apply(java.lang.StringBuilder,
		 *      java.lang.String)
		 */
		public void apply(final FormatContext ctx) {
			if (!occursInsideOptionals || ctx.atDigit > 0) {
				ctx.buffer.append(value);
			}
		}
	}

	/**
	 * Represents a digit position in the format pattern.
	 */
	class DigitFormatPart implements FormatPart {
		int		offset;
		boolean	usePadding;
		char	padCharacter;

		/**
		 * Creates a new DigitFormatPart.
		 * 
		 * @param offset
		 *            the decimal position of the number.
		 * @param usePadding
		 *            whether to use padding or not
		 * @param padCharacter
		 *            what padding character to use if padding is enabled.
		 */
		DigitFormatPart(final int offset, final boolean usePadding, final char padCharacter) {
			this.offset = offset;
			this.usePadding = usePadding;
			this.padCharacter = padCharacter;
		}

		/**
		 * Creates a new DigitFormatPart, this is equivalent to
		 * DigitFormatPart(offset,false,' ');
		 * 
		 * @param offset
		 */
		DigitFormatPart(final int offset) {
			this.offset = offset;
			this.usePadding = false;
		}

		/**
		 * @see com.emc.xcelerator.activities.GeneratedNumberFormat.CaseNumberFormat.FormatPart#apply(java.lang.StringBuilder,
		 *      java.lang.String)
		 */
		public void apply(final FormatContext ctx) {
			final int position = ctx.numberLength - (getDigitPositions() - offset);
			if (position >= 0) {
				ctx.buffer.append(ctx.number.charAt(position));
				ctx.atDigit += 1;
			} else {
				if (usePadding) {
					ctx.buffer.append(padCharacter);
				}
			}
		}
	}

}
