package org.sdrc.fani.exceptions;

import javax.servlet.http.HttpServletRequest;

import org.sdrc.usermgmt.core.util.ApiError;
import org.sdrc.usermgmt.core.util.RestExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author Subham Ashish(subham@sdrc.co.in)
 */

@Component
public class ExceptionHandler extends RestExceptionHandler {

	@org.springframework.web.bind.annotation.ExceptionHandler(DuplicatePartnerException.class)
	protected ResponseEntity<Object> handleDuplicateRecordException(DuplicatePartnerException ex,
			HttpServletRequest request) {
		String error = ex.getMessage();
		return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, error, ex));
	}

}
