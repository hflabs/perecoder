package ru.hflabs.rcd.service.rule;

import ru.hflabs.rcd.RoleNames;
import ru.hflabs.rcd.model.rule.Rule;

import javax.annotation.security.RolesAllowed;
import java.util.Collection;

/**
 * Интерфейс <class>IRuleService</class> декларирует методы для управления правилами перекодирования
 *
 * @see Rule
 */
public interface IRuleService<R extends Rule<?, ?, R>> {

    /**
     * Выполняет модификацию правил перекодирования по изменившимся зависимостям
     *
     * @param dependencyClass класс зависимости
     * @param dependencies коллекцию изменившихся зависимостей
     * @return Возвращает модифицированные правила перекодирования
     */
    @RolesAllowed(RoleNames.ADMINISTRATOR)
    <T> Collection<R> modifyByDependencies(Class<T> dependencyClass, Collection<T> dependencies);
}
