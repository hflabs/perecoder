package ru.hflabs.rcd.backend.console.preference;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import lombok.Getter;

/**
 * Класс <class>Preference</class> содержит информацию о настройках работы с приложением
 *
 * @author Nazin Alexander
 */
@Getter
public class Preference {

    /** Флаг, указывающий на вывод подсказки использования */
    @Parameter(names = {"-h", "--help"}, description = "print usage", help = true)
    private boolean help = false;
    /** Настройки соединения с сервером */
    @ParametersDelegate
    private ConnectionPreference connectionPreference;
    /** Настройки аутентификации */
    @ParametersDelegate
    private AuthenticationPreference authenticationPreference;

    public Preference() {
        this.connectionPreference = new ConnectionPreference();
        this.authenticationPreference = new AuthenticationPreference();
    }
}
