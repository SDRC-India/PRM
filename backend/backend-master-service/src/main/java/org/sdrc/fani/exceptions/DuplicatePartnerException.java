package org.sdrc.fani.exceptions;

/**
 * @author subham
 *
 */
public class DuplicatePartnerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8157602321334779958L;

	public DuplicatePartnerException() {
		super();
	}

	public DuplicatePartnerException(String args) {
		super(args);
	}

	public DuplicatePartnerException(Throwable args) {
		super(args);
	}

}
