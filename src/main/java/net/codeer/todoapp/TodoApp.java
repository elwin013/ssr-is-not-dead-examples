package net.codeer.todoapp;

import io.github.cdimascio.dotenv.Dotenv;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public final class TodoApp {
    private static final String CONNECTION_STRING = "PSQL_CONNECTION_STRING";

    private TodoApp() {
    }

    public static void main(String... args) {
        configureDataSource();
        Quarkus.run(args);
    }

    private static void configureDataSource() {
        String url = System.getenv(CONNECTION_STRING);
        if (url == null || url.isBlank()) {
            url = Dotenv.configure().ignoreIfMissing().load().get(CONNECTION_STRING);
        }
        if (url != null && !url.isBlank()) {
            System.setProperty("quarkus.datasource.jdbc.url", url);
        }
    }
}
