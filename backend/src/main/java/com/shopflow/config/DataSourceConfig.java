package com.shopflow.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

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
        if (databaseUrl != null && (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://"))) {
            String rest = databaseUrl.substring(11);
            int atIndex = rest.lastIndexOf('@');
            if (atIndex < 0) throw new IllegalStateException("DATABASE_URL invalide : " + databaseUrl);

            String userPass = rest.substring(0, atIndex);
            String hostPortDb = rest.substring(atIndex + 1);
            String[] userParts = userPass.split(":", 2);
            String[] hostDbParts = hostPortDb.split("/", 2);
            String hostPort = hostDbParts[0];
            String dbName = hostDbParts.length > 1 ? hostDbParts[1] : "";
            String[] hostPortParts = hostPort.split(":", 2);
            String host = hostPortParts[0];
            int port = hostPortParts.length > 1 ? Integer.parseInt(hostPortParts[1]) : 5432;

            return buildDataSource(
                "jdbc:postgresql://" + host + ":" + port + "/" + dbName,
                userParts[0],
                userParts.length > 1 ? userParts[1] : ""
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
