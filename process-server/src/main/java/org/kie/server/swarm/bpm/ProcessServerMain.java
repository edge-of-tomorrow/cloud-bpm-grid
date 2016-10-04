package org.kie.server.swarm.bpm;

import java.util.Arrays;

import org.kie.server.swarm.AbstractKieServerMain;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.transactions.TransactionsFraction;

public class ProcessServerMain extends AbstractKieServerMain {

    public static void main(String[] args) throws Exception {

        Container container = new Container();

        // Configure the Datasources subsystem with a driver and a datasource
        boolean inMemory = Boolean.valueOf(System.getProperty("org.kie.server.inmemory"));
        if (inMemory) {
            container.fraction(new DatasourcesFraction().jdbcDriver("h2", (d) -> {
                d.driverClassName("org.h2.Driver");
                d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                d.driverModuleName("com.h2database.h2");
            }).dataSource("ExampleDS", (ds) -> {
                ds.driverName("h2");
                ds.connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
                ds.userName("sa");
                ds.password("sa");
            }));
        } else {
            String dbhost = System.getProperty("org.kie.server.db.host");
            String dbport = System.getProperty("org.kie.server.db.port");
            String db = System.getProperty("org.kie.server.db.name");
            String username = System.getProperty("org.kie.server.db.username");
            String password = System.getProperty("org.kie.server.db.password");
            container.fraction(new DatasourcesFraction().jdbcDriver("postgresql", (d) -> {
                d.driverClassName("org.postgresql.Driver");
                d.xaDatasourceClass("org.postgresql.xa.PGXADataSource");
                d.driverModuleName("org.postgresql.postgresql");
            }).dataSource("ExampleDS", (ds) -> {
                ds.driverName("postgresql");
                ds.connectionUrl("jdbc:postgresql://" + dbhost + ":" + dbport + "/" + db);
                ds.userName(username);
                ds.password(password);
            }));

            System.setProperty("org.kie.server.persistence.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            System.setProperty("org.kie.server.persistence.ds", "java:jboss/datasources/ExampleDS");
            
        }
        // configure transactions
        container.fraction(TransactionsFraction.createDefaultFraction());

        System.out.println("\tBuilding kie server deployable...");
        JAXRSArchive deployment = createDeployment(container);

        System.out.println("\tStarting Wildfly Swarm....");
        container.start();

        System.out.println("\tConfiguring kjars to be auto deployed to server " + Arrays.toString(args));
        installKJars(args);

        System.out.println("\tDeploying kie server ....");
        container.deploy(deployment);
    }
}
