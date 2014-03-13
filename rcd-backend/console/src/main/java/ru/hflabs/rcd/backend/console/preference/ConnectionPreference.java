package ru.hflabs.rcd.backend.console.preference;

import com.beust.jcommander.Parameter;

/**
 * Класс <class>ConnectionPreference</class> содержит информацию о настройках соединения с сервером
 *
 * @author Nazin Alexander
 */
public class ConnectionPreference {

    public static final String SERVER_HOST = "server.host";
    public static final String SERVER_PORT = "server.port";
    public static final String SERVER_PROTOCOL = "server.protocol";

    @Parameter(names = {"--host"}, description = "server host")
    private String serverHost;
    @Parameter(names = {"--port"}, description = "server port")
    private Integer serverPort;
    @Parameter(names = {"--protocol"}, description = "connection protocol")
    private String serverProtocol;

    public ConnectionPreference() {
        this.serverHost = "localhost";
        this.serverPort = 8080;
        this.serverProtocol = "http";
    }

    public String getServerHost() {
        return serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public String getServerProtocol() {
        return serverProtocol;
    }
}
