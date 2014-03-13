package ru.hflabs.rcd.web;

import ru.hflabs.rcd.model.criteria.FilterCriteria;
import ru.hflabs.rcd.model.criteria.FilterResult;
import ru.hflabs.rcd.service.IPagingService;
import ru.hflabs.rcd.web.model.PageResponseBean;

/**
 * Класс <class>PagingUtils</class> реализует вспомогательные методы для формирования страниц объектов
 *
 * @see ru.hflabs.rcd.service.IFilterService
 */
public abstract class PagingUtils {

    protected PagingUtils() {
    }

    /**
     * Рассчитывает общее количество страниц
     *
     * @param pageSize размер страницы
     * @param itemsCount общее количество объектов
     * @return Возвращает рассчитанное общее количество страниц
     */
    private static int calculateTotalPageCount(int pageSize, int itemsCount) {
        if (pageSize != FilterCriteria.COUNT_ALL) {
            int tail = (itemsCount % pageSize == 0) ? 0 : 1;
            return itemsCount / pageSize + tail;
        } else {
            return 1;
        }
    }

    /**
     * Рассчитывает смещение относительно начала объектов
     *
     * @param pageSize размер страницы
     * @param page текущая страница
     * @return Возвращает рассчитанное смешение
     */
    private static int calculateOffset(int pageSize, int page) {
        return (pageSize != FilterCriteria.COUNT_ALL) ?
                Math.max(page - 1, 0) * pageSize :
                0;
    }

    /**
     * Рассчитывает количество объектов на странице
     *
     * @param pageSize предпочитаемый размер
     * @param defaultSize размер по умолчанию
     * @return Возвращает рассчитанный размер
     */
    private static int calculatePageSize(Integer pageSize, int defaultSize) {
        return (pageSize != null && (pageSize > 0 || pageSize == FilterCriteria.COUNT_ALL)) ?
                pageSize :
                defaultSize;
    }

    /**
     * Выполняет поиск страницы объектов
     *
     * @param pageSize количество объектов на странице
     * @param defaultPageSize количество объектов на странице по умолчанию
     * @param currentPage запрашиваемая страница
     * @param service сервис получения результатов
     * @return Возвращает результат фильтрации
     */
    public static <T> PageResponseBean<T> findPageByCriteria(Integer pageSize, int defaultPageSize, int currentPage, IPagingService<T> service) {
        final int targetPage = Math.max(1, currentPage);
        final int targetPageSize = calculatePageSize(pageSize, defaultPageSize);
        // Формируем смещение относительно начала объектов
        final int offset = calculateOffset(targetPageSize, targetPage);
        // Получаем коллекцию результатов
        FilterResult<T> filterResult = service.findPage(targetPageSize, offset);
        // Если объекты не найдены и целевая страница не является первой,
        // то перерасчитываем критерии поиска для получения последней страницы
        if (filterResult.isEmpty() && targetPage != 1) {
            return findPageByCriteria(pageSize, defaultPageSize, calculateTotalPageCount(targetPageSize, filterResult.getCountByFilter()), service);
        }
        // Формируем и возвращаем результат
        return new PageResponseBean<>(
                Math.max(targetPageSize, filterResult.size()),
                targetPage,
                calculateTotalPageCount(targetPageSize, filterResult.getCountByFilter()),
                filterResult.getCountByFilter(),
                filterResult.getResult()
        );
    }
}
