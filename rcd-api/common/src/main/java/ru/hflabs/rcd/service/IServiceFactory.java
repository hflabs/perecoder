package ru.hflabs.rcd.service;

/**
 * Интерфейс <class>IServiceFactory</class> декларирует методы фабрики сервисов
 */
public interface IServiceFactory<S, K> {

    /**
     * @param key ключ доступа к сервису
     * @return Создает и возвращает сервис по его ключу
     */
    S retrieveService(K key);

    /**
     * Освобождает ресурсы сервиса
     *
     * @param key ключ доступа к сервису
     * @param service экземпляр сервиса
     */
    void destroyService(K key, S service);
}
