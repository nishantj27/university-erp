package edu.univ.erp.util;

/** Something went wrong while writing an export file (CSV/PDF). Shown to the user as a message. */
public class ExportException extends RuntimeException {

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
