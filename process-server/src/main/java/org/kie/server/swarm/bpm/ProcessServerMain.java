package org.kie.server.swarm.bpm;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.kie.server.util.NotifyOwnersByEmailTaskListener;
import org.kie.server.util.TemplateConfiguration;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.keycloak.Secured;
import org.wildfly.swarm.mail.MailFraction;
import org.wildfly.swarm.transactions.TransactionsFraction;

public class ProcessServerMain {

    public static void main(String[] args) throws Exception {

        Swarm container = new Swarm();

        // Configure the Datasources subsystem with a driver and a datasource
        boolean inMemory = Boolean.valueOf(System.getProperty("org.kie.server.inmemory"));
        if (inMemory) {
            System.setProperty("org.kie.server.persistence.dialect", "org.hibernate.dialect.H2Dialect");
            System.setProperty("org.kie.server.persistence.ds", "java:jboss/datasources/ExampleDS");
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
            }).dataSource("PostgreDS", (ds) -> {
                ds.driverName("postgresql");
                ds.connectionUrl("jdbc:postgresql://" + dbhost + ":" + dbport + "/" + db);
                ds.userName(username);
                ds.password(password);
            }));

            System.setProperty("org.kie.server.persistence.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            System.setProperty("org.kie.server.persistence.ds", "java:jboss/datasources/PostgreDS");

        }

        // configure mail server

        container.fraction(MailFraction.defaultFraction()); // localhost:25
        System.setProperty("org.kie.mail.session", "java:jboss/mail/Default");

        // configure transactions
        container.fraction(TransactionsFraction.createDefaultFraction());

        System.out.println("\tBuilding kie server deployable...");
        File war = new File("target/kie-server-1.0-SNAPSHOT.war");
        WebArchive deployment = ShrinkWrap.create(ZipImporter.class, "kie-server.war").importFrom(war)
                .as(WebArchive.class);

        ClassLoaderAsset webxml = new ClassLoaderAsset("/config/web/web.xml", ProcessServerMain.class.getClassLoader());
        deployment.addAsWebInfResource(webxml, "web.xml");

        ClassLoaderAsset userInfo = new ClassLoaderAsset("/config/jbpm.user.info.properties",
                ProcessServerMain.class.getClassLoader());
        deployment.addAsWebInfResource(userInfo, "classes/jbpm.user.info.properties");
        System.setProperty("org.jbpm.ht.userinfo", "db");

        deployment.addClass(NotifyOwnersByEmailTaskListener.class);
        deployment.addClass(TemplateConfiguration.class);
        //ClassLoaderAsset templates = new ClassLoaderAsset("/templates", ProcessServerMain.class.getClassLoader());
        Set<String> templates = new Reflections("templates", new ResourcesScanner()).getResources(Pattern.compile(".*"));
        //deployment.addAsResource(templates, "classes/templates");
        for (String t : templates) {
            deployment.addAsWebInfResource(new ClassLoaderAsset("/" + t, ProcessServerMain.class.getClassLoader()), "classes/" + t);
        }

        deployment.as(Secured.class);

        System.out.println("\tStarting Wildfly Swarm....");
        container.start();

        System.out.println("\tDeploying kie server ....");
        container.deploy(deployment);
    }

}
