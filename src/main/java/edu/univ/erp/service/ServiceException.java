package edu.univ.erp.service;

/**
 * A business-rule problem the user should see as a friendly message, e.g. "Section full",
 * "You are already registered", or "The add/drop deadline has passed". Not a bug - just a
 * request that can't be carried out. The UI catches these and shows the message.
 */
public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }
}
