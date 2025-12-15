package io.gdcc.export.iso19115_3;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ISO_Validator {
    private static final Logger logger = LoggerFactory.getLogger(ISO_Validator.class);

    private final Schema schema;

    public ISO_Validator(String mainXsdPath) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Build resolver with automatic index of all XSDs in classpath/JAR
        AutoResourceResolver resolver = new AutoResourceResolver();
        factory.setResourceResolver(resolver);

        try (InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(mainXsdPath)) {
            if (xsdStream == null) {
                throw new IllegalArgumentException("XSD not found in resources: " + mainXsdPath);
            }
            this.schema = factory.newSchema(new StreamSource(xsdStream));
        }
    }

    public void validate(InputStream xmlStream) throws Exception {
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlStream));
    }

    /**
     * ResourceResolver that scans the JAR/classpath and indexes all XSDs.
     */
    static class AutoResourceResolver implements LSResourceResolver {

        @Override
        public LSInput resolveResource(
                String type,
                String namespaceURI,
                String publicId,
                String systemId,
                String baseURI) {

            if (systemId == null) {
                logger.warn("systemId is null");
                return null;
            }
            logger.info("Resolving resource " + systemId + " for namespace " + namespaceURI + "");
            if (systemId.equals("xlink.xsd")) {
                namespaceURI = "/isotc211.org/19115/-3/gco/1.0";
            }
            String resourcePath = "/";
            namespaceURI = namespaceURI.replace("isotc211.org", "iso");
            int index = namespaceURI.indexOf("iso/");
            if (index != -1) {
                resourcePath = namespaceURI.substring(index);
                logger.info("Resolved " + resourcePath);
            } else {
                logger.warn("Could not resolve, cannot find iso,  namespaceURI: " + namespaceURI  );
                return null;
            }

            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourcePath + "/" + systemId);

            if (resourceAsStream == null) {
                logger.warn("Could not resolve " + resourcePath + "/" + systemId);
                return null; // let default resolver handle it
            }

            return new SimpleLSInput(publicId, systemId, resourceAsStream);
        }

        // Simple LSInput implementation
        private static class SimpleLSInput implements LSInput {
            private String publicId;
            private String systemId;
            private InputStream inputStream;

            public SimpleLSInput(String publicId, String systemId, InputStream inputStream) {
                this.publicId = publicId;
                this.systemId = systemId;
                this.inputStream = inputStream;
            }

            @Override
            public Reader getCharacterStream() {
                return null;
            }

            @Override
            public void setCharacterStream(Reader reader) {
            }

            @Override
            public InputStream getByteStream() {
                return inputStream;
            }

            @Override
            public void setByteStream(InputStream inputStream) {
                this.inputStream = inputStream;
            }

            @Override
            public String getStringData() {
                return null;
            }

            @Override
            public void setStringData(String s) {
            }

            @Override
            public String getSystemId() {
                return systemId;
            }

            @Override
            public void setSystemId(String systemId) {
                this.systemId = systemId;
            }

            @Override
            public String getPublicId() {
                return publicId;
            }

            @Override
            public void setPublicId(String publicId) {
                this.publicId = publicId;
            }

            @Override
            public String getBaseURI() {
                return null;
            }

            @Override
            public void setBaseURI(String baseURI) {
            }

            @Override
            public String getEncoding() {
                return null;
            }

            @Override
            public void setEncoding(String encoding) {
            }

            @Override
            public boolean getCertifiedText() {
                return false;
            }

            @Override
            public void setCertifiedText(boolean certifiedText) {
            }
        }
    }





