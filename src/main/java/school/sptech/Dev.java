package school.sptech;


import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class Dev {

    private DataSource dev;

    public Dev() {

        DriverManagerDataSource driver = new DriverManagerDataSource();

        driver.setUrl("jdbc:mysql://localhost:3306/voltix");
        driver.setUsername("alert_sender_user"); // LEMBRAR DE MUDAR O USER
        driver.setPassword("senha_aqui"); // LEMBRAR DE MUDAR A SENHA
        driver.setDriverClassName("com.mysql.cj.jdbc.Driver");

        this.dev = driver;
    }

    public DataSource getConexao() {
        return this.dev;
    }
}