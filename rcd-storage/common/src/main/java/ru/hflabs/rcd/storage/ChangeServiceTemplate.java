package ru.hflabs.rcd.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.hflabs.rcd.event.ContextEvent;
import ru.hflabs.rcd.event.modify.ChangeEvent;
import ru.hflabs.rcd.model.Identifying;
import ru.hflabs.rcd.model.ModelOrder;
import ru.hflabs.rcd.model.change.ChangeMode;
import ru.hflabs.rcd.service.ISingleClassObserver;

import java.util.Collection;

/**
 * Класс <class>ChangeDocumentServiceTemplate</class> реализует базовый сервис изменения документов
 *
 * @author Nazin Alexander
 */
public abstract class ChangeServiceTemplate<E extends Identifying> implements ISingleClassObserver<E>, SmartApplicationListener, BeanNameAware {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    /** Идентификатор сервиса */
    private String serviceId;
    /** Класс отслеживаемых документов */
    private final Class<E> targetClass;
    /** Позиция слушателя, относительно других */
    private int listenerOrder;

    public ChangeServiceTemplate(Class<E> targetClass) {
        this.targetClass = targetClass;
        this.listenerOrder = ModelOrder.getOrder(targetClass);
    }

    @Override
    public void setBeanName(String name) {
        this.serviceId = name;
    }

    @Override
    public int getOrder() {
        return listenerOrder;
    }

    public void setOrder(int order) {
        this.listenerOrder = order;
    }

    @Override
    public final Class<E> retrieveTargetClass() {
        return targetClass;
    }

    public String retrieveTargetClassName() {
        return retrieveTargetClass().getSimpleName();
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return ContextEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return true;
    }

    /**
     * Выполняет создание сущностей
     *
     * @param changed коллекция для создания
     * @return Возвращает созданные сущности
     */
    protected abstract Collection<E> handleSelfCreateEvent(Collection<E> changed);

    /**
     * Выполняет обновление сущностей
     *
     * @param changed коллекция для обновления
     * @return Возвращает обновленные сущности
     */
    protected abstract Collection<E> handleSelfUpdateEvent(Collection<E> changed);

    /**
     * Выполняет восстановление сущностей
     *
     * @param changed коллекция для обновления
     * @return Возвращает восстановленные сущности
     */
    protected abstract Collection<E> handleSelfRestoreEvent(Collection<E> changed);

    /**
     * Выполняет закрытие сущностей
     *
     * @param changed коллекция для закрытия
     * @return Возвращает закрытые сущности
     */
    protected abstract Collection<E> handleSelfCloseEvent(Collection<E> changed);

    /**
     * Выполняет обработку событий изменения отслеживаемых сущностей
     *
     * @param event событие
     */
    protected void handleSelfChangeEvent(ChangeEvent event) {
        Collection<E> changed = event.getChanged(retrieveTargetClass());
        // По типу события выпоняем модификацию документов в хранилище
        switch (event.getChangeType()) {
            case IGNORE:
            case SKIP: {
                break;
            }
            case CREATE: {
                handleSelfCreateEvent(changed);
                break;
            }
            case UPDATE: {
                handleSelfUpdateEvent(changed);
                break;
            }
            case RESTORE: {
                handleSelfRestoreEvent(changed);
                break;
            }
            case CLOSE: {
                handleSelfCloseEvent(changed);
                break;
            }
            default: {
                throw new UnsupportedOperationException(
                        String.format("Change event '%s' not supported by '%s'", event.getChangeType(), getClass().getSimpleName())
                );
            }
        }
    }

    /**
     * Выполняет обработку события создания зависимых документов
     *
     * @param event событие
     */
    protected void handleOtherCreateEvent(ChangeEvent event) {
        // do nothing
    }

    /**
     * Выполняет обработку события обновления зависимых документов
     *
     * @param event событие
     */
    protected void handleOtherUpdateEvent(ChangeEvent event) {
        // do nothing
    }

    /**
     * Выполняет обработку события восстановления зависимых документов
     *
     * @param event событие
     */
    private void handleOtherRestoreEvent(ChangeEvent event) {
        // do noting
    }

    /**
     * Выполняет обработку события закрытия зависимых документов
     *
     * @param event событие
     */
    protected void handleOtherCloseEvent(ChangeEvent event) {
        // do nothing
    }

    /**
     * Выполняет обработку событий изменения документов, которые не отслеживаются данным сервисом
     *
     * @param event событие
     */
    protected void handleOtherChangeEvent(ChangeEvent event) {
        if (!ChangeMode.ISOLATED.equals(event.getChangeMode())) {
            switch (event.getChangeType()) {
                case IGNORE:
                case SKIP: {
                    break;
                }
                case CREATE: {
                    handleOtherCreateEvent(event);
                    break;
                }
                case UPDATE: {
                    handleOtherUpdateEvent(event);
                    break;
                }
                case RESTORE: {
                    handleOtherRestoreEvent(event);
                    break;
                }
                case CLOSE: {
                    handleOtherCloseEvent(event);
                    break;
                }
                default: {
                    throw new UnsupportedOperationException(String.format("Event %s not supported by %s", event.getChangeType(), getClass().getName()));
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    protected void handleChangeEvent(ChangeEvent event) {
        if (retrieveTargetClass().isAssignableFrom(event.getChangedClass())) {
            handleSelfChangeEvent(event);
        } else {
            handleOtherChangeEvent(event);
        }
    }

    /**
     * Выполняет обработку события приложения
     *
     * @param event событие
     */
    protected void handleContextEvent(ContextEvent event) {
        if (event instanceof ChangeEvent) {
            handleChangeEvent((ChangeEvent) event);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextEvent && ((ContextEvent) event).registryListener(serviceId)) {
            handleContextEvent((ContextEvent) event);
        }
    }
}
