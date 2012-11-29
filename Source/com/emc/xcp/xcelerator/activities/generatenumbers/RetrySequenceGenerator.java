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
// Note :
// Modified by   : Mélissa TSANG
// Modifications : Recreated another type gennum_number_sequence with xcp2.
// ****************************************************************************

package com.emc.xcp.xcelerator.activities.generatenumbers;

import java.text.MessageFormat;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfException;

/**
 * The RetrySequenceGenerator strategy generates new numbers in a specified
 * sequence by relying on VERSION_MISMATCH exceptions being thrown, after such
 * an exception is caught, it retries a fixed number of times (with increasing
 * randomized intervals between each retry). If a new number cannot be generated
 * after the retry attempts have been exhausted a
 * SequenceGeneratorFailedException will be thrown. Copyright (c) 1994 - 2010.
 * EMC Corporation. All Rights Reserved.
 */
public class RetrySequenceGenerator implements SequenceGenerator {
  private static final String LOG_CATEGORY = RetrySequenceGenerator.class
      .getCanonicalName();

  private static final int MAX_TRY_COUNT = 10;
  private static final int INITIAL_RETRY_INTERVAL_MS = 1000;
  private static final double RETRY_INTERVAL_GROWTH = 1.5;
  private static final boolean RANDOMIZE_RETRY_INTERVAL = true;
  // New Type gennum_number_sequence
  private static final MessageFormat SEQUENCE_GENERATOR_QUALIFICATION = new MessageFormat(
      "numgen_number_sequence where object_name = ''{0}''");
  private static final MessageFormat ERR_NUMBER_SEQUENCE_NOT_FOUND = new MessageFormat(
      "Number sequence not found or user does not have access to the number sequence ''{0}''.");
  private static final MessageFormat ERR_RETRY_ATTEMPTS_EXHAUSTED = new MessageFormat(
      "Sequence Generator failed to generate a new number in the sequence ''{0}'' because all retry attempts was exhausted.");

  private static final MessageFormat ERR_NUMBER_GENERATION_FAILED = new MessageFormat(
      "Sequence Generator failed to generate a new number in the sequence ''{0}'' due to an internal error.");

  private static final String EXCEPTION_MSG_ID_CANT_SAVE = "CANT_SAVE";
  private static final String EXCEPTION_MSG_ID_VERSION_MISMATCH = "VERSION_MISMATCH";

  /**
   * @see com.emc.xcp.xcelerator.activities.generatenumbers.SequenceGenerator#getNextGeneratedNumber(com.documentum.fc.client.IDfSession,
   *      java.lang.String)
   */
  public String getNextGeneratedNumber(final IDfSession session,
      final String numberSequenceName) throws DfException,
      SequenceObjectNotFoundException, SequenceGeneratorFailedException {

    final NumberSequenceObject seq = getSequenceGeneratorObject(session,
        numberSequenceName);

    for (int i = 0; i < getMaxRetryCount(); ++i) {
      try {
        seq.refresh(session);
        return seq.getAndIncrement();
      } catch (final DfException e) {
        if (!isVersionMismatchError(e)) {
          throw new SequenceGeneratorFailedException(
              getErrorNumberGenerationFailed(numberSequenceName), e);
        } else {
          sleep(i);
        }
      }
    }

    throw new SequenceGeneratorFailedException(
        getErrorRetryAttemptsExhausted(numberSequenceName));
  }

  /**
   * Returns the number sequence object specified by numberSequenceName.
   * @param session
   *          the repository session
   * @param numberSequenceName
   *          The name of the number sequence
   * @return The number sequence object specified by the numberSequenceName
   * @throws SequenceObjectNotFoundException
   *           if the number sequence wasn't found.
   * @throws DfException
   *           if an internal error occurred.
   */
  protected NumberSequenceObject getSequenceGeneratorObject(
      final IDfSession session, final String numberSequenceName)
      throws SequenceObjectNotFoundException, DfException {
    final String qualification = getNumberSequenceQualification(numberSequenceName);
    final IDfPersistentObject doc = session
        .getObjectByQualification(qualification);
    if (doc == null) {
      throw new SequenceObjectNotFoundException(
          getErrorNumberSequenceNotFound(numberSequenceName));
    }
    return new NumberSequenceObject(doc);
  }

