package io.gdcc.export.iso19115_3;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ISO_Validator {
    private static final Logger logger = LoggerFactory.getLogger(ISO_Validator.class);

    public static void validate(OutputStream xmlFile, String schemaDir) throws IOException, SAXException {
        String path = "19115-3/"; // folder inside resources
        URL dirURL = ISO_Validator.class.getClassLoader().getResource(path);

        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
        JarFile jar = new JarFile(jarPath);

        logger.info("Jar path: {}", jarPath);

        Enumeration<JarEntry> entries = jar.entries();
        List<Source> schemaSources = new ArrayList<>();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(path) && name.substring(name.length() -4).equals(".xsd")) {// && !entry.isDirectory()) {
                System.out.println(name);
                schemaSources.add(new StreamSource(ISO_Validator.class.getClassLoader().getResourceAsStream(name)));
            }
        }
        jar.close();

        // Load all schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(schemaSources.toArray(new Source[0]));

                //factory.setResourceResolver(new LocalResourceResolver("/home/lubitchv/work/exporter-iso19115-3/src/main/resources/all_xsds"));

        // Create a validator
        Validator validator = schema.newValidator();

        // Attach custom error handler
        //validator.setErrorHandler(new ValidationErrorHandler());

        InputStream in = new ByteArrayInputStream(xmlFile.toString().getBytes());

        // Validate
        validator.validate(new StreamSource(in));

        logger.info("XML is valid against ISO 19115-3 schemas.");
    }
////        if (!xmlFile.exists()) {
////            throw new IOException("XML file not found: " + xmlFile);
////        }
////        if (!schemaDir.exists() || !schemaDir.isDirectory()) {
////            throw new IOException("Schema directory not found: " + schemaDir);
////        }
//
//        // Collect all XSD files in the schema directory (recursively)
//        List<Source> schemaSources = new ArrayList<>();
//        collectXSDs(schemaDir, schemaSources);
////        for (Source source : schemaSources) {
////            System.out.println("Found schema: " + source.getSystemId());
////        }
////
////        // Load all schemas
////        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
////        Schema schema = factory.newSchema(schemaSources.toArray(new Source[0]));
////
////        //factory.setResourceResolver(new LocalResourceResolver("/home/lubitchv/work/exporter-iso19115-3/src/main/resources/all_xsds"));
////
////        // Create a validator
////        Validator validator = schema.newValidator();
////
////        // Attach custom error handler
////        validator.setErrorHandler(new ValidationErrorHandler());
////
////        // Validate
////        validator.validate(new StreamSource(xmlFile));
////
////        System.out.println("XML is valid against ISO 19115-3 schemas.");
//    }
//
//    private static void collectXSDs(String path, List<Source> schemaSources) throws IOException {
//        JarFile jar = new JarFile(path);
//        Enumeration<JarEntry> entries = jar.entries();
//        while (entries.hasMoreElements()) {
//            JarEntry entry = entries.nextElement();
//            String name = entry.getName();
//            if (name.startsWith(path) && !entry.isDirectory()) {
//                System.out.println(name);
//
//            }
//        }
//        jar.close();
//
//
////        File[] files = path.listFiles();
////        if (files == null) return;
////
////        for (File file : files) {
////            if (file.isDirectory()) {
////                collectXSDs(file, schemaSources);
////            } else if (file.getName().endsWith(".xsd")) {
////                schemaSources.add(new StreamSource(file));
////            }
////        }
//    }
//
//    // Custom error handler
//    private static class ValidationErrorHandler implements ErrorHandler {
//        @Override
//        public void warning(SAXParseException exception) {
//            System.err.println("[WARNING] Line " + exception.getLineNumber() + ": " + exception.getMessage());
//        }
//
//        @Override
//        public void error(SAXParseException exception) {
//            System.err.println("[ERROR] Line " + exception.getLineNumber() + ": " + exception.getMessage());
//        }
//
//        @Override
//        public void fatalError(SAXParseException exception) throws SAXException {
//            System.err.println("[FATAL] Line " + exception.getLineNumber() + ": " + exception.getMessage());
//            throw exception; // stop validation on fatal error
//        }
//    }
}