//        private final Map<String, String> xsdIndex = new HashMap<>();
//
//        public AutoResourceResolver() {
//            logger.info("Start AutoResourceResolver");
//            try {
//                // Locate the JAR that contains our resources
//                String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
//                logger.info("path: " + path);
//
//                if (path.endsWith(".jar")) {
//                    try (JarFile jarFile = new JarFile(path)) {
//                        Enumeration<JarEntry> entries = jarFile.entries();
//                        while (entries.hasMoreElements()) {
//                            JarEntry entry = entries.nextElement();
//                            if (entry.getName().endsWith(".xsd")) {
//                                String fileName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
//                                xsdIndex.put(fileName, entry.getName());
//                                xsdIndex.put(entry.getName(), entry.getName());
//                            }
//                        }
//                    }
//                    logger.info("Printing xsdIndex");
//                    for (Map.Entry<String, String> entry : xsdIndex.entrySet()) {
//                        System.out.println(entry.getKey() + " = " + entry.getValue());
//                    }
//                    logger.info("End AutoResourceResolver");
//                } else {
//                    // Fallback: in dev mode (classes/resources directory)
//                    // Just rely on ClassLoader lookups
//                }
//
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public LSInput resolveResource(
//                String type,
//                String namespaceURI,
//                String publicId,
//                String systemId,
//                String baseURI) {
//            String resourcePath = "/";
//            logger.info("Resolving " + type + " " + namespaceURI + " " + publicId + " " + systemId + " " + baseURI);
//
//            int index = namespaceURI.indexOf("19115");
//            if (index != -1) {
//                resourcePath = namespaceURI.substring(index);
//            }
//            logger.info("Resolved " + resourcePath);
//            if (systemId == null) return null;

//            String resolvedPath = systemId;
//
//
//            // Try resolving relative paths
//            if (baseURI != null) {
//                try {
//                    URI base = new URI(baseURI);
//                    URI resolved = base.resolve(systemId);
//                    resolvedPath = resolved.getPath();
//                    logger.info("Resolved path: " + resolvedPath);
//                    if (resolvedPath.startsWith("/")) resolvedPath = resolvedPath.substring(1);
//                } catch (URISyntaxException ignored) {}
//            }
//
//            // Try exact path lookup
//            String resourcePath = xsdIndex.get(resolvedPath);
//            if (resourcePath == null) {
//                logger.info("resourcePath is null");
//                logger.info("Resolved path2: " + resolvedPath);
//                // Try lookup by file name only
//                String fileName = resolvedPath.substring(resolvedPath.lastIndexOf('/') + 1);
//                logger.info("fileName: " + fileName);
//                for (Map.Entry<String, String> entry : xsdIndex.entrySet()) {
//                    System.out.println(entry.getKey() + " = " + entry.getValue());
//                }
//                resourcePath = xsdIndex.get(fileName);
//                logger.info("ResourcePath after: " + resourcePath);
//            }
//
//            if (resourcePath == null) {
//                System.err.println("Warning: Could not find XSD in resources: " + resolvedPath);
//                return null;
//            }
//
//            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath + "/" + systemId);
//            if (resourceStream == null) {
//                System.err.println("Warning: Resource lookup failed for: " + resourcePath);
//                return null;
//            }
//
//            final String sysId = systemId;
//            final String pubId = publicId;
//            final String base = baseURI;
//
//            return new LSInput() {
//                @Override public Reader getCharacterStream() { return null; }
//                @Override public void setCharacterStream(Reader characterStream) {}
//                @Override public InputStream getByteStream() { return resourceStream; }
//                @Override public void setByteStream(InputStream byteStream) {}
//                @Override public String getStringData() { return null; }
//                @Override public void setStringData(String stringData) {}
//                @Override public String getSystemId() { return sysId; }
//                @Override public void setSystemId(String systemId) {}
//                @Override public String getPublicId() { return pubId; }
//                @Override public void setPublicId(String publicId) {}
//                @Override public String getBaseURI() { return base; }
//                @Override public void setBaseURI(String baseURI) {}
//                @Override public String getEncoding() { return null; }
//                @Override public void setEncoding(String encoding) {}
//                @Override public boolean getCertifiedText() { return false; }
//                @Override public void setCertifiedText(boolean certifiedText) {}
//            };
//        }
//    }

}



