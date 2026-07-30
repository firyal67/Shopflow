package com.shopflow.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
@Profile("prod")
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String jdbcUrl = System.getenv("JDBC_DATABASE_URL");
        String username = System.getenv("JDBC_DATABASE_USERNAME");
        String password = System.getenv("JDBC_DATABASE_PASSWORD");

        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            return buildDataSource(jdbcUrl, username, password);
        }

        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            URI uri = URI.create("http://" + databaseUrl.substring(11));
            String host = uri.getHost();
            int port = uri.getPort();
            String path = uri.getPath() != null ? uri.getPath().substring(1) : "";
            String[] userInfo = uri.getUserInfo() != null ? uri.getUserInfo().split(":", 2) : new String[]{"", ""};

            return buildDataSource(
                "jdbc:postgresql://" + host + ":" + port + "/" + path,
                userInfo[0],
                userInfo.length > 1 ? userInfo[1] : ""
            );
        }

        throw new IllegalStateException("Aucune variable DATABASE_URL ou JDBC_DATABASE_URL trouvée");
    }

    private DataSource buildDataSource(String url, String username, String password) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
