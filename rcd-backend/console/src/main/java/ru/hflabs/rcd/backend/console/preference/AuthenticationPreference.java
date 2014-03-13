package ru.hflabs.rcd.backend.console.preference;

import com.beust.jcommander.Parameter;

/**
 * Класс <class>AuthenticationPreference</class> содержит информацию о настройках аутентификации
 *
 * @author Nazin Alexander
 */
public class AuthenticationPreference {

    public static final String SERVER_LOGIN = "server.login";
    public static final String SERVER_PASSWORD = "server.password";

    @Parameter(names = {"-l", "--login"}, description = "login")
    private String login;
    @Parameter(names = {"-p", "--password"}, description = "password", password = true)
    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
