package ru.hflabs.rcd.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.convert.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.hflabs.rcd.exception.ApplicationException;
import ru.hflabs.rcd.exception.ApplicationValidationException;
import ru.hflabs.rcd.exception.constraint.IllegalPrimaryKeyException;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.definition.ModelDefinition;
import ru.hflabs.rcd.service.IChangeService;
import ru.hflabs.rcd.service.IServiceFactory;
import ru.hflabs.rcd.web.model.ErrorBean;
import ru.hflabs.rcd.web.model.ErrorView;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static ru.hflabs.rcd.service.ServiceUtils.extractSingleDocument;

/**
 * Класс <class>ControllerTemplate</class> реализует базовый класс контроллеров страниц, содержищий обработку исключений
 *
 * @see ExceptionHandler
 */
public abstract class ControllerTemplate {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Постфикс идентификатора контроллера */
    public static final String NAME_POSTFIX = ".controller";
    /** Постфикс URI контроллера */
    public static final String DATA_URI = "/data";

    /** Сервис работы с сообщениями */
    @Resource(name = "messageSource")
    protected MessageSource messageSource;
    /** Фабрика описания моделей */
    @Resource(name = "modelDefinitionFactory")
    protected IServiceFactory<ModelDefinition, Class<?>> modelDefinitionFactory;
    /** Сервис конвертации объектов */
    @Resource(name = "jacksonObjectMapper")
    protected ObjectMapper objectMapper;
    /** Размер страницы фильтрации объектов по умолчанию */
    @Value("$web{paging.size}")
    protected int defaultPagingSize;

    /**
     * Выполняет создание одного документа
     *
     * @param service сервис работы с документами
     * @param target создаваемый документ
     * @param needValidation флаг необходимости валидации документа перед созданием
     * @return Возвращает обновленный документ
     */
    protected static <T extends Identifying> T createSingleDocument(IChangeService<T> service, T target, boolean needValidation) {
        return extractSingleDocument(service.create(Arrays.asList(target), needValidation));
    }

    /**
     * Выполняет обновление одного документа
     *
     * @param service сервис работы с документами
     * @param target обновляемый документ
     * @param needValidation флаг необходимости валидации документа перед обновлением
     * @return Возвращает обновленный документ
     */
    protected static <T extends Identifying> T updateSingleDocument(IChangeService<T> service, T target, boolean needValidation) {
        return extractSingleDocument(service.update(Arrays.asList(target), needValidation));
    }

    /**
     * Выделяет название локализации для исключительной ситуации
     *
     * @param exception исключение
     * @return Возвращает название локализации
     */
    private static String extractExceptionName(Throwable exception) {
        return exception.getClass().getSimpleName();
    }

    /**
     * Создает и возвращает декоратор ошибки
     *
     * @param code код ошибки
     * @param locale текущая локаль
     * @param exception исключительная ситуация
     * @param parameters параметры сообщения
     * @return Возвращает созданный декоратор ошибки
     */
    private ErrorBean createErrorBean(String code, Locale locale, Throwable exception, Object... parameters) {
        return new ErrorBean(ImmutableList.of(messageSource.getMessage(code, parameters, exception.getMessage(), locale)));
    }

    /**
     * Формируем декоратор ошибок валидации объекта
     *
     * @param errors ошибки валидации
     * @param locale локализация
     * @return Возвращает декоратор ошибок валидации
     */
    protected ErrorBean doHandleValidationException(Errors errors, final Locale locale) {
        Collection<String> globalErrors = Collections2.transform(errors.getGlobalErrors(), new Function<ObjectError, String>() {
            @Override
            public String apply(ObjectError input) {
                return messageSource.getMessage(input, locale);
            }
        });
        ImmutableMap.Builder<String, String> fieldErrors = ImmutableMap.builder();
        for (FieldError error : errors.getFieldErrors()) {
            fieldErrors.put(error.getField(), messageSource.getMessage(error, locale));
        }
        return new ErrorBean(globalErrors, fieldErrors.build());
    }

    @ExceptionHandler({
            ServletRequestBindingException.class,
            HttpMessageConversionException.class,
            ConversionException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorBean handleMissingParameterException(Exception exception, Locale locale) {
        return createErrorBean(extractExceptionName(exception), locale, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorBean handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, Locale locale) {
        return doHandleValidationException(exception.getBindingResult(), locale);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorBean handleBindException(BindException exception, Locale locale) {
        return doHandleValidationException(exception.getBindingResult(), locale);
    }

    @ExceptionHandler(ApplicationValidationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorBean handleApplicationValidationException(ApplicationValidationException exception, Locale locale) {
        return doHandleValidationException(exception.getErrors(), locale);
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorBean handleApplicationException(ApplicationException exception, Locale locale) {
        return createErrorBean(extractExceptionName(exception), locale, exception);
    }

    @ExceptionHandler(IllegalPrimaryKeyException.class)
    @ResponseStatus(HttpStatus.GONE)
    @ResponseBody
    public ErrorBean handleIllegalPrimaryKeyException(IllegalPrimaryKeyException exception, Locale locale) {
        return handleApplicationException(exception, locale);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ModelAndView handleThrowable(Throwable exception, Locale locale) {
        LOG.error(exception.getMessage(), exception);
        return new ErrorView(createErrorBean(ErrorBean.UNEXPECTED_ERROR_KEY, locale, exception)).asView();
    }
}
