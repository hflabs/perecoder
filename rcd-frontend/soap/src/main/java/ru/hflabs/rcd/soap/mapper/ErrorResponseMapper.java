package ru.hflabs.rcd.soap.mapper;

import ru.hflabs.rcd.soap.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Класс <class>WErrorResponseMapper</class> реализует сервис трансформации {@link ru.hflabs.rcd.soap.ErrorResponse ошибки} в REST-full ответ
 *
 * @author Nazin Alexander
 */
@Provider
public class ErrorResponseMapper implements ExceptionMapper<ErrorResponse> {

    @Override
    public Response toResponse(ErrorResponse exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(exception.getFaultInfo())
                .build();
    }
}
