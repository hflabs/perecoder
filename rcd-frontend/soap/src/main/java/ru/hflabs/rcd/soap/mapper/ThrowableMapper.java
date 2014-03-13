package ru.hflabs.rcd.soap.mapper;

import ru.hflabs.rcd.model.notification.NotifyType;
import ru.hflabs.rcd.soap.model.WError;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Класс <class>WExceptionMapper</class> реализует сервис трансформации ошибки в REST-full ответ
 *
 * @author Nazin Alexander
 */
@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    /**
     * Формирует сообщение об ошибке
     *
     * @param notifyType тип ошибки
     * @param cause причина ошибки
     * @return Возвращает сообщение об ошибке
     */
    public static WError createError(NotifyType notifyType, Throwable cause) {
        WError errorMessage = new WError();
        errorMessage.setErrorType(notifyType.name());
        errorMessage.setErrorMessage(String.format("%s: %s", cause.getClass().getSimpleName(), cause.getMessage()));
        return errorMessage;
    }

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(createError(NotifyType.ERROR, exception))
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(exception.getMessage())
                    .build();
        }
    }
}
