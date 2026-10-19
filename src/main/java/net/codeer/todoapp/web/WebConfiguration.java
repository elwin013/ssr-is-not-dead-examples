package net.codeer.todoapp.web;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletRequest;
import net.codeer.todoapp.common.InMemoryLoginDAO;
import net.codeer.todoapp.common.LoginDAO;
import net.codeer.todoapp.common.UserInfo;
import net.codeer.todoapp.psql.PsqlTodoDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.sql.DataSource;
import java.util.List;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    public static final String SESSION_USER = "user";

    @Bean(destroyMethod = "close")
    DataSource dataSource() {
        String url = System.getenv("PSQL_CONNECTION_STRING");
        if (url == null || url.isBlank())
            url = Dotenv.configure().ignoreIfMissing().load().get("PSQL_CONNECTION_STRING");
        if (url == null || url.isBlank())
            throw new IllegalStateException("Set PSQL_CONNECTION_STRING before starting the application.");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(10);
        config.setPoolName("todoapp-postgres");
        return new HikariDataSource(config);
    }

    @Bean
    LoginDAO loginDAO() {
        return new InMemoryLoginDAO(List.of(
                new InMemoryLoginDAO.User("kamil@signaturky.com", "asdf1234", 1, "Signaturky"),
                new InMemoryLoginDAO.User("paulina@codeer.net", "asdf1234", 2, "Codeer"),
                new InMemoryLoginDAO.User("lusia@codeer.net", "asdf1234", 2, "Codeer")));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
                if (request.getSession(false) == null || request.getSession(false).getAttribute(SESSION_USER) == null) {
                    response.sendRedirect("/login");
                    return false;
                }
                return true;
            }
        }).excludePathPatterns("/login", "/bulma.css", "/htmx.js", "/hx-sse.js");
    }

    static UserInfo user(HttpServletRequest request) {
        return (UserInfo) request.getSession().getAttribute(SESSION_USER);
    }
}
