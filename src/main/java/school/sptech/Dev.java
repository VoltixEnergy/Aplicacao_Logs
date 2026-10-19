package school.sptech;


import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class Dev {

    private DataSource dev;

    public Dev() {

        DriverManagerDataSource driver = new DriverManagerDataSource();

        driver.setUrl("jdbc:mysql://localhost:3306/voltix");
        driver.setUsername("alert_sender_user");
        driver.setPassword("senha_aqui");
        driver.setDriverClassName("com.mysql.cj.jdbc.Driver");

        this.dev = driver;
    }

    public DataSource getConexao() {
        return this.dev;
    }
}