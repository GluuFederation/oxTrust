package org.gluu.oxtrust.api.errors;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.Date;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException e) {
        List<ValidationError> errors = FluentIterable.from(e.getConstraintViolations())
                .transform(toValidationError())
                .toList();

        return Response.status(BAD_REQUEST)
                .entity(ApiError.of(e, errors))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    private Function<ConstraintViolation, ValidationError> toValidationError() {
        final Date date = new Date();
        return new Function<ConstraintViolation, ValidationError>() {
            @Override
            public ValidationError apply(ConstraintViolation constraintViolation) {
                final ValidationError error = new ValidationError();
                error.setPath(constraintViolation.getPropertyPath().toString());
                error.setMessage(constraintViolation.getMessage());
                error.setDate(date);
                return error;
            }
        };
    }

}
