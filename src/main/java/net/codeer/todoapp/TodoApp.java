package net.codeer.todoapp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import net.codeer.todoapp.common.CustomAccessManager;
import net.codeer.todoapp.common.InMemoryLoginDAO;
import net.codeer.todoapp.common.LoginController;
import net.codeer.todoapp.common.TodoController;
import net.codeer.todoapp.common.TodoDAO;
import net.codeer.todoapp.psql.PsqlTodoDAO;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public final class TodoApp {
    static void main() {
        HikariDataSource dataSource = dataSource();

        Function<Context, TodoDAO> todoDAOProvider = ctx ->
                new PsqlTodoDAO(dataSource, CustomAccessManager.getUserInfo(ctx).tenantId());

        Javalin app = Javalin.create(config -> {
            config.startup.showJavalinBanner = false;
            config.fileRenderer(new JavalinJte(templateEngine()));
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.routes.beforeMatched(CustomAccessManager::beforeMatched);
            new LoginController(new InMemoryLoginDAO(initialUsers())).registerRoutes(config.routes);
            new TodoController(todoDAOProvider).registerRoutes(config.routes);
            config.events.serverStopping(dataSource::close);

            config.events.serverStopping(dataSource::close);
        });
        app.start(7070);
    }

    private static List<InMemoryLoginDAO.User> initialUsers() {
        return List.of(
                new InMemoryLoginDAO.User("kamil@signaturky.com", "asdf1234", 1, "Signaturky"),
                new InMemoryLoginDAO.User("paulina@codeer.net", "asdf1234", 2, "Codeer"),
                new InMemoryLoginDAO.User("lusia@codeer.net", "asdf1234", 2, "Codeer")
        );
    }

    private static TemplateEngine templateEngine() {
        return TemplateEngine.create(
                new DirectoryCodeResolver(Path.of("src", "main", "jte")),
                Path.of("target", "jte-classes"),
                ContentType.Html
        );
    }

    private static HikariDataSource dataSource() {
        String url = System.getenv("PSQL_CONNECTION_STRING");
        if (url == null || url.isBlank()) {
            url = Dotenv.configure().ignoreIfMissing().load().get("PSQL_CONNECTION_STRING");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Set PSQL_CONNECTION_STRING before starting the application.");
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(10);
        config.setPoolName("todoapp-postgres");
        return new HikariDataSource(config);
    }
}
