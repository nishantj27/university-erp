package edu.univ.erp.data;

/**
 * Wraps low-level SQLExceptions so the service and UI layers don't have to deal with checked
 * database exceptions everywhere. Something went wrong talking to the database.
 */
public class DataException extends RuntimeException {

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(String message) {
        super(message);
    }
}