//    public static void validate(OutputStream xmlFile, String schemaDir) throws IOException, SAXException {
////        String path = "19115-3-schemas/"; // folder inside resources
////        URL dirURL = ISO_Validator.class.getClassLoader().getResource(path);
////
////        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
////        JarFile jar = new JarFile(jarPath);
////
////        logger.info("Jar path: {}", jarPath);
////
////        Enumeration<JarEntry> entries = jar.entries();
////        List<Source> schemaSources = new ArrayList<>();
////        while (entries.hasMoreElements()) {
////            JarEntry entry = entries.nextElement();
////            String name = entry.getName();
////            if (name.startsWith(path) && name.substring(name.length() -7).equals("mdb.xsd")) {// && !entry.isDirectory()) {
////                System.out.println(name);
////                schemaSources.add(new StreamSource(ISO_Validator.class.getClassLoader().getResourceAsStream(name)));
////            }
////        }
////        jar.close();
//
//        // Load all 19115-3
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        //Schema schema = factory.newSchema(schemaSources.toArray(new Source[0]));
//
//                //factory.setResourceResolver(new LocalResourceResolver("/home/lubitchv/work/exporter-iso19115-3/src/main/resources/all_xsds"));
//
//
//
//        InputStream in = new ByteArrayInputStream(xmlFile.toString().getBytes());
//
//        // Load entry schema (mdb.xsd)
//        try (InputStream schemaStream = ISO_Validator.class.getClassLoader()
//                .getResourceAsStream("mdb.xsd")) {
//
//            Schema schema = factory.newSchema(new StreamSource(schemaStream));
//            Validator validator = schema.newValidator();
//
//            // Validate XML
//
//                validator.validate(new StreamSource(in));
//                System.out.println("Validation successful");
//        }
//
//
////        // Create a validator
////        Validator validator = schema.newValidator();
////
////        // Attach custom error handler
////        //validator.setErrorHandler(new ValidationErrorHandler());
////
////
////
////        // Validate
////        validator.validate(new StreamSource(in));
//
//        logger.info("XML is valid against ISO 19115-3 19115-3.");
//    }
//////        if (!xmlFile.exists()) {
//////            throw new IOException("XML file not found: " + xmlFile);
//////        }
//////        if (!schemaDir.exists() || !schemaDir.isDirectory()) {
//////            throw new IOException("Schema directory not found: " + schemaDir);
//////        }
////
////        // Collect all XSD files in the schema directory (recursively)
////        List<Source> 19115-3ources = new ArrayList<>();
////        collectXSDs(schemaDir, 19115-3ources);
//////        for (Source source : 19115-3ources) {
//////            System.out.println("Found schema: " + source.getSystemId());
//////        }
//////
//////        // Load all 19115-3
//////        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//////        Schema schema = factory.newSchema(19115-3ources.toArray(new Source[0]));
//////
//////        //factory.setResourceResolver(new LocalResourceResolver("/home/lubitchv/work/exporter-iso19115-3/src/main/resources/all_xsds"));
//////
//////        // Create a validator
//////        Validator validator = schema.newValidator();
//////
//////        // Attach custom error handler
//////        validator.setErrorHandler(new ValidationErrorHandler());
//////
//////        // Validate
//////        validator.validate(new StreamSource(xmlFile));
//////
//////        System.out.println("XML is valid against ISO 19115-3 19115-3.");
////    }
////
////    private static void collectXSDs(String path, List<Source> 19115-3ources) throws IOException {
////        JarFile jar = new JarFile(path);
////        Enumeration<JarEntry> entries = jar.entries();
////        while (entries.hasMoreElements()) {
////            JarEntry entry = entries.nextElement();
////            String name = entry.getName();
////            if (name.startsWith(path) && !entry.isDirectory()) {
////                System.out.println(name);
////
////            }
////        }
////        jar.close();
////
////
//////        File[] files = path.listFiles();
//////        if (files == null) return;
//////
//////        for (File file : files) {
//////            if (file.isDirectory()) {
//////                collectXSDs(file, 19115-3ources);
//////            } else if (file.getName().endsWith(".xsd")) {
//////                19115-3ources.add(new StreamSource(file));
//////            }
//////        }
////    }
////
////    // Custom error handler
////    private static class ValidationErrorHandler implements ErrorHandler {
////        @Override
////        public void warning(SAXParseException exception) {
////            System.err.println("[WARNING] Line " + exception.getLineNumber() + ": " + exception.getMessage());
////        }
////
////        @Override
////        public void error(SAXParseException exception) {
////            System.err.println("[ERROR] Line " + exception.getLineNumber() + ": " + exception.getMessage());
////        }
////
////        @Override
////        public void fatalError(SAXParseException exception) throws SAXException {
////            System.err.println("[FATAL] Line " + exception.getLineNumber() + ": " + exception.getMessage());
////            throw exception; // stop validation on fatal error
////        }
////    }
//}
