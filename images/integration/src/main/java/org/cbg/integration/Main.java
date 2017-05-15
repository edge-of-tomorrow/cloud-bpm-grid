package org.cbg.integration;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {

    private static String HELP = "Usage: java -jar cbg-integration.jar path to Camel context file";

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1 || "-h".equals(args[0])) {
            System.out.println(HELP);
            return;
        }

        File contextFile = new File(args[0]);
        if (!contextFile.exists()) {
            throw new FileNotFoundException("Camel context file was not found on path " + contextFile.getAbsolutePath());
        }

        startCamel(contextFile.getAbsolutePath());
    }

    private static void startCamel(final String springContextFileUri) {
        org.apache.camel.spring.Main camelMain = new org.apache.camel.spring.Main();
        camelMain.setFileApplicationContextUri("file:" + springContextFileUri);

        try {
            camelMain.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
