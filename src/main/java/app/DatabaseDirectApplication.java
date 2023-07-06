package app;

import java.util.*;
import java.util.stream.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class DatabaseDirectApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDirectApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DatabaseDirectApplication.class, args);
	}

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... strings) throws Exception {
        logger.info("Creating table");

        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE customers (" +
            "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long")
            .stream()
            .map(name -> name.split(" "))
            .collect(Collectors.toList());

        splitUpNames.forEach(name -> logger.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?, ?)", splitUpNames);

        logger.info("Querying for customer records where first_name = 'Josh':");

        jdbcTemplate.query(
            "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
            ).forEach(customer -> logger.info(customer.toString()));

        logger.info("List all customers");
        jdbcTemplate.query(
            "SELECT id, first_name, last_name FROM customers",
                (result, rowNumber) -> new Customer(result.getLong("id"), result.getString("first_name"), result.getString("last_name"))
        ).forEach(customer -> logger.info(customer.toString()));
    }

}
