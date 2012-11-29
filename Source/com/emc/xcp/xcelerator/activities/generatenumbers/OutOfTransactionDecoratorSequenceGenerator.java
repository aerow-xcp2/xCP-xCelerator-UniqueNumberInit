package com.emc.xcp.xcelerator.activities.generatenumbers;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;

public class OutOfTransactionDecoratorSequenceGenerator implements
		SequenceGenerator {

	private SequenceGenerator sequenceGenerator;
	private static final int[] LOCK = new int[0];	

	public OutOfTransactionDecoratorSequenceGenerator(
			SequenceGenerator sequenceGenerator) {
		this.sequenceGenerator = sequenceGenerator;
	}

	public String getNextGeneratedNumber(IDfSession session,
			String numberSequenceName) throws DfException,
			SequenceObjectNotFoundException, SequenceGeneratorFailedException {

		synchronized (LOCK) {

			if (session.isTransactionActive()) {
				IDfSessionManager sessionManager = session.getSessionManager();
				IDfSession newSession = null;
				try {
					newSession = sessionManager.newSession(session
							.getDocbaseName());
					
					
					return sequenceGenerator.getNextGeneratedNumber(newSession,numberSequenceName);
				} finally {
					if (newSession != null) {
						sessionManager.release(newSession);
					}
				}
			} else {
				return sequenceGenerator.getNextGeneratedNumber(session,
						numberSequenceName);
			}
		}
	}

}