  /**
   * Temporarily pauses the execution of the current thread for the amount
   * specified by the current retry interval.
   * @param retryNumber
   *          The number of the retry attempt.
   */
  protected void sleep(final int retryNumber) {
    try {
      final long interval = getInterval(retryNumber);
      Thread.sleep(interval);
    } catch (final InterruptedException e) {
      DfLogger.warn(LOG_CATEGORY, "Got interrupted while trying to sleep.",
          null, e);
      // IGNORE
    }
  }

  /**
   * Calculates the interval time, it is in the interval [0,
   * initialRetryInterval * (retryAttemptNumber * getRetryIntervalGrowth)].
   * @param retryAttemptNumber
   *          The number of the retry attempt.
   * @return The interval time.
   */
  protected long getInterval(final int retryAttemptNumber) {
    final double maxInterval = getInitialRetryInterval()
        * (retryAttemptNumber * getRetryIntervalGrowth());
    if (getRandomizeRetryInterval()) {
      return Math.round(maxInterval * Math.random());
    } else {
      return Math.round(maxInterval);
    }
  }

  /**
   * Determines if an exceptions is a version mismatch exception.
   * @param e
   *          The exception to check.
   * @return true if the exception is a version mismatch exception, false
   *         otherwise.
   */
  protected boolean isVersionMismatchError(final DfException e) {
    if (e.getMessageId().contains(EXCEPTION_MSG_ID_CANT_SAVE)) {
      final IDfException e1 = e.getNextException();
      return e1.getMessageId().contains(EXCEPTION_MSG_ID_VERSION_MISMATCH);
    }
    return e.getMessageId().contains(EXCEPTION_MSG_ID_VERSION_MISMATCH);
  }

  /**
   * Creates the number sequence not found error message.
   * @param name
   *          The name of the number sequence.
   * @return The error message.
   */
  protected String getErrorNumberSequenceNotFound(final String name) {
    return ERR_NUMBER_SEQUENCE_NOT_FOUND.format(new Object[] {name});
  }

  /**
   * Creates the number generation failed error message.
   * @param name
   *          The name of the number sequence.
   * @return The error message.
   */
  protected String getErrorNumberGenerationFailed(final String name) {
    return ERR_NUMBER_GENERATION_FAILED.format(new Object[] {name});
  }

  /**
   * Creates the retry attempts exhausted error message.
   * @param name
   *          The name of the number sequence.
   * @return The error message.
   */
  protected String getErrorRetryAttemptsExhausted(final String name) {
    return ERR_RETRY_ATTEMPTS_EXHAUSTED.format(new Object[] {name});
  }

  /**
   * Creates the number sequence qualification DQL which is used to find the
   * number sequence by name.
   * @param name
   *          the name of the number sequence
   * @return The number sequence DQL qualification
   */
  protected String getNumberSequenceQualification(final String name) {
    return SEQUENCE_GENERATOR_QUALIFICATION.format(new Object[] {name});
  }

  /**
   * Retrieves the maximum retry count.
   * @return the maximum retry count.
   */
  protected int getMaxRetryCount() {
    return MAX_TRY_COUNT;
  }

  /**
   * Retrieves the initial retry interval.
   * @return the initial retry interval
   */
  protected int getInitialRetryInterval() {
    return INITIAL_RETRY_INTERVAL_MS;
  }

  /**
   * Retrieves the retry interval growth.
   * @return the retry interval growth.
   */
  protected double getRetryIntervalGrowth() {
    return RETRY_INTERVAL_GROWTH;
  }

  /**
   * Returns whether retry interval randomization is to be employed.
   * @return true if retry interval randomization is used, false otherwise.
   */
  protected boolean getRandomizeRetryInterval() {
    return RANDOMIZE_RETRY_INTERVAL;
  }
}