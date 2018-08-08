package org.gluu.oxtrust.api.errors;

import java.util.Date;
import java.util.UUID;

public class ApiError {

    private String reference;
    private Date date;
    private Object message;
    private Class<? extends Throwable> throwable;

    public ApiError() {
        // default-Ctor.
    }

    public ApiError(String reference,
                    Date date,
                    Object message,
                    Class<? extends Throwable> throwable) {
        this.reference = reference;
        this.date = date;
        this.message = message;
        this.throwable = throwable;
    }

    static ApiError of(Throwable throwable, Object message) {
        return new ApiError(UUID.randomUUID().toString(), new Date(), message, throwable.getClass());
    }
    static ApiError of(Throwable throwable) {
        return new ApiError(UUID.randomUUID().toString(), new Date(), throwable.getMessage(), throwable.getClass());
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Class<? extends Throwable> getThrowable() {
        return throwable;
    }

    public void setThrowable(Class<? extends Throwable> throwable) {
        this.throwable = throwable;
    }
}