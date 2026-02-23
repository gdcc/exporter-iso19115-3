package io.gdcc.export.iso19115_3;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import java.io.*;

import java.net.InetAddress;
import java.net.URL;

import java.net.UnknownHostException;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
class Dataset {
    @JsonProperty("publicationDate")
    public String publicationDate;

    @JsonProperty("identifier")
    private String identifier;

    @JsonProperty("authority")
    private String authority;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("datasetVersion")
    private DatasetVersion datasetVersion;

    String getIdentifier() {
        return identifier;
    }

    String getAuthority() {
        return authority;
    }

    String getProtocol() {
        return protocol;
    }
    
    DatasetVersion getDatasetVersion() { 
        return datasetVersion; 
    }

}
@JsonIgnoreProperties(ignoreUnknown = true)
class DatasetVersion {

    @JsonProperty("metadataBlocks")
    private Map<String, MetadataBlock> metadataBlocks;

    @JsonProperty("files")
    private ArrayList<DataverseFiles> files;

    @JsonProperty("termsOfUse")
    private String termsOfUse;

    @JsonProperty("restrictions")
    private String restrictions;

    @JsonProperty("citationrequirements")
    private String citationrequirements;

    @JsonProperty("depositorrequirements")
    private String depositorrequirements;

    @JsonProperty("conditions")
    private String conditions;

    @JsonProperty("disclaimer")
    private String disclaimer;

    @JsonProperty("originalArchive")
    private String originalArchive;

    public Map<String, MetadataBlock> getMetadataBlocks() {
        return metadataBlocks;
    }

    public ArrayList<DataverseFiles> getFiles() {
        return files;
    }

    public String getTermsOfUse() {
        return termsOfUse;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public String getCitationrequirements() {
        return citationrequirements;
    }

    public String getDepositorrequirements() {
        return depositorrequirements;
    }

    public String getConditions() {
        return conditions;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public String getOriginalArchive() {
        return originalArchive;
    }
}

class MetadataBlock {
    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("fields")
    private List<Field> fields;

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class DataverseFiles {
    @JsonProperty("description")
    private String description;

    @JsonProperty("label")
    private String label;

    @JsonProperty("restricted")
    private boolean restricted;

    @JsonProperty("directoryLabel")
    private String directoryLabel;

    @JsonProperty("dataFile")
    private DataFile dataFile;

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public boolean getRestricted() {
        return restricted;
    }

    public String getDirectoryLabel() {
        return directoryLabel;
    }

    public DataFile getDataFile() {
        return dataFile;
    }
}
@JsonIgnoreProperties(ignoreUnknown = true)
class DataFile {
    @JsonProperty("id")
    private int id;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("contentType")
    private String contentType;

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "typeClass",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrimitiveField.class, name = "primitive"),
        @JsonSubTypes.Type(value = CompoundField.class, name = "compound"),
        @JsonSubTypes.Type(value = ControlledVocabularyField.class, name = "controlledVocabulary")
})

abstract class Field {
    @JsonProperty("typeName")
    protected String typeName;

    @JsonProperty("multiple")
    protected boolean multiple;

    @JsonProperty("typeClass")
    protected String typeClass;

    public boolean isMultiple() {
        return multiple;
    }
    
    public String getTypeName() {
        return typeName;
    }

}
@JsonIgnoreProperties(ignoreUnknown = true)
class PrimitiveField extends Field {
    @JsonProperty("value")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> value;  // Jackson will coerce single string -> list

    public String getSingleValue() {
        return (value != null && !value.isEmpty()) ? value.get(0) : null;
    }

    public List<String> getMultipleValues() {
        return value;
    }

    // getters/setters
}

class CompoundField extends Field {
    @JsonProperty("value")

    private List<HashMap<String, Field>> value;

    public List<HashMap<String, Field>> getMultipleValues() {
        return value;
    }

    public HashMap<String, Field> getSingleValue() {
        return (value != null && !value.isEmpty()) ? value.get(0) : null;
    }

}

@JsonIgnoreProperties(ignoreUnknown = true)
class ControlledVocabularyField extends Field {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty("value")
    private List<String> value;

    public String getSingleValue() {
        return (value != null && !value.isEmpty()) ? value.get(0) : null;
    }

    public List<String> getMultipleValues() {
        return value;
    }
}



public class ISO19115_3ExportUtil {

    private static final Logger logger = LoggerFactory.getLogger(ISO19115_3ExportUtil.class);

    public static class TitleAndDescription {
        public String Title;
        public String Description;
        public String Language;
    }

    private ISO19115_3ExportUtil() {
        // As this is a util class, adding a private constructor disallows instances of this class.
    }

    private static Properties getPropValues() throws Exception {

        Properties prop = null;
        try {
            InputStream inputStream;
            prop = new Properties();

            String propFileName = "configs/config.properties";
                inputStream = ISO19115_3ExportUtil.class.getClassLoader().getResourceAsStream(propFileName);
                if (inputStream != null) {
                    prop.load(inputStream);
                    inputStream.close();
                } else {
                    throw new FileNotFoundException("Property configs/config.properties file not found");
                }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("Config file was not loaded");
        }
        return prop;
    }

    public static void parseDataverseJson(InputStream jsonInputStream, OutputStream outputStream) throws Exception {
        Properties prop  = getPropValues();
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Read json dataverse");
            Dataset dataset = mapper.readValue(jsonInputStream, Dataset.class);

            MetadataBlock citation = dataset.getDatasetVersion().getMetadataBlocks().get("citation");
            MetadataBlock geospatial = dataset.getDatasetVersion().getMetadataBlocks().get("geospatial");

            String restrictions = dataset.getDatasetVersion().getRestrictions();
            String citationrequirements = dataset.getDatasetVersion().getCitationrequirements();
            String depositorrequirements = dataset.getDatasetVersion().getDepositorrequirements();
            String conditions = dataset.getDatasetVersion().getConditions();
            String disclaimer = dataset.getDatasetVersion().getDisclaimer();

            ArrayList<DataverseFiles> dataversFiles = dataset.getDatasetVersion().getFiles();

            logger.info("Block: " + citation.getDisplayName());
            //logger.info("Block: " + geospatial.getDisplayName());

            Field geographicBoundingBox = null;
            Field otherId = null;
            Field language = null;
            Field keyword = null;
            Field referenceSystemInfo = null;
            Field author = null;
            Field title = null;
            Field alternativeTitle = null;
            Field distributionDate = null;
            Field distribution = null;
            Field referenceDate = null;
            Field topicClassification = null;
            Field note = null;
            Field series = null;
            Field software = null;
            Field lineageStatement = null;
            Field sourceDescription = null;
            Field processStep = null;
            Field spatialResolution = null;
            Field spatialRepresentationType = null;
            Field geometricObjectCount = null;
            Field geometricObjectType = null;
            Field datasetContact = null;
            Field description = null;
            Field publication = null;
            Field producer = null;
            Field distributor = null;
            Field dateOfDeposit = null;
            Field timePeriodCovered = null;
            Field otherReferences = null;
            Field characteristicOfSources = null;
            Field geographicCoverage = null;
            Field contributor = null;
            Field geographicUnit = null;
            Field productionDate = null;
            Field resourceType = null;
            Field numberOfDimensions = null;
            Field axisDimensionProperties = null;
            Field cellGeometry = null;
            //Field relatedDatasets = null;


            for (Field f : citation.getFields()) {
                if (f.getTypeName().equals("otherId")) {
                    otherId = f;
                }
                if (f.getTypeName().equals("language")) {
                    language = f;
                }
                if (f.getTypeName().equals("keyword")) {
                    keyword = f;
                }
                if (f.getTypeName().equals("publication")) {
                    publication = f;
                }
                if (f.getTypeName().equals("producer")) {
                    producer = f;
                }
                if (f.getTypeName().equals("distributor")) {
                    distributor = f;
                }
                if (f.getTypeName().equals("dateOfDeposit")) {
                    dateOfDeposit = f;
                }
                if (f.getTypeName().equals("timePeriodCovered")) {
                    timePeriodCovered = f;
                }

                if (f.getTypeName().equals("author")) {
                    author = f;
                }
                if (f.getTypeName().equals("title")) {
                    title = f;
                }
                if (f.getTypeName().equals("alternativeTitle")) {
                    alternativeTitle = f;
                }
                if (f.getTypeName().equals("distributionDate")) {
                    distributionDate = f;
                }
                if (f.getTypeName().equals("topicClassification")) {
                    topicClassification = f;
                }
                if (f.getTypeName().equals("notesText")) {
                    note = f;
                }
                if (f.getTypeName().equals("series")) {
                    series = f;
                }
                if (f.getTypeName().equals("software")) {
                    software = f;
                }
                if (f.getTypeName().equals("datasetContact")) {
                    datasetContact = f;
                }
                if (f.getTypeName().equals("dsDescription")) {
                    logger.info("Found description field");
                    description = f;
                }
//                if (f.getTypeName().equals("dataSources")) {
//                    sourceDescription = f;
//                }

                if (f.getTypeName().equals("otherReferences")) {
                    otherReferences = f;
                }

                if (f.getTypeName().equals("characteristicOfSources")) {
                    characteristicOfSources = f;
                }

                if (f.getTypeName().equals("contributor")) {
                    contributor = f;
                }

                if (f.getTypeName().equals("productionDate")) {
                    productionDate = f;
                }


            }
            if (geospatial != null) {

                for (Field f : geospatial.getFields()) {
                    if (f.getTypeName().equals("geographicBoundingBox")) {
                        geographicBoundingBox = f;
                    }
                    if (f.getTypeName().equals("referenceSystemInfo")) {
                        referenceSystemInfo = f;
                    }
                    if (f.getTypeName().equals("distribution")) {
                        distribution = f;
                    }
                    if (f.getTypeName().equals("referenceDate")) {
                        referenceDate = f;
                    }
                    if (f.getTypeName().equals("dataLineageStatement")) {
                        lineageStatement = f;
                    }

                    if (f.getTypeName().equals("processStep")) {
                        processStep = f;
                    }
                    if (f.getTypeName().equals("spatialResolution")) {
                        spatialResolution = f;
                    }
                    if (f.getTypeName().equals("spatialRepresentationType")) {
                        spatialRepresentationType = f;
                    }
                    if (f.getTypeName().equals("geometricObjectCount")) {
                        geometricObjectCount = f;
                    }
                    if (f.getTypeName().equals("geometricObjectType")) {
                        geometricObjectType = f;
                    }
                    if (f.getTypeName().equals("geographicCoverage")) {
                        geographicCoverage = f;
                    }
                    if (f.getTypeName().equals("geographicUnit")) {
                        geographicUnit = f;
                    }
                    if (f.getTypeName().equals("resourceType")) {
                        resourceType = f;
                    }
                    if (f.getTypeName().equals("numberOfDimensions")) {
                        numberOfDimensions = f;
                    }
                    if (f.getTypeName().equals("axisDimensionProperties")) {
                        axisDimensionProperties = f;
                    }
                    if (f.getTypeName().equals("cellGeometry")) {
                        cellGeometry = f;
                    }
                }
            }


            logger.info("Finished reading json");


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter xmlw = XMLOutputFactory.newInstance().createXMLStreamWriter(baos);
            //XMLStreamWriter xmlw = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            xmlw.writeStartDocument();
            xmlw.writeStartElement("mdb:MD_Metadata");
            writeNamespaces(xmlw);
            writeDatasetPersistentId(xmlw, dataset.getIdentifier(), dataset.getAuthority(), dataset.getProtocol());
            writeDefaultLocale(xmlw, language);
            writeEmptyPrereq(xmlw);
            writeMetadataScope(xmlw, resourceType);

            logger.info("Before writeDateInfo");
            writeDateInfo(xmlw, description, "revised");
            writeDateDepositeInfo(xmlw, dateOfDeposit);
            //writeAlternativeMetadataReferenceForDataset(xmlw);
            String newOtherId = writeAlternativeMetadataReference(xmlw, otherId);
            writeSpatialRepresentationInfo(xmlw, geometricObjectCount, geometricObjectType, numberOfDimensions,
                    axisDimensionProperties, cellGeometry);
            writeReferenceSystemInfo(xmlw, referenceSystemInfo);
            writeIdentificationInfo(xmlw, geographicBoundingBox, keyword, author,
                    title, alternativeTitle, distributionDate, referenceDate,
                    topicClassification, note, series, software,
                    spatialResolution, spatialRepresentationType, dataset.getDatasetVersion().getTermsOfUse(),
                    datasetContact, description, publication, producer, timePeriodCovered, otherReferences, geographicCoverage,
                    contributor, geographicUnit, productionDate, restrictions, citationrequirements,
                    depositorrequirements, conditions, disclaimer, dataversFiles, prop);
            writeDistributionInfo(xmlw, distribution, distributor, dataversFiles, newOtherId, prop);
            writeResourceLineage(xmlw, lineageStatement, processStep, characteristicOfSources); //unclear
            writeMetadataMaintenance(xmlw, dataset.getDatasetVersion().getOriginalArchive());
            xmlw.writeEndElement(); // MD_Metadata
            xmlw.writeEndDocument();
            xmlw.flush();
            xmlw.close();
            logger.info("Befor validation starting");
            InputStream in = new ByteArrayInputStream(baos.toByteArray());

//
            ISO_Validator isoVal = new ISO_Validator("iso/19115/-3/mdb/2.0/mdb.xsd");
            isoVal.validate(in);
            logger.info("After validation ending");
            outputStream.write(baos.toByteArray());
            logger.info("After write to outputstream");

        } catch (XMLStreamException xse) {
            throw new RuntimeException(xse);
        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("XML Validation Error: " + e.getMessage());
            throw new RuntimeException(e);
        }


//        } catch (Exception e) {
//            logger.info("XML Error");
//            logger.error(e.getMessage());
//        }

    }

    private static void writeDateDepositeInfo(XMLStreamWriter xmlw, Field dateOfDeposit) throws XMLStreamException {
        logger.info("writeDateDepositeInfo");
        if (dateOfDeposit != null) {
            xmlw.writeStartElement("mdb:dateInfo");
            String date = ((PrimitiveField) dateOfDeposit).getSingleValue();
            if (date != null && !date.isEmpty()) {
                dateISO(xmlw, date, "released");
            }
            xmlw.writeEndElement();
        }
    }

    private static void writeNamespaces(XMLStreamWriter xmlw) throws XMLStreamException {
        xmlw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xmlw.writeAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        xmlw.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2");
        xmlw.writeAttribute("xmlns:cat", "http://standards.iso.org/iso/19115/-3/cat/1.0");
        xmlw.writeAttribute("xmlns:gco", "http://standards.iso.org/iso/19115/-3/gco/1.0");
        xmlw.writeAttribute("xmlns:gcx", "http://standards.iso.org/iso/19115/-3/gcx/1.0");
        xmlw.writeAttribute("xmlns:gex", "http://standards.iso.org/iso/19115/-3/gex/1.0");
        xmlw.writeAttribute("xmlns:lan", "http://standards.iso.org/iso/19115/-3/lan/1.0");
        xmlw.writeAttribute("xmlns:mas", "http://standards.iso.org/iso/19115/-3/mas/1.0");
        xmlw.writeAttribute("xmlns:mcc", "http://standards.iso.org/iso/19115/-3/mcc/1.0");
        xmlw.writeAttribute("xmlns:mco", "http://standards.iso.org/iso/19115/-3/mco/1.0");
        xmlw.writeAttribute("xmlns:mda", "http://standards.iso.org/iso/19115/-3/mda/1.0");
        xmlw.writeAttribute("xmlns:mdq", "http://standards.iso.org/iso/19157/-2/mdq/1.0");
        xmlw.writeAttribute("xmlns:mex", "http://standards.iso.org/iso/19115/-3/mex/1.0");
        xmlw.writeAttribute("xmlns:mmi", "http://standards.iso.org/iso/19115/-3/mmi/1.0");
        xmlw.writeAttribute("xmlns:mpc", "http://standards.iso.org/iso/19115/-3/mpc/1.0");
        xmlw.writeAttribute("xmlns:mrd", "http://standards.iso.org/iso/19115/-3/mrd/1.0");
        xmlw.writeAttribute("xmlns:mri", "http://standards.iso.org/iso/19115/-3/mri/1.0");
        xmlw.writeAttribute("xmlns:mrs", "http://standards.iso.org/iso/19115/-3/mrs/1.0");


        xmlw.writeAttribute("xmlns:cit", "http://standards.iso.org/iso/19115/-3/cit/2.0");
        xmlw.writeAttribute("xmlns:mac", "http://standards.iso.org/iso/19115/-3/mac/2.0");
        xmlw.writeAttribute("xmlns:mdb", "http://standards.iso.org/iso/19115/-3/mdb/2.0");
        xmlw.writeAttribute("xmlns:mds", "http://standards.iso.org/iso/19115/-3/mds/2.0");
        xmlw.writeAttribute("xmlns:mdt", "http://standards.iso.org/iso/19115/-3/mdt/2.0");
        xmlw.writeAttribute("xmlns:mrl", "http://standards.iso.org/iso/19115/-3/mrl/2.0");
        xmlw.writeAttribute("xmlns:mrc", "http://standards.iso.org/iso/19115/-3/mrc/2.0");
        xmlw.writeAttribute("xmlns:msr", "http://standards.iso.org/iso/19115/-3/msr/2.0");
        xmlw.writeAttribute("xmlns:srv", "http://standards.iso.org/iso/19115/-3/srv/2.0");

        //        xmlw.writeAttribute("xmlns:cat", "http://standards.iso.org/iso/19115/-3/cat/1.0");
//        xmlw.writeAttribute("xmlns:cit","http://standards.iso.org/iso/19115/-3/cit/2.0");
//        //xmlw.writeAttribute("xmlns:dc","http://purl.org/dc/terms/");
//        xmlw.writeAttribute("xmlns:gcx", "http://standards.iso.org/iso/19115/-3/gcx/1.0");
//        xmlw.writeAttribute("xmlns:gex", "http://standards.iso.org/iso/19115/-3/gex/1.0");
//        xmlw.writeAttribute("xmlns:lan", "http://standards.iso.org/iso/19115/-3/lan/1.0");
//        xmlw.writeAttribute("xmlns:srv", "http://standards.iso.org/iso/19115/-3/srv/2.0");
//        xmlw.writeAttribute("xmlns:mac", "http://standards.iso.org/iso/19115/-3/mac/2.0");
//        xmlw.writeAttribute("xmlns:mas", "http://standards.iso.org/iso/19115/-3/mas/1.0");
//        xmlw.writeAttribute("xmlns:mcc", "http://standards.iso.org/iso/19115/-3/mcc/1.0");
//        xmlw.writeAttribute("xmlns:mco", "http://standards.iso.org/iso/19115/-3/mco/1.0" );
//        xmlw.writeAttribute("xmlns:mda", "http://standards.iso.org/iso/19115/-3/mda/2.0");
//        xmlw.writeAttribute("xmlns:mdb","http://standards.iso.org/iso/19115/-3/mdb/2.0");
//        xmlw.writeAttribute("xmlns:mdt", "http://standards.iso.org/iso/19115/-3/mdt/1.0");
//        xmlw.writeAttribute("xmlns:mex", "http://standards.iso.org/iso/19115/-3/mex/1.0");
//        xmlw.writeAttribute("xmlns:mrl", "http://standards.iso.org/iso/19115/-3/mrl/2.0");
//        xmlw.writeAttribute("xmlns:mds","http://standards.iso.org/iso/19115/-3/mds/2.0");
//        xmlw.writeAttribute("xmlns:mmi", "http://standards.iso.org/iso/19115/-3/mmi/1.0");
//        xmlw.writeAttribute("xmlns:mpc", "http://standards.iso.org/iso/19115/-3/mpc/1.0");
//        xmlw.writeAttribute("xmlns:mrc", "http://standards.iso.org/iso/19115/-3/mrc/2.0");
//        xmlw.writeAttribute("xmlns:mrd", "http://standards.iso.org/iso/19115/-3/mrd/1.0");
//        xmlw.writeAttribute("xmlns:mri", "http://standards.iso.org/iso/19115/-3/mri/1.0");
//        xmlw.writeAttribute("xmlns:mrs", "http://standards.iso.org/iso/19115/-3/mrs/1.0");
//        xmlw.writeAttribute("xmlns:msr","http://standards.iso.org/iso/19115/-3/msr/2.0");
//        xmlw.writeAttribute("xmlns:mdq", "http://standards.iso.org/iso/19157/-2/mdq/1.0" );
//        xmlw.writeAttribute("xmlns:dqc", "http://standards.iso.org/iso/19157/-2/dqc/1.0");
//        xmlw.writeAttribute("xmlns:gco", "http://standards.iso.org/iso/19115/-3/gco/1.0");
//        xmlw.writeAttribute("xmlns:gfc", "http://standards.iso.org/iso/19110/gfc/1.1");
//        xmlw.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2" );
//        xmlw.writeAttribute("xmlns:xlink","http://www.w3.org/1999/xlink" );
//        xmlw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");

    }

    private static void writeMetadataScope(XMLStreamWriter xmlw, Field resourceType) throws XMLStreamException {
        //mdb:MD_Metadata/mdb:metadataScope/mdb:MD_MetadataScope
        String resType = "dataset"; //default
        if (resourceType != null) {
            resType = ((ControlledVocabularyField) resourceType).getSingleValue();
        }
        if (resType.isEmpty()) {
            resType = "dataset"; //default
        }
        if (resType != null && !resType.isEmpty()) {
            xmlw.writeStartElement("mdb:metadataScope");
            xmlw.writeStartElement("mdb:MD_MetadataScope");
//                <mdb:resourceScope>
//                        <mcc:MD_ScopeCode codeList="http://standards.iso.org/iso/19115/-3/mcc/1.0/codelists.xml#MD_ScopeCode"
//                codeListValue="dataset">dataset</mcc:MD_ScopeCode>
//                        </mdb:resourceScope>
//                        <mdb:name>
//                        <gco:CharacterString>Main dataset metadata</gco:CharacterString>
//                        </mdb:name>
            xmlw.writeStartElement("mdb:resourceScope");
            xmlw.writeStartElement("mcc:MD_ScopeCode");
            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_ScopeCode");
            xmlw.writeAttribute("codeListValue", resType);
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
            xmlw.writeCharacters(resType);
            xmlw.writeEndElement(); //mcc:MD_ScopeCode
            xmlw.writeEndElement(); //mdb:resourceScope
            xmlw.writeEndElement(); //mdb:MD_MetadataScope
            xmlw.writeEndElement(); //mdb:metadataScope
            xmlw.writeStartElement("mdb:contact");
            xmlw.writeEndElement();
        }

    }

    private static void writeDatasetPersistentId(XMLStreamWriter xmlw, String persistentId, String authority, String protocol) throws XMLStreamException {
        xmlw.writeStartElement("mdb:metadataIdentifier");
        xmlw.writeStartElement("mcc:MD_Identifier");
        xmlw.writeStartElement("mcc:authority");
        xmlw.writeStartElement("cit:CI_Citation");
        xmlw.writeStartElement("cit:title");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(protocol);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:title
        xmlw.writeStartElement("cit:presentationForm");
        xmlw.writeStartElement("cit:CI_PresentationFormCode");
        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_PresentationFormCode");
        xmlw.writeAttribute("codeListValue", "multimediaHardcopy");
        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
        xmlw.writeCharacters("multimediaHardcopy");
        xmlw.writeEndElement(); //cit:CI_PresentationFormCode
        xmlw.writeEndElement(); //cit:presentationForm
        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mcc:authority
        xmlw.writeStartElement("mcc:code");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(protocol + ":" + authority + "/" + persistentId);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //mcc:code
        xmlw.writeEndElement(); //mcc:MD_Identifier
        xmlw.writeEndElement(); //mdb:metadataIdentifier

    }

    private static void writeDefaultLocale(XMLStreamWriter xmlw, Field langF) throws XMLStreamException {

        logger.info("writeDefaultLocale");
        String language = "eng";
        if (langF != null) {
            for (String lang : ((ControlledVocabularyField) langF).getMultipleValues()) {

                if (lang.equals("English")) {
                    language = "eng";
                    break;
                } else if (lang.equals("French")) {
                    language = "fr";
                    break;
                }
            }
        }
//            if (!language.isEmpty() ) {
        xmlw.writeStartElement("mdb:defaultLocale");
        xmlw.writeStartElement("lan:PT_Locale");
        xmlw.writeStartElement("lan:language");
        xmlw.writeStartElement("lan:LanguageCode");
        xmlw.writeAttribute("codeList", "http://www.loc.gov/standards/iso639-2/");
        xmlw.writeAttribute("codeListValue", "eng");
        xmlw.writeCharacters(language);
        xmlw.writeEndElement(); //LanguageCode
        xmlw.writeEndElement(); //language
        xmlw.writeStartElement("lan:country");
        xmlw.writeStartElement("lan:CountryCode");
        xmlw.writeAttribute("codeList", "http://www.loc.gov/standards/iso366-1/");
        xmlw.writeAttribute("codeListValue", "can");
        xmlw.writeCharacters("can");
        xmlw.writeEndElement(); //CountryCode
        xmlw.writeEndElement(); //country
        xmlw.writeStartElement("lan:characterEncoding");
        xmlw.writeStartElement("lan:MD_CharacterSetCode");
        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode");
        xmlw.writeAttribute("codeListValue", "utf8");
        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
        xmlw.writeCharacters("utf8");
        xmlw.writeEndElement(); //MD_CharacterSetCode
        xmlw.writeEndElement(); //characterEncoding
        xmlw.writeEndElement(); //lan:PT_Locale
        xmlw.writeEndElement(); //mdb:defaultLocale

    }

    private static void writeEmptyPrereq(XMLStreamWriter xmlw) throws XMLStreamException {

        xmlw.writeStartElement("mdb:parentMetadata");
        xmlw.writeEndElement();

//        xmlw.writeStartElement("mdb:metadataScope");
//        xmlw.writeEndElement();
//
//        xmlw.writeStartElement("mdb:contact");
//        xmlw.writeEndElement();

//        <mdb:parentMetadata/>        <!-- optional, empty -->
//                <mdb:metadataScope/>         <!-- optional, empty -->
//                <mdb:contact/>               <!-- optional, empty -->
    }

    private static void writeDateInfo(XMLStreamWriter xmlw, Field descriptionF, String code) throws XMLStreamException {
        logger.info("writeDateInfo");
        if (descriptionF != null) {
            xmlw.writeStartElement("mdb:dateInfo");

            for (HashMap<String, Field> foo : ((CompoundField) descriptionF).getMultipleValues()) {

                logger.info("Got foo");
                String date = "";
                PrimitiveField descDate = (PrimitiveField) foo.get("dsDescriptionDate");
                logger.info("Date field found");
                if (descDate != null) {
                    date = descDate.getSingleValue();
                    logger.info(date);
                }
                if (date != null && !date.isEmpty()) {
                    //mdb:dateInfo/cit:CI_Date/cit:date/gco:DateTime
                    //code=publication
                    dateISO(xmlw, date, code); //For description (abstract) date code is not clear
                    break; //only first date
                }
            }
            xmlw.writeEndElement(); //mdb:dateInfo
        }
//        if (dateOfDepositF != null) {
//            String date = ((PrimitiveField) dateOfDepositF).getSingleValue();
//            if (date != null && !date.isEmpty()) {
//                dateISO(xmlw, date, "released");
//            }
//        }

    }


    private static void dateISO(XMLStreamWriter xmlw, String date, String code) throws XMLStreamException {
        logger.info("dateISO");
        xmlw.writeStartElement("cit:CI_Date");
        xmlw.writeStartElement("cit:date");
        xmlw.writeStartElement("gco:Date");
        xmlw.writeCharacters(date);
        xmlw.writeEndElement(); //gco:Date
        xmlw.writeEndElement(); //cit:date
        xmlw.writeStartElement("cit:dateType");
        xmlw.writeStartElement("cit:CI_DateTypeCode");
        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_DateTypeCode");
        xmlw.writeAttribute("codeListValue", code);
        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
        xmlw.writeCharacters(code);
        xmlw.writeEndElement(); //cit:CI_DateTypeCode
        xmlw.writeEndElement(); //cit:dateType
        xmlw.writeEndElement(); //cit:CI_Date
        logger.info("dateISO end");

    }

    private static String determineOtherIdDescription(String otherIdAgency) {
        if (otherIdAgency != null && !otherIdAgency.isEmpty()) {
            if (otherIdAgency.equals("ScholarsPortal-Old")) {
                return "GeoPortal Old Naming Conventions";
            }
            if (otherIdAgency.equals("ScholarsPortal")) {
                return "GeoPortal Naming Conventions [2025]";
            }
            if (otherIdAgency.equals("ScholarsPortal-FileID")) {
                return "The filename of the dataset's metadata record before the migration";
            }
        }

        return "Alternative metadata reference";
    }

    private static void writeAlternativeMetadataReferenceForDataset(XMLStreamWriter xmlw) throws XMLStreamException {

        xmlw.writeStartElement("mdb:alternativeMetadataReference");
        xmlw.writeStartElement("cit:CI_Citation");
        xmlw.writeStartElement("cit:title");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters("Alternative metadata reference 1: The GeoPortal landing page");
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:title
        xmlw.writeStartElement("cit:identifier");
        xmlw.writeStartElement("mcc:MD_Identifier");
        xmlw.writeStartElement("mcc:code");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters("[thisDataset]");
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //mcc:code
        xmlw.writeStartElement("mcc:codeSpace");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters("GeoPortal Archived Metadata Record");
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //mcc:codeSpace
        xmlw.writeStartElement("mcc:description");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters("Name of GeoPortal metadata landing page on site");
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //mcc:description
        xmlw.writeEndElement(); //mcc:MD_Identifier
        xmlw.writeEndElement(); //cit:identifier
        writeOnlineResource(xmlw, "https://geo.scholarsportal.info/thisDataset.html", "https", "Linkage for GeoPortal landing page for this dataset", null,"documentDigital");
        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mdb:alternativeMetadataReference
    }

    private static void writeOnlineResource(XMLStreamWriter xmlw, String linkage, String protocol, String description, String name, String function) throws XMLStreamException {
        //xmlw.writeStartElement("cit:onlineResource");
        xmlw.writeStartElement("cit:CI_OnlineResource");
//        if (name != null && !name.isEmpty()) {
//            xmlw.writeStartElement("cit:name");
//            xmlw.writeStartElement("gco:CharacterString");
//            xmlw.writeCharacters(name);
//            xmlw.writeEndElement(); //gco:CharacterString
//            xmlw.writeEndElement(); //cit:name
//        }
        xmlw.writeStartElement("cit:linkage");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(linkage);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:linkage
        xmlw.writeStartElement("cit:protocol");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(protocol);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:protocol
        if (name != null && !name.isEmpty()) {
            xmlw.writeStartElement("cit:name");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(name);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //cit:name
        }
        if (description != null && !description.isEmpty()) {
            xmlw.writeStartElement("cit:description");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(description);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //cit:description
        }
        if (function != null && !function.isEmpty()) {
            xmlw.writeStartElement("cit:function");
            xmlw.writeStartElement("cit:CI_OnLineFunctionCode");
            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_DateTypeCode");

            if (function.equals("documentDigital")) {
                xmlw.writeAttribute("codeListValue", "download");
            } else {
                xmlw.writeAttribute("codeListValue", function);
            }

            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
            xmlw.writeCharacters(function);
            xmlw.writeEndElement(); //cit:CI_OnLineFunctionCode
            xmlw.writeEndElement(); //cit:function
        }
        xmlw.writeEndElement(); //cit:CI_OnlineResource
        //xmlw.writeEndElement(); //cit:onlineResource
    }

    private static String writeAlternativeMetadataReference(XMLStreamWriter xmlw, Field otherIdF) throws XMLStreamException {
        String otherId = "";
        String otherIdAgency = "";
        String newOtherId = "";
        if (otherIdF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) otherIdF).getMultipleValues()) {
                Field otherIdAgencyF = foo.get("otherIdAgency");
                Field otherIdValueF = foo.get("otherIdValue");
                if (otherIdValueF != null) {
                    otherId = ((PrimitiveField) otherIdValueF).getSingleValue();
                }
                if (otherIdAgencyF != null) {
                    otherIdAgency = ((PrimitiveField) otherIdAgencyF).getSingleValue();
                }
                String description = determineOtherIdDescription(otherIdAgency);
                if (description.equals("GeoPortal Naming Conventions [2025]")) {
                    newOtherId = otherId;
                }

                if (otherId != null && !otherId.isEmpty()) {
                    xmlw.writeStartElement("mdb:alternativeMetadataReference");
                    xmlw.writeStartElement("cit:CI_Citation");
                    xmlw.writeStartElement("cit:title");
                    //xmlw.writeAttribute("xsi:type", "lan:PT_FreeText_PropertyType");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(otherIdAgency);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //cit:title
                    xmlw.writeStartElement("cit:identifier");
                    xmlw.writeStartElement("mcc:MD_Identifier");
                    xmlw.writeStartElement("mcc:code");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(otherId);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:code
                    xmlw.writeStartElement("mcc:codeSpace");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(otherIdAgency);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:codeSpace
                    xmlw.writeStartElement("mcc:description");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(description);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:description
                    xmlw.writeEndElement(); //mcc:MD_Identifier
                    xmlw.writeEndElement(); //cit:identifier


//                    xmlw.writeStartElement("cit:presentationForm");
//                    xmlw.writeStartElement("cit:CI_PresentationFormCode");
//                    xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode");
//                    xmlw.writeAttribute("codeListValue", "documentDigital");
//                    xmlw.writeCharacters("documentDigital");
                    //xmlw.writeEndElement(); //cit:CI_PresentationFormCode
                    //xmlw.writeEndElement(); //cit:presentationForm
                    xmlw.writeEndElement(); //cit:CI_Citation
                    xmlw.writeEndElement(); //mdb:alternativeMetadataReference
                }
            }
        }
        return newOtherId;
    }

    private static void writeSpatialRepresentationInfo(XMLStreamWriter xmlw, Field geometricObjectCountF,
                                                       Field geometricObjectTypeF, Field numberOfDimensionsF,
                                                       Field axisDimensionPropertyF, Field cellGeometryF) throws XMLStreamException {
        String geometricObjectCount = "";
        if (geometricObjectCountF != null) {
            geometricObjectCount = ((PrimitiveField) geometricObjectCountF).getSingleValue();
        }
        String geometricObjectType = "";
        if (geometricObjectTypeF != null) {
            geometricObjectType = ((ControlledVocabularyField) geometricObjectTypeF).getSingleValue();
        }

        String numberOfDimensions = "";
        if (numberOfDimensionsF != null) {
            numberOfDimensions = ((PrimitiveField) numberOfDimensionsF).getSingleValue();
        }

        String cellGeometry = "";
        if (cellGeometryF != null) {
            cellGeometry = ((ControlledVocabularyField) cellGeometryF).getSingleValue();
        }

        //mdb:MD_Metadata/mdb:spatialRepresentationInfo/msr:MD_SpatialRepresentation/msr:MD_GridSpatialRepresentation/msr:axisDimensionProperties/msr:numberOfDimensions/msr:MD_CellGeometryCode


        if ((numberOfDimensions != null && !numberOfDimensions.isEmpty()) ||
                (cellGeometry != null && !cellGeometry.isEmpty()) || axisDimensionPropertyF != null) {

            xmlw.writeStartElement("mdb:spatialRepresentationInfo");
            xmlw.writeStartElement("msr:MD_GridSpatialRepresentation");

            if (numberOfDimensions != null && !numberOfDimensions.isEmpty()) {

                xmlw.writeStartElement("msr:numberOfDimensions");
                xmlw.writeStartElement("gco:Integer");
                xmlw.writeCharacters(numberOfDimensions);
                xmlw.writeEndElement(); //gco:Integer
                xmlw.writeEndElement(); //msr:numberOfDimensions

                //mdb:MD_Metadata/mdb:spatialRepresentationInfo/msr:MD_SpatialRepresentation/msr:MD_GridSpatialRepresentation/msr:numberOfDimensions/gco:Integer
            }
            //axisDimensionProperties
            if (axisDimensionPropertyF != null) {
                for (HashMap<String, Field> foo : ((CompoundField) axisDimensionPropertyF).getMultipleValues()) {
                    Field dimensionNameTypeF = foo.get("dimensionNameType");
                    Field dimensionSizeF = foo.get("dimensionSize");
                    Field resolutionF = foo.get("resolution");
                    Field resolutionUnitOfMeasureF = foo.get("resolutionUnitOfMeasure");
                    String resolutionUnitOfMeasure = "";
                    String resolution = "";
                    String dimensionSize = "";
                    String dimensionNameType = "";
                    if (dimensionNameTypeF != null) {
                        dimensionNameType = ((ControlledVocabularyField) dimensionNameTypeF).getSingleValue();
                    }
                    if (dimensionSizeF != null) {
                        dimensionSize = ((PrimitiveField) dimensionSizeF).getSingleValue();
                    }
                    if (resolutionF != null) {
                        resolution = ((PrimitiveField) resolutionF).getSingleValue();
                    }
                    if (resolutionUnitOfMeasureF != null) {
                        resolutionUnitOfMeasure = ((PrimitiveField) resolutionUnitOfMeasureF).getSingleValue();
                    }

                    xmlw.writeStartElement("msr:axisDimensionProperties");
                    xmlw.writeStartElement("msr:MD_Dimension");
                    if (dimensionNameType != null && !dimensionNameType.isEmpty()) {
                        xmlw.writeStartElement("msr:dimensionName");
                        xmlw.writeStartElement("msr:MD_DimensionNameTypeCode");
                        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_DimensionNameTypeCode");
                        xmlw.writeAttribute("codeListValue", dimensionNameType);
                        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
                        xmlw.writeCharacters(dimensionNameType);
                        xmlw.writeEndElement(); //msr:MD_DimensionNameTypeCode
                        xmlw.writeEndElement(); //msr:dimensionName
                    }
                    if (dimensionSize != null && !dimensionSize.isEmpty()) {
                        xmlw.writeStartElement("msr:dimensionSize");
                        xmlw.writeStartElement("gco:Integer");
                        xmlw.writeCharacters(dimensionSize);
                        xmlw.writeEndElement(); //gco:Integer
                        xmlw.writeEndElement(); //msr:dimensionSize
                    }
                    if ((resolution != null && !resolution.isEmpty()) || (resolutionUnitOfMeasure != null && !resolutionUnitOfMeasure.isEmpty())) {
                        xmlw.writeStartElement("msr:resolution");
                        xmlw.writeStartElement("gco:Measure");
                        xmlw.writeAttribute(("uom"), resolutionUnitOfMeasure);
                        xmlw.writeCharacters(resolution);
                        xmlw.writeEndElement(); //gco:Measure
                        xmlw.writeEndElement(); //msr:resolution
                    }
                    xmlw.writeEndElement(); //msr:MD_Dimension
                    xmlw.writeEndElement(); //msr:axisDimensionProperties
                }

            }

            //cellGeometry
            if (cellGeometry != null && !cellGeometry.isEmpty()) {
                xmlw.writeStartElement("msr:cellGeometry");
                xmlw.writeStartElement("msr:MD_CellGeometryCode");
                xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_CellGeometryCode");
                xmlw.writeAttribute("codeListValue", cellGeometry);
                xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
                xmlw.writeCharacters(cellGeometry);
                xmlw.writeEndElement(); //msr:MD_CellGeometryCode
                xmlw.writeEndElement(); //msr:cellGeometry
            }
            xmlw.writeStartElement("msr:transformationParameterAvailability");
            xmlw.writeEndElement(); //msr:transformationParameterAvailability
            xmlw.writeEndElement(); //msr:MD_GridSpatialRepresentation
            xmlw.writeEndElement(); //mdb:spatialRepresentationInfo

        }

        if ((geometricObjectCount != null && !geometricObjectCount.isEmpty()) ||
                (geometricObjectType != null && !geometricObjectType.isEmpty())) {
            //mdb:MD_Metadata/mdb:spatialRepresentationInfo/msr:MD_SpatialRepresentation/msr:MD_VectorSpatialRepresentation/geometricObjects/MD_GeometricObjects/
            //xmlw.writeStartElement("msr:MD_SpatialRepresentation");
            xmlw.writeStartElement("mdb:spatialRepresentationInfo");
            xmlw.writeStartElement("msr:MD_VectorSpatialRepresentation");
            xmlw.writeStartElement("msr:geometricObjects");
            xmlw.writeStartElement("msr:MD_GeometricObjects");

            if (geometricObjectType != null && !geometricObjectType.isEmpty()) {
                xmlw.writeStartElement("msr:geometricObjectType");
                xmlw.writeStartElement("msr:MD_GeometricObjectTypeCode");
                xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_GeometricObjectTypeCode");
                xmlw.writeAttribute("codeListValue", geometricObjectType);
                xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
                xmlw.writeCharacters(geometricObjectType);
                xmlw.writeEndElement(); //msr:MD_GeometricObjectTypeCode
                xmlw.writeEndElement(); //msr:geometricObjectType
            }

            if (geometricObjectCount != null && !geometricObjectCount.isEmpty()) {
                xmlw.writeStartElement("msr:geometricObjectCount");
                xmlw.writeStartElement("gco:Integer");
                xmlw.writeCharacters(geometricObjectCount);
                xmlw.writeEndElement(); //gco:Integer
                xmlw.writeEndElement(); //mrs:geometricObjectCount
            }
            xmlw.writeEndElement(); //msr:MD_GeometricObjects
            xmlw.writeEndElement(); //msr:geometricObjects
            xmlw.writeEndElement(); //msr:MD_VectorSpatialRepresentation
            xmlw.writeEndElement(); //mdb:spatialRepresentationInfo
            //xmlw.writeEndElement(); //msr:MD_SpatialRepresentation
        }
        //xmlw.writeEndElement(); //msr:MD_SpatialRepresentation


    }

    private static void writeReferenceSystemInfo(XMLStreamWriter xmlw, Field referenceSystemInfoF) throws XMLStreamException {
        //logger.info("writeReferenceSystemInfo");
        //logger.info(Long.toString(referenceSystemInfoDTO.getMultipleCompound().size()));
        if (referenceSystemInfoF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) referenceSystemInfoF).getMultipleValues()) {

                String referenceSystemCode = "";
                String referenceSystemCodeSpace = "";

                Field referenceSystemCodeF = foo.get("referenceSystemCode");
                Field referenceSystemCodeSpaceF = foo.get("referenceSystemCodeSpace");
                if (referenceSystemCodeF != null) {
                    referenceSystemCode = ((PrimitiveField) referenceSystemCodeF).getSingleValue();
                }
                if (referenceSystemCodeSpaceF != null) {
                    referenceSystemCodeSpace = ((PrimitiveField) referenceSystemCodeSpaceF).getSingleValue();
                }


                xmlw.writeStartElement("mdb:referenceSystemInfo");
                xmlw.writeStartElement("mrs:MD_ReferenceSystem");
                xmlw.writeStartElement("mrs:referenceSystemIdentifier");
                xmlw.writeStartElement("mcc:MD_Identifier");
                xmlw.writeStartElement("mcc:code");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(referenceSystemCode);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //mcc:code
                xmlw.writeStartElement("mcc:codeSpace");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(referenceSystemCodeSpace);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //mcc:codeSpace
                xmlw.writeEndElement(); // mcc:MD_Identifier
                xmlw.writeEndElement(); //mrs:referenceSystemIdentifier
                xmlw.writeEndElement(); //mrs:MD_ReferenceSystem
                xmlw.writeEndElement(); //mdb:referenceSystemInfo
            }
        }
    }

    private static void writeIdentificationInfo(XMLStreamWriter xmlw, Field geographicBoundingBox, Field keyword, Field author,
                                                Field title, Field alternativeTitle, Field distributionDate, Field referenceDate,
                                                Field topicClass, Field note, Field series, Field software,
                                                Field spatialResolution, Field spatialRepresentationType, String termsOfuse,
                                                Field datasetContact, Field description, Field publication,
                                                Field producer, Field timePeriodCovered, Field otherReferences,
                                                Field geographicCoverage, Field contributor, Field geographicUnit,
                                                Field productionDate, String restrictions, String citationrequirements,
                                                String depositorrequirements, String conditions, String disclaimer,
                                                ArrayList dataverseFiles, Properties prop) throws XMLStreamException, UnknownHostException {


        xmlw.writeStartElement("mdb:identificationInfo");
        xmlw.writeStartElement("mri:MD_DataIdentification");

        writeCitation(xmlw, author, title, alternativeTitle, distributionDate, referenceDate, series,
                producer, contributor, productionDate);
        writeAbstractAndPurpose(xmlw, description);
        writePointOfContact(xmlw, datasetContact);
        writeSpatialRepresentationType(xmlw, spatialRepresentationType);
        writeSpatialResolution(xmlw, spatialResolution);
        writeTopicClass(xmlw, topicClass);
        writeExtent(xmlw, geographicBoundingBox, timePeriodCovered, geographicCoverage);
        writeAdditionalDocumentation(xmlw, otherReferences);
        writeAdditionalDocumentationForDataverseFiles(xmlw, dataverseFiles, prop);
        writeThumbnails(xmlw, dataverseFiles, prop);
        writeDescritiveKeywords(xmlw, keyword, geographicCoverage, geographicUnit);
        writeResourceConstrains(xmlw, termsOfuse, restrictions, citationrequirements, depositorrequirements, conditions, disclaimer);
        writeAssociatedResource(xmlw, publication);
        writeAssociatedResourceForDataverseFiles(xmlw, dataverseFiles, prop);
        writeSoftware(xmlw, software);
        writeNote(xmlw, note);

        //mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:additionalDocumentation/cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:name/gco:CharacterString;
        // mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:additionalDocumentation/cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:linkage/gco:CharacterString;
        // mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:additionalDocumentation/cit:CI_Citation/cit:onlineResource/cit:CI_OnlineResource/cit:protocol/gco:CharacterString;
        //Label+URL of the website

        xmlw.writeEndElement(); //mri:MD_DataIdentification
        xmlw.writeEndElement(); //mdb:identificationInfo
        logger.info("writeIdentificationInfo");
    }

    private static void writeThumbnails(XMLStreamWriter xmlw, ArrayList<DataverseFiles> dataverseFiles, Properties prop) throws XMLStreamException {
        logger.info("Writing thumbnails for dataverse files");
        if (dataverseFiles != null) {
            logger.info(prop.getProperty("DATAVERSE_URL"));
            String serverName = prop.getProperty("DATAVERSE_URL");
            for (DataverseFiles file : dataverseFiles) {

                if (file.getDirectoryLabel() != null && file.getDirectoryLabel().equals("thumbnails")) {
                    xmlw.writeStartElement("mri:graphicOverview");
                    xmlw.writeStartElement("mcc:MD_BrowseGraphic");

                    xmlw.writeStartElement("mcc:fileName");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(file.getLabel());
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:fileName

                    xmlw.writeStartElement("mcc:fileDescription");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(file.getDescription());
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:fileDescription

                    xmlw.writeStartElement("mcc:fileType");
                    xmlw.writeStartElement("gco:CharacterString");
                    int dotIndex = file.getLabel().lastIndexOf('.'); // Find the last occurrence of '.'

                    if (dotIndex > 0 && dotIndex < file.getLabel().length() - 1) {
                        xmlw.writeCharacters(file.getLabel().substring(dotIndex + 1));
                    }
                    
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mcc:fileType

                    xmlw.writeStartElement("mcc:linkage");
                    String url = serverName + "/api/access/datafile/" + Integer.toString(file.getDataFile().getId());
                    String description = file.getDescription();
                    if ((description == null || description.isEmpty()) && file.getRestricted()) {
                        description = "(restricted)";
                    } else if (file.getRestricted()) {
                        description = description + " (restricted)";
                    }
                    writeOnlineResource(xmlw, url, "https", description, file.getLabel(),  null);
                    xmlw.writeEndElement(); //mcc:linkage

                    xmlw.writeEndElement(); //mcc:MD_BrowseGraphic
                    xmlw.writeEndElement(); //mri:graphicOverview
                    break; //only first thumbnail
                }
            }
        }
        logger.info("Writing thumbnails for dataverse files END");

    }
    private static void writeAssociatedResourceForDataverseFiles(XMLStreamWriter xmlw, ArrayList<DataverseFiles> dataverseFiles, Properties prop) throws XMLStreamException {
        if (dataverseFiles != null) {
            String serverName = prop.getProperty("DATAVERSE_URL");
            for (DataverseFiles file : dataverseFiles) {
                if (file.getDirectoryLabel() != null && (file.getDirectoryLabel().equals("associatedResource"))) {
                    String url = serverName + "/api/access/datafile/" + Integer.toString(file.getDataFile().getId());
                    writeSingleAssociatedRes(xmlw, file.getLabel(), url, "dependency", file.getDescription(), file.getLabel());
                }
            }

        }
    }

    private static void writeAdditionalDocumentationForDataverseFiles(XMLStreamWriter xmlw, ArrayList<DataverseFiles> dataverseFiles, Properties prop) throws XMLStreamException, UnknownHostException {
        logger.info("Writing additional documentation for dataverse files");
        if (dataverseFiles != null) {

            String serverName = prop.getProperty("DATAVERSE_URL"); //default
//            if (dataverseFiles.size() > 0) {
//                InetAddress localHost = InetAddress.getLocalHost();
//                serverName = localHost.getHostName();
//                logger.info("Hostname: " + serverName);
//            }


            for (DataverseFiles file : dataverseFiles) {
                if (file.getDirectoryLabel() != null && (file.getDirectoryLabel().equals("additionalDocumentation") || file.getDirectoryLabel().equals("userguides") )) {
                    xmlw.writeStartElement("mri:additionalDocumentation");
                    xmlw.writeStartElement("cit:CI_Citation");
                    xmlw.writeStartElement("cit:title");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(file.getLabel());
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //cit:title
                    String url = serverName + "/api/access/datafile/" + Integer.toString(file.getDataFile().getId());
                    xmlw.writeStartElement("cit:onlineResource");
                    String description = file.getDescription();
                    if ((description == null || description.isEmpty()) && file.getRestricted()) {
                        description = "(restricted)";
                    } else if (file.getRestricted()) {
                        description = description + " (restricted)";
                    }
                    writeOnlineResource(xmlw, url, "https", description, file.getLabel(),  null);
                    xmlw.writeEndElement(); //cit:onlineResource
                    xmlw.writeEndElement(); //cit:CI_Citation
                    xmlw.writeEndElement(); //mri:additionalDocumentation
                }

            }
        }
        logger.info("Writing additional documentation for dataverse files End");

    }

    private static void writeAdditionalDocumentation(XMLStreamWriter xmlw, Field otherReferencesF) throws XMLStreamException {
        logger.info("Writing additional documentation");
        if (otherReferencesF != null) {
            List<String> otherReferences = ((PrimitiveField) otherReferencesF).getMultipleValues();
            for (String ref : otherReferences) {
                if (ref != null && !ref.isEmpty()) {
                    String[] parts = ref.split(";");
                    xmlw.writeStartElement("mri:additionalDocumentation");
                    xmlw.writeStartElement("cit:CI_Citation");
                    xmlw.writeStartElement("cit:title");
                    xmlw.writeEndElement(); // cit:title
                    xmlw.writeStartElement("cit:onlineResource");
                    xmlw.writeStartElement("cit:CI_OnlineResource");
                    logger.info(" linkage field " + String.valueOf(parts.length));
                    if (parts.length>1) {
                        xmlw.writeStartElement("cit:linkage");
                        xmlw.writeStartElement("gco:CharacterString");
                        xmlw.writeCharacters(parts[1]);
                        xmlw.writeEndElement(); //gco:CharacterString
                        xmlw.writeEndElement(); //cit:linkage

                        //protocol is not known
                        xmlw.writeStartElement("cit:protocol");
                        xmlw.writeStartElement("gco:CharacterString");
                        xmlw.writeCharacters("https"); //hardcoded for now
                        xmlw.writeEndElement(); //gco:CharacterString
                        xmlw.writeEndElement(); //cit:protocol
                    } else {
                        xmlw.writeStartElement("cit:linkage");
                        xmlw.writeEndElement(); //cit:linkage
                        //protocol is not known
                        xmlw.writeStartElement("cit:protocol");
                        xmlw.writeEndElement(); //cit:protocol
                    }
                    xmlw.writeStartElement("cit:name");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(parts[0]);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //cit:name

                    xmlw.writeEndElement(); //cit:CI_OnlineResource
                    xmlw.writeEndElement(); //cit:onlineResource
                    xmlw.writeEndElement(); //cit:CI_Citation
                    xmlw.writeEndElement(); //mri:additionalDocumentation
                }
            }
        }
        logger.info("Writing additional documentation End");
    }

    private static void writeAssociatedResource(XMLStreamWriter xmlw, Field publicationF) throws XMLStreamException {
        logger.info("Writing associated resource");
        if (publicationF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) publicationF).getMultipleValues()) {
                Field publicationRelationTypeF = foo.get("publicationRelationType");
                Field publicationCitationF = foo.get("publicationCitation");
                Field publicationURLF = foo.get("publicationURL");

                String publicationRelationType = "";
                String publicationCitation = "";
                String publicationURL = "";
                String type = "";
                if (publicationRelationTypeF != null) {
                    publicationRelationType = ((ControlledVocabularyField) publicationRelationTypeF).getSingleValue();
                    if (publicationRelationType.equals("IsCitedBy") || publicationRelationType.equals("Cites") ||
                            publicationRelationType.equals("IsReferencedBy") || publicationRelationType.equals("References")) {
                        type = "crossReference";
                    } else if (publicationRelationType.equals("IsSupplementedBy") || publicationRelationType.equals("IsSupplementTo")) {
                        type = "dependency";
                    }
                }
                if (publicationCitationF != null) {
                    publicationCitation = ((PrimitiveField) publicationCitationF).getSingleValue();
                }
                if (publicationURLF != null) {
                    publicationURL = ((PrimitiveField) publicationURLF).getSingleValue();
                }

                //writeSingleAssociatedResource(xmlw, publicationRelationTypeF, publicationCitationF, publicationURLF, prop);
                writeSingleAssociatedRes(xmlw, publicationCitation, publicationURL, type, null, null);
            }
        }
        logger.info("Writing associated resource End");
    }

    private static void writeSingleAssociatedRes(XMLStreamWriter xmlw,String title, String url, String type, String desc, String name) throws XMLStreamException {
        xmlw.writeStartElement("mri:associatedResource");
        xmlw.writeStartElement("mri:MD_AssociatedResource");
        xmlw.writeStartElement("mri:name");
        xmlw.writeStartElement("cit:CI_Citation");
        xmlw.writeStartElement("cit:title");
        if (title != null && !title.trim().isEmpty()) {
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(title);
            xmlw.writeEndElement(); //gco:CharacterString
        }
        xmlw.writeEndElement(); //cit:title
        if (url != null && !url.trim().isEmpty()) {
                xmlw.writeStartElement("cit:onlineResource");
                writeOnlineResource(xmlw, url, "https", desc, name, null);
                xmlw.writeEndElement(); //cit:onlineResource
        }
        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mri:name

        if (type != null && !type.trim().isEmpty()) {
            xmlw.writeStartElement("mri:associationType");
            xmlw.writeStartElement("mri:DS_AssociationTypeCode");

            xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#DS_AssociationTypeCode");
            xmlw.writeAttribute("codeListValue",type);
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");

            xmlw.writeEndElement(); //mri:DS_AssociationTypeCode
            xmlw.writeEndElement(); //mri:associationType
        }
        xmlw.writeEndElement(); //mri:associatedResource
        xmlw.writeEndElement(); //mri:MD_AssociatedResource
        //logger.info("Writing single associated resource End");
    }

    private static void writeSingleAssociatedResource(XMLStreamWriter xmlw, Field publicationRelationTypeF, Field publicationCitationF, Field publicationURLF, Properties prop) throws XMLStreamException {
        logger.info("Writing single associated resource");
        String publicationRelationType = "";
        String publicationCitation = "";
        String publicationURL = "";
        xmlw.writeStartElement("mri:associatedResource");
        xmlw.writeStartElement("mri:MD_AssociatedResource");
        if (publicationRelationTypeF != null) {
            publicationRelationType = ((ControlledVocabularyField) publicationRelationTypeF).getSingleValue();
        }
        if (publicationCitationF != null) {
            publicationCitation = ((PrimitiveField) publicationCitationF).getSingleValue();
        }
        if (publicationURLF != null) {
            publicationURL = ((PrimitiveField) publicationURLF).getSingleValue();
        }

        if (!publicationCitation.isEmpty() || !publicationURL.isEmpty()) {

            xmlw.writeStartElement("mri:name");
            xmlw.writeStartElement("cit:CI_Citation");
            if (!publicationCitation.isEmpty()) {
                xmlw.writeStartElement("cit:title");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(publicationCitation);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:title
            }
            if (!publicationURL.isEmpty()) {
                xmlw.writeStartElement("cit:onlineResource");
                writeOnlineResource(xmlw, publicationURL, "https", null, null,  null);
                xmlw.writeEndElement(); //cit:onlineResource
            }
            xmlw.writeEndElement(); //cit:CI_Citation
            xmlw.writeEndElement(); //mri:name
        }

        if (!publicationRelationType.isEmpty()) {
            xmlw.writeStartElement("mri:associationType");
            xmlw.writeStartElement("mri:DS_AssociationTypeCode");

            xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#DS_AssociationTypeCode");
            xmlw.writeAttribute("codeListValue",publicationRelationType);
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");

//            xmlw.writeStartElement("gco:CharacterString");
//            xmlw.writeCharacters(publicationRelationType);
//            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //mri:DS_AssociationTypeCode
            xmlw.writeEndElement(); //mri:associationType
        }

        xmlw.writeEndElement(); //mri:associatedResource
        xmlw.writeEndElement(); //mri:MD_AssociatedResource
        logger.info("Writing single associated resource End");
    }

    private static void writeCitation(XMLStreamWriter xmlw, Field authorF,Field titleF,
                               Field alternativeTitleF, Field distributionDateF,
                               Field referenceDateF, Field series, Field producerF, Field contributorF,
                                      Field productionDateF) throws XMLStreamException {

        xmlw.writeStartElement("mri:citation");
        xmlw.writeStartElement("cit:CI_Citation");
        title(xmlw, titleF);
        alternativeTitle(xmlw, alternativeTitleF);

        if (distributionDateF != null) {
            String distributionDate = ((PrimitiveField) distributionDateF).getSingleValue();
            if (distributionDate != null && !distributionDate.isEmpty()) {
                printDate(xmlw, distributionDate, "distribution");
            }
        }
        if (referenceDateF != null) {
            referenceDate(xmlw, referenceDateF);
        }

        if (productionDateF != null) {
            String productionDate = ((PrimitiveField) productionDateF).getSingleValue();
            if (productionDate != null && !productionDate.isEmpty()) {
                printDate(xmlw, productionDate, "creation");
            }
        }

        for (HashMap<String, Field> foo : ((CompoundField) authorF).getMultipleValues()) {
            Field authorNameF = foo.get("authorName");
            Field authorAffiliationF = foo.get("authorAffiliation");
            String authorName = "";
            String authorAffiliation = "";
            if (authorNameF != null) {
                authorName = ((PrimitiveField) authorNameF).getSingleValue();
            }
            if (authorAffiliationF != null) {
                authorAffiliation = ((PrimitiveField) authorAffiliationF).getSingleValue();
            }
            if ((authorAffiliation != null && !authorAffiliation.isEmpty() ||
                    (authorName != null && !authorName.isEmpty()))) {
                responsibleParty(xmlw, authorName, "author", authorAffiliation);
                //responsibleParty(xmlw, authorName, "originator");
            }
        }
        if (producerF != null) {
            writeProducer(xmlw, producerF, "producerName", "producerAffiliation", "publisher");
        }

        if (contributorF != null) {
            writeContributor(xmlw, contributorF);
        }

        if (series != null) {
            writeSeries(xmlw, series);
        }









        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mri:citation
        logger.info("Writing citation End");
    }



    private static String getContributorRole(String contributorType) {
        if (contributorType != null && !contributorType.isEmpty()) {
            switch (contributorType) {
                case "Hosting Institution":
                    return "resourceProvider";
                case "Other":
                    return "collaborator";
                case "Rights Holder":
                    return "rightsHolder";
                case "Researcher":
                    return "principalInvestigator"; //"collaborator"?
                case "Data Curator":
                    return "processor"; //"collaborator"?
                case "Funder":
                    return "funder";
                case "Editor":
                    return "editor";
                case "Research Group":
                    return "publisher";
                case "Project Member":
                    return "collaborator";
                case "Sponsor":
                    return "sponsor";
                case "Data Collector":
                    return "collaborator";
                case "Data Manager":
                    return "collaborator";
                case "Work Package Leader":
                    return "collaborator";
                case "Supervisor":
                    return "collaborator";
                case "Related Person":
                    return "collaborator";
                case "Project Manager":
                    return "collaborator";
                case "Project Leader":
                    return "collaborator";
                default:
                    return "";

            }
        }
        return "";
    }

    private static void writeContributor(XMLStreamWriter xmlw, Field contributorF) throws XMLStreamException {

        String role = "";
        for (HashMap<String, Field> foo : ((CompoundField) contributorF).getMultipleValues()) {
            Field contributorTypeF = foo.get("contributorType");
            Field contributorNameF = foo.get("contributorName");
            String contributorType = "";
            String contributorName = "";

            if (contributorTypeF != null) {
                contributorType = ((ControlledVocabularyField) contributorTypeF).getSingleValue();
                role = getContributorRole(contributorType);
            }
            if (contributorNameF != null) {
                contributorName = ((PrimitiveField) contributorNameF).getSingleValue();
            }
            if ((contributorName != null && !contributorName.isEmpty()) ||
                    (role != null && !role.isEmpty())) {
                responsibleParty(xmlw, null, role, contributorName);
            }
        }

    }



    private static void writeProducer(XMLStreamWriter xmlw, Field producerF, String producerNameStr, String producerAffiliationStr, String role) throws XMLStreamException {
        logger.info("Writing producer ");
        for (HashMap<String, Field> foo : ((CompoundField) producerF).getMultipleValues()) {
            Field producerNameF = foo.get(producerNameStr);
            Field producerAffiliationF = foo.get(producerAffiliationStr);
            String producerName = "";
            String producerAffiliation = "";
            if (producerNameF != null) {
                producerName = ((PrimitiveField) producerNameF).getSingleValue();
            }
            if (producerAffiliationF != null) {
                producerAffiliation = ((PrimitiveField) producerAffiliationF).getSingleValue();
            }
            if ((producerAffiliation != null && !producerAffiliation.isEmpty() ||
                    (producerName != null && !producerName.isEmpty()))) {
                responsibleParty(xmlw, producerName, role, producerAffiliation);
            }
        }
        logger.info("Writing producer End");
    }

    private static void title(XMLStreamWriter xmlw, Field titleF) throws XMLStreamException {
        String title = ((PrimitiveField) titleF).getSingleValue();
        xmlw.writeStartElement("cit:title");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(title);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:title
    }

    private static void alternativeTitle(XMLStreamWriter xmlw, Field alternativeTitleF) throws XMLStreamException {
        if (alternativeTitleF != null) {
            for (String altTitle : ((PrimitiveField) alternativeTitleF).getMultipleValues()) {
                xmlw.writeStartElement("cit:alternateTitle");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(altTitle);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:alternateTitle
            }
        }
    }

    private static void printDate(XMLStreamWriter xmlw, String distributionDate, String code) throws XMLStreamException {
        xmlw.writeStartElement("cit:date");
        dateISO(xmlw, distributionDate, code);
        xmlw.writeEndElement();//cit:date
    }

    private static void referenceDate(XMLStreamWriter xmlw, Field referenceDateF) throws XMLStreamException {
        for (HashMap<String, Field> foo : ((CompoundField) referenceDateF).getMultipleValues()) {
            String referenceDateType = "";
            String referenceDateValue = "";

            Field referenceDateTypeF = foo.get("referenceDateType");
            Field referenceDateValueF = foo.get("referenceDateValue");
            if (referenceDateTypeF != null) {
                referenceDateType = ((ControlledVocabularyField) referenceDateTypeF).getSingleValue();
            }
            if (referenceDateValueF != null) {
                referenceDateValue = ((PrimitiveField) referenceDateValueF).getSingleValue();
            }


            if (referenceDateValue != null && !referenceDateValue.isEmpty()) {
                xmlw.writeStartElement("cit:date");
                dateISO(xmlw, referenceDateValue, referenceDateType);
                xmlw.writeEndElement();//cit:date
            }
        }
    }

    private static void responsibleParty(XMLStreamWriter xmlw, String name, String role, String affiliation) throws XMLStreamException {

        xmlw.writeStartElement("cit:citedResponsibleParty");
        xmlw.writeStartElement("cit:CI_Responsibility");
        xmlw.writeStartElement("cit:role");
        xmlw.writeStartElement("cit:CI_RoleCode");
        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode");
        xmlw.writeAttribute("codeListValue", role);
        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
        xmlw.writeCharacters(role);
        xmlw.writeEndElement(); // cit:CI_RoleCode
        xmlw.writeEndElement(); // cit:role
        xmlw.writeStartElement("cit:party");
        xmlw.writeStartElement("cit:CI_Organisation");
        if (affiliation != null && !affiliation.isEmpty()) {
            xmlw.writeStartElement("cit:name");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(affiliation);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //cit:name
        }
        if (name != null && !name.isEmpty()) {
            xmlw.writeStartElement("cit:individual");
            xmlw.writeStartElement("cit:CI_Individual");
            xmlw.writeStartElement("cit:name");
            xmlw.writeAttribute("xsi:type", "lan:PT_FreeText_PropertyType");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(name);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //cit:name
            xmlw.writeEndElement(); //cit:CI_Individual
            xmlw.writeEndElement(); //cit:individual
        }
        xmlw.writeEndElement(); //cit:CI_Organisation
        xmlw.writeEndElement(); //cit:party
        xmlw.writeEndElement(); //cit:CI_Responsibility
        xmlw.writeEndElement(); //cit:citedResponsibleParty
        logger.info("Writing responsibleParty End");
    }

    private static void writeSeries(XMLStreamWriter xmlw, Field seriesF) throws XMLStreamException {
        logger.info("Writing series");
        String totalSeries = "";
        int i = 1;
        List<HashMap<String, Field>> fields =  ((CompoundField) seriesF).getMultipleValues();
        for (HashMap<String, Field> foo : fields) {
            logger.info("Writing series " + i + " of " + fields.size());
            String seriesName = "";
            Field seriesNameF = foo.get("seriesName");

            if (seriesNameF != null) {
                logger.info("SeriesNameF");
                seriesName = ((PrimitiveField) seriesNameF).getSingleValue();
            }

//              <cit:presentationForm>
//                    <cit:CI_PresentationFormCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode" codeListValue="documentDigital">documentDigital</cit:CI_PresentationFormCode>
//                    </cit:presentationForm>

//            xmlw.writeStartElement("cit:presentationForm");
//            xmlw.writeStartElement("cit:CI_PresentationFormCode");
//            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode");
//            xmlw.writeAttribute("codeListValue", "documentDigital");
//            xmlw.writeCharacters("documentDigital");
//            xmlw.writeEndElement(); //cit:CI_PresentationFormCode
//            xmlw.writeEndElement(); //cit:presentationForm



            if (seriesName != null && !seriesName.isEmpty()) {
                logger.info("Size of series " + Integer.toString(foo.size()));
                if (i < fields.size()) {
                    seriesName += ";";
                }
            }
            i++;
            totalSeries += seriesName;
        }
        if (totalSeries != null || !totalSeries.isEmpty()) {
            xmlw.writeStartElement("cit:series");
            xmlw.writeStartElement("cit:CI_Series");
            xmlw.writeStartElement("cit:name");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(totalSeries);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //cit:name
            xmlw.writeEndElement(); //cit:CI_Series
            xmlw.writeEndElement(); //cit:series
            //cit:series/cit:CI_Series/cit:name/gco:CharacterString
        }
    }

    private static void writeAbstractAndPurpose(XMLStreamWriter xmlw, Field descriptionF) throws XMLStreamException {
        if (descriptionF != null) {
            for (HashMap<String, Field> foo : ((CompoundField)descriptionF).getMultipleValues()) {
                String description = "";
                String date = "";
                Field descriptionValue = foo.get("dsDescriptionValue");
                Field descriptionDate = foo.get("dsDescriptionDate");
                if (descriptionValue != null) {
                    description = ((PrimitiveField) descriptionValue).getSingleValue();
                }
                if (descriptionDate != null) {
                    date = ((PrimitiveField) descriptionDate).getSingleValue();
                }
                xmlw.writeStartElement("mri:abstract");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(description);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //field
                xmlw.writeStartElement("mri:purpose"); //mri:abstract
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(description);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //mri:purpose
                break; //only first description
                //Only one description is allowed in ISO19115-3
                //If multiple descriptions are found only the first one is written
                //mri:abstract/gco:CharacterString
                //mri:purpose/gco:CharacterString
            }
        }
    }

    private static void writePointOfContact(XMLStreamWriter xmlw, Field datasetContactF) throws XMLStreamException {
        //mri:pointOfContact/cit:CI_Responsibility/cit:party/cit:CI_Organisation/cit:individual/cit:CI_Individual/cit:name/gco:CharacterString
        for (HashMap<String, Field> foo : ((CompoundField) datasetContactF).getMultipleValues()) {
            String datasetContactName = "";
            String datasetContactAffiliation = "";
            String datasetContactEmail = "";
            Field datasetContactNameF = foo.get("datasetContactName");
            Field datasetContactAffiliationF = foo.get("datasetContactAffiliation");
            Field datasetContactEmailF = foo.get("datasetContactEmail");

            if (datasetContactNameF != null) {
                datasetContactName = ((PrimitiveField) datasetContactNameF).getSingleValue();
            }
            if (datasetContactAffiliationF != null) {
                datasetContactAffiliation = ((PrimitiveField) datasetContactAffiliationF).getSingleValue();
            }
            if (datasetContactEmailF != null) {
                datasetContactEmail = ((PrimitiveField) datasetContactEmailF).getSingleValue();
            }

            xmlw.writeStartElement("mri:pointOfContact");
            xmlw.writeStartElement("cit:CI_Responsibility");
            xmlw.writeStartElement("cit:role");
            // <cit:CI_RoleCode codeList="http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_RoleCode" codeListValue="pointOfContact">pointOfContact</cit:CI_RoleCode>
            xmlw.writeStartElement("cit:CI_RoleCode");
            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode");
            xmlw.writeAttribute("codeListValue", "pointOfContact");
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
            xmlw.writeCharacters("pointOfContact");
            xmlw.writeEndElement(); //cit:CI_RoleCode
            xmlw.writeEndElement(); //cit:role
            xmlw.writeStartElement("cit:party");
            xmlw.writeStartElement("cit:CI_Organisation");
            if (datasetContactAffiliation != null && !datasetContactAffiliation.isEmpty()) {
                xmlw.writeStartElement("cit:name");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(datasetContactAffiliation);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:name
            }
            if (datasetContactEmail != null && !datasetContactEmail.isEmpty()) {
                //cit:contactInfo/cit:CI_Contact/cit:address/cit:CI_Address/cit:electronicMailAddress/gco:CharacterString
                xmlw.writeStartElement("cit:contactInfo");
                xmlw.writeStartElement("cit:CI_Contact");
                xmlw.writeStartElement("cit:address");
                xmlw.writeStartElement("cit:CI_Address");
                xmlw.writeStartElement("cit:electronicMailAddress");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(datasetContactEmail);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:electronicMailAddress
                xmlw.writeEndElement(); //cit:CI_Address
                xmlw.writeEndElement(); //cit:address
                xmlw.writeEndElement(); //cit:CI_Contact
                xmlw.writeEndElement(); //cit:contactInfo
            }
            if (datasetContactName != null && !datasetContactName.isEmpty()) {
                xmlw.writeStartElement("cit:individual");
                xmlw.writeStartElement("cit:CI_Individual");
                xmlw.writeStartElement("cit:name");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(datasetContactName);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:name
                xmlw.writeEndElement(); //cit:CI_Individual
                xmlw.writeEndElement(); //cit:individual
            }

            xmlw.writeEndElement(); //cit:CI_Organisation
            xmlw.writeEndElement(); //cit:party
            xmlw.writeEndElement(); //cit:CI_Responsibility
            xmlw.writeEndElement(); //mri:pointOfContact

        }

    }

    private static void writeSpatialRepresentationType(XMLStreamWriter xmlw, Field spatialRepresentationTypeF ) throws XMLStreamException {

        if (spatialRepresentationTypeF != null) {
            String spatialRepresentationType = ((ControlledVocabularyField) spatialRepresentationTypeF).getSingleValue();
            if (spatialRepresentationType != null && !spatialRepresentationType.isEmpty()) {
                xmlw.writeStartElement("mri:spatialRepresentationType");
                xmlw.writeStartElement("mcc:MD_SpatialRepresentationTypeCode");
                xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_SpatialRepresentationTypeCode");
                xmlw.writeAttribute("codeListValue", spatialRepresentationType);
                xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
                xmlw.writeCharacters(spatialRepresentationType);
                xmlw.writeEndElement(); //mcc:spatialRepresentationTypeCode
                xmlw.writeEndElement(); //mri:spatialRepresentationType
            }
        }
    }

    private static void writeSpatialResolution(XMLStreamWriter xmlw, Field spatialResolutionF) throws XMLStreamException {
        if (spatialResolutionF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) spatialResolutionF).getMultipleValues()) {
                String spatialResolutionValue = "";
                String spatialResolutionType = "";
                String spatialResolutionUnitOfMeasure = "";

                Field spatialResolutionValueF = foo.get("spatialResolutionValue");
                Field spatialResolutionTypeF = foo.get("spatialResolutionType");
                Field spatialResolutionUnitOfMeasureF = foo.get("spatialResolutionUnitOfMeasure");

                if (spatialResolutionValueF != null) {
                    spatialResolutionValue = ((PrimitiveField) spatialResolutionValueF).getSingleValue();
                }
                if (spatialResolutionTypeF != null) {
                    spatialResolutionType = ((ControlledVocabularyField) spatialResolutionTypeF).getSingleValue();
                }
                if (spatialResolutionUnitOfMeasureF != null) {
                    spatialResolutionUnitOfMeasure = ((PrimitiveField) spatialResolutionUnitOfMeasureF).getSingleValue();
                }

                if (spatialResolutionValue != null && !spatialResolutionValue.isEmpty()) {

                    if (spatialResolutionType.equals("equivalentScale")) {
                        xmlw.writeStartElement("mri:spatialResolution");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("mri:equivalentScale");
                        xmlw.writeStartElement("mri:MD_RepresentativeFraction");
                        xmlw.writeStartElement("mri:denominator");
                        xmlw.writeStartElement("gco:Integer");
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:Integer
                        xmlw.writeEndElement(); //mri:denominator
                        xmlw.writeEndElement(); //mri:MD_RepresentativeFraction
                        xmlw.writeEndElement(); //mri:equivalentScale
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:spatialResolution
                        break;  //only equivalent scale is supported for now
                    } else if (spatialResolutionType.equals("distance")) {
                        xmlw.writeStartElement("mri:spatialResolution");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("mri:distance");
                        xmlw.writeStartElement("gco:Distance");
                        xmlw.writeAttribute("uom", spatialResolutionUnitOfMeasure);
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:Distance
                        xmlw.writeEndElement(); //mri:distance
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:spatialResolution
                    } else if (spatialResolutionType.equals("vertical")) {
                        xmlw.writeStartElement("mri:spatialResolution");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("mri:vertical");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("gco:Distance");
                        xmlw.writeAttribute("uom", spatialResolutionUnitOfMeasure);
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:Distance
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:vertical
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:spatialResolution
                    } else if (spatialResolutionType.equals("angularDistance")) {
                        xmlw.writeStartElement("mri:spatialResolution");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("mri:angularDistance");
                        xmlw.writeStartElement("gco:Angle");
                        xmlw.writeAttribute("uom", spatialResolutionUnitOfMeasure);
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:Angle
                        xmlw.writeEndElement(); //mri:angularDistance
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:spatialResolution
                    } else if (spatialResolutionType.equals("levelOfDetail")) {
                        xmlw.writeStartElement("mri:spatialResolution");
                        xmlw.writeStartElement("mri:MD_Resolution");
                        xmlw.writeStartElement("mri:levelOfDetail");
                        xmlw.writeStartElement("gco:CharacterString");
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:CharacterString
                        xmlw.writeEndElement(); //mri:levelOfDetail
                        xmlw.writeEndElement(); //mri:MD_Resolution
                        xmlw.writeEndElement(); //mri:spatialResolution
                    }

                }

            }
        }
    }

    private static void writeTopicClass(XMLStreamWriter xmlw, Field topicClassF) throws XMLStreamException {


        boolean isCVV = false;
        if (topicClassF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) topicClassF).getMultipleValues()) {

                String topicClassificationValue = "";
                String topicClassificationVocab = "";
                String topicClassificationURI = "";

                Field topicClassificationValueF = foo.get("topicClassValue");
                Field topicClassificationVocabF = foo.get("topicClassVocab");
                Field topicClassificationURIF = foo.get("topicClassVocabURI");

                if (topicClassificationValueF != null) {
                    topicClassificationValue = ((PrimitiveField) topicClassificationValueF).getSingleValue();
                }
                if (topicClassificationVocabF != null) {
                    topicClassificationVocab = ((PrimitiveField) topicClassificationVocabF).getSingleValue();
                }
                if (topicClassificationURIF != null) {
                    topicClassificationURI = ((PrimitiveField) topicClassificationURIF).getSingleValue();
                }

                if (topicClassificationValue != null &&  !topicClassificationValue.isEmpty()) {
                    xmlw.writeStartElement("mri:topicCategory");
                    xmlw.writeStartElement("mri:MD_TopicCategoryCode");
//                    xmlw.writeAttribute("CodeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_TopicCategoryCode");
//                    xmlw.writeAttribute("CodeListValue", topicClassificationValue);
//                    xmlw.writeAttribute("CodeSpace", "http://standards.iso.org/iso/19115");
                    logger.info("Topic classification: " + topicClassificationValue);
                    xmlw.writeCharacters(topicClassificationValue);
                    xmlw.writeEndElement(); //mri:MD_TopicCategoryCode
                    xmlw.writeEndElement(); //mri:topicCategory
                }
            }
        }
    }

    private static void writeExtent(XMLStreamWriter xmlw, Field geographicBoundingBoxF, Field timePeriodCoveredF,
                                    Field geographicCoverageF) throws XMLStreamException {
        logger.info("writeExtent");
        /* Only 1 geoBndBox is
           So, I'm just going to arbitrarily use the first one, and ignore the rest! */


        if (geographicCoverageF != null) {
            HashMap<String, Field> foo = ((CompoundField) geographicCoverageF).getMultipleValues().get(0); //for now
            Field otherGeographicCoverageF = foo.get("otherGeographicCoverage");
            if (otherGeographicCoverageF != null) {
                //cit:description/gco:CharacterString
                String otherGeographicCoverage = ((PrimitiveField) otherGeographicCoverageF).getSingleValue();
                if (otherGeographicCoverage != null && !otherGeographicCoverage.isEmpty()) {

                    xmlw.writeStartElement("mri:extent");
                    xmlw.writeStartElement("gex:EX_Extent");
                    xmlw.writeStartElement("gex:description");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(otherGeographicCoverage);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //gex:description
                    xmlw.writeEndElement(); //gex:EX_Extent
                    xmlw.writeEndElement(); //mri:extent
                }
            }
        }

        if (geographicBoundingBoxF != null) {
            xmlw.writeStartElement("mri:extent");
            xmlw.writeStartElement("gex:EX_Extent");
            HashMap<String, Field> bndBoxMap = ((CompoundField) geographicBoundingBoxF).getMultipleValues().get(0);
            Field westLongitudeF = bndBoxMap.get("westLongitude");
            Field eastLongitudeF = bndBoxMap.get("eastLongitude");
            Field northLatitudeF = bndBoxMap.get("northLatitude");
            Field southLatitudeF = bndBoxMap.get("southLatitude");

            String westLongitudeValue = "";
            String eastLongitudeValue = "";
            String northLatitudeValue = "";
            String southLatitudeValue = "";
            if (westLongitudeF != null) {
                westLongitudeValue = ((PrimitiveField) westLongitudeF).getSingleValue();
            }
            if (eastLongitudeF != null) {
                eastLongitudeValue = ((PrimitiveField) eastLongitudeF).getSingleValue();
            }
            if (northLatitudeF != null) {
                northLatitudeValue = ((PrimitiveField) northLatitudeF).getSingleValue();
            }
            if (southLatitudeF != null) {
                southLatitudeValue = ((PrimitiveField) southLatitudeF).getSingleValue();
            }


            xmlw.writeStartElement("gex:geographicElement");
            xmlw.writeStartElement("gex:EX_GeographicBoundingBox");


            xmlw.writeStartElement("gex:westBoundLongitude");
            xmlw.writeStartElement("gco:Decimal");
            xmlw.writeCharacters(westLongitudeValue);
            xmlw.writeEndElement(); //gco:Decimal
            xmlw.writeEndElement();//gex:westBoundLongitude


            xmlw.writeStartElement("gex:eastBoundLongitude");
            xmlw.writeStartElement("gco:Decimal");
            xmlw.writeCharacters(eastLongitudeValue);
            xmlw.writeEndElement(); //gco:Decimal
            xmlw.writeEndElement();//gex:eastBoundLongitude

            xmlw.writeStartElement("gex:southBoundLatitude");
            xmlw.writeStartElement("gco:Decimal");
            xmlw.writeCharacters(southLatitudeValue);
            xmlw.writeEndElement(); //gco:Decimal
            xmlw.writeEndElement(); //gex:southBoundLatitude

            xmlw.writeStartElement("gex:northBoundLatitude");
            xmlw.writeStartElement("gco:Decimal");
            xmlw.writeCharacters(northLatitudeValue);
            xmlw.writeEndElement(); //gco:Decimal
            xmlw.writeEndElement(); //gex:northBoundLatitude

            xmlw.writeEndElement(); //ex:EX_GeographicBoundingBox
            xmlw.writeEndElement(); //gex:geographicElement
            xmlw.writeEndElement(); //gex:EX_Extent
            xmlw.writeEndElement(); //mri:extent

        }
        if (timePeriodCoveredF != null) {
            writeTimePeriod(xmlw, timePeriodCoveredF );
        }


        logger.info("writeExtent End");
    }



    private static void writeTimePeriod(XMLStreamWriter xmlw, Field timePeriodCoveredF) throws XMLStreamException {
        logger.info("writeTimePeriod");
        HashMap<String, Field> timePeriodCovered = ((CompoundField) timePeriodCoveredF).getMultipleValues().get(0);
        Field timePeriodCoveredStartF = timePeriodCovered.get("timePeriodCoveredStart");
        Field timePeriodCoveredEndF = timePeriodCovered.get("timePeriodCoveredEnd");
        String timePeriodCoveredStart = "";
        String timePeriodCoveredEnd = "";

        if (timePeriodCoveredStartF != null) {
            timePeriodCoveredStart = ((PrimitiveField) timePeriodCoveredStartF).getSingleValue();
        }
        if (timePeriodCoveredEndF != null) {
            timePeriodCoveredEnd = ((PrimitiveField) timePeriodCoveredEndF).getSingleValue();
        }

        //gex:temporalElement/gex:EX_TemporalExtent/gex:extent/gml:TimePeriod/gml:beginPosition
        if (!timePeriodCoveredStart.isEmpty() || !timePeriodCoveredEnd.isEmpty()) {
            xmlw.writeStartElement("mri:extent");
            xmlw.writeStartElement("gex:EX_Extent");
            xmlw.writeStartElement("gex:temporalElement");
            xmlw.writeStartElement("gex:EX_TemporalExtent");
            xmlw.writeStartElement("gex:extent");
            xmlw.writeStartElement("gml:TimePeriod");
            if (timePeriodCoveredStart != null && !timePeriodCoveredStart.isEmpty()) {
                xmlw.writeStartElement("gml:beginPosition");
                xmlw.writeCharacters(timePeriodCoveredStart);
                xmlw.writeEndElement(); //gml:beginPosition
            }
            if (timePeriodCoveredEnd != null && !timePeriodCoveredEnd.isEmpty()) {
                xmlw.writeStartElement("gml:endPosition");
                xmlw.writeCharacters(timePeriodCoveredEnd);
                xmlw.writeEndElement(); //gml:endPosition
            }
            xmlw.writeEndElement(); //gml:TimePeriod
            xmlw.writeEndElement(); //gex:extent
            xmlw.writeEndElement(); //gex:EX_TemporalExtent
            xmlw.writeEndElement(); //gex:temporalElement
            xmlw.writeEndElement(); //gex:EX_Extent
            xmlw.writeEndElement(); //mri:extent
        }
        logger.info("writeTimePeriod End");

    }
    private static HashMap<String, List<String>> prepareKeywords(Field keywordsF) {
        HashMap<String, List<String>> mapTypeField = new HashMap<>();;
        if (keywordsF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) keywordsF).getMultipleValues()) {
                String keywordValue = "";
                String keywordVocab = "";
                String keywordURI = "";

                Field keywordValueF = foo.get("keywordValue");
                Field keywordVocabF = foo.get("keywordVocabulary");
                Field keywordURIF = foo.get("keywordVocabularyURI");
                if (keywordValueF != null) {
                    keywordValue = ((PrimitiveField) keywordValueF).getSingleValue();
                }
                if (keywordVocabF != null) {
                    keywordVocab = ((PrimitiveField) keywordVocabF).getSingleValue();
                    if (!mapTypeField.containsKey(keywordVocab)) {
                        List<String> keywordList = new ArrayList<>();
                        keywordList.add(keywordValue);
                        mapTypeField.put(keywordVocab, keywordList);
                    } else {
                        List<String> keywordList = mapTypeField.get(keywordVocab);
                        keywordList.add(keywordValue);
                        mapTypeField.put(keywordVocab, keywordList);
                    }
                }

            }

        }
        return mapTypeField;

    }

    private static String findKeywordType(String thesaurusName) {
        String type = null;
        switch (thesaurusName) {
            case "Government of Canada Core Subject Thesaurus":
                type = "theme";
                break;
            case "Global Change Master Directory (GCMD) Location Keywords":
                type = "location";
                break;
            case "Ontario Ministry of Natural Resources (OMNR) Thesaurus":
                type = "theme";
                break;
            case "free keywords":
                type = "free keywords";
                break;
            case "dataverseLocation":
                type = "location";
                break;
            case "dataverseGeographicUnit":
                type = "featureType";
                break;
            default:
                type = "free keywords";
        }
        return type;
    }

    private static HashMap<String, List<String>>  addKeywordToMap(HashMap<String, List<String>> mapTypeField,
                                                                  String keyword, String keywordType) {
        if (keyword != null && !keyword.isEmpty()) {

            if (!mapTypeField.containsKey(keywordType)) {
                List<String> keywordList = new ArrayList<>();
                keywordList.add(keyword);
                mapTypeField.put(keywordType, keywordList);
            } else {
                List<String> keywordList = mapTypeField.get(keywordType);
                keywordList.add(keyword);
                mapTypeField.put(keywordType, keywordList);
            }
        }
        return mapTypeField;
    }

    private static HashMap<String, List<String>> addGeographicKeyword(HashMap<String, List<String>> mapTypeField,
                                                          Field geographicCoverageF, Field geographicUnitF) {
        if (geographicCoverageF != null) {
            HashMap<String, Field> geoCov = ((CompoundField) geographicCoverageF).getMultipleValues().get(0);
            Field countryF = geoCov.get("country");
            if (countryF != null) {
                String country = ((ControlledVocabularyField) countryF).getSingleValue();
                addKeywordToMap(mapTypeField, country, "dataverseLocation");
            }
            Field stateF = geoCov.get("state");
            if (stateF != null) {
                String state = ((PrimitiveField) stateF).getSingleValue();
                addKeywordToMap(mapTypeField, state, "dataverseLocation");
            }
            Field cityF = geoCov.get("city");
            if (cityF != null) {
                String city = ((PrimitiveField) cityF).getSingleValue();
                addKeywordToMap(mapTypeField, city, "dataverseLocation");
            }
            Field otherGeographicCoverageF = geoCov.get("otherGeographicCoverage");
            if (otherGeographicCoverageF != null) {
                String otherGeographicCoverage = ((PrimitiveField) otherGeographicCoverageF).getSingleValue();
                addKeywordToMap(mapTypeField, otherGeographicCoverage, "dataverseLocation");
            }

        }
        if (geographicUnitF != null) {
            String geoUnit = ((PrimitiveField) geographicUnitF).getMultipleValues().get(0);
            addKeywordToMap(mapTypeField, geoUnit, "dataverseGeographicUnit");
        }

        return mapTypeField;
    }

    private static void writeDescritiveKeywords(XMLStreamWriter xmlw, Field keywordsF,
                                                Field geographicCoverageF, Field geographicUnitF) throws XMLStreamException {
        logger.info("writeDescritiveKeywords");
        HashMap<String, List<String>> mapTypeField = prepareKeywords(keywordsF);
        mapTypeField = addGeographicKeyword(mapTypeField, geographicCoverageF, geographicUnitF);

        for (String thesaurusName : mapTypeField.keySet()) {
            xmlw.writeStartElement("mri:descriptiveKeywords");
            xmlw.writeStartElement("mri:MD_Keywords");
            for (String keywordValue : mapTypeField.get(thesaurusName)) {
                if (keywordValue != null && !keywordValue.isEmpty()) {
                    xmlw.writeStartElement("mri:keyword");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(keywordValue);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mri:keyword
                }
            }
            String type = findKeywordType(thesaurusName);
            xmlw.writeStartElement("mri:type");
            xmlw.writeStartElement("mri:MD_KeywordTypeCode");
            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#MD_KeywordTypeCode");
            xmlw.writeAttribute("codeListValue", type);
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
            xmlw.writeCharacters(type);
            xmlw.writeEndElement(); //mri:MD_KeywordTypeCode
            xmlw.writeEndElement(); //mri:type

            if (!type.equals("free keywords") && !thesaurusName.equals("dataverseLocation") && !thesaurusName.equals("dataverseGeographicUnit") ) {

                xmlw.writeStartElement("mri:thesaurusName");
                xmlw.writeStartElement("cit:CI_Citation");
                xmlw.writeStartElement("cit:title");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(thesaurusName);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:title
                xmlw.writeStartElement("cit:presentationForm");
                xmlw.writeStartElement("cit:CI_PresentationFormCode");
                xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_PresentationFormCode");
                xmlw.writeAttribute("codeListValue", "documentDigital");
                xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
                xmlw.writeCharacters("documentDigital");
                xmlw.writeEndElement(); //cit:CI_PresentationFormCode
                xmlw.writeEndElement(); //cit:presentationForm
                xmlw.writeEndElement(); //cit:CI_Citation
                xmlw.writeEndElement(); //mri:thesaurusName
            }
            xmlw.writeEndElement(); //mri:MD_Keywords
            xmlw.writeEndElement(); //mri:descriptiveKeywords

        }
        logger.info("writeDescritiveKeywords END");

    }

    private static void useLimitations( XMLStreamWriter xmlw, String restrictions) throws XMLStreamException {
        if (restrictions != null && !restrictions.isEmpty()) {
            xmlw.writeStartElement("mri:resourceConstraints");
            xmlw.writeStartElement("mco:MD_Constraints");
            xmlw.writeStartElement("mco:useLimitation");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(restrictions);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //mco:useLimitation
            xmlw.writeEndElement(); //mco:MD_Constraints
            xmlw.writeEndElement(); //mri:resourceConstraints
        }
    }

    private static void writeResourceConstrains(XMLStreamWriter xmlw, String termsOfUse, String restrictions, String citationrequirements,
                                                String depositorrequirements, String conditions,
                                                String disclaimer) throws XMLStreamException {
        logger.info("writeResourceConstrains");
        //mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:resourceConstraints/mco:MD_LegalConstraints/mco:otherConstraints //terms of us
        //mri:resourceConstraints/mco:MD_Constraints/mco:UseLimitation //restrictions
        //mdb:MD_Metadata/mdb:identificationInfo/mri:MD_DataIdentification/mri:resourceConstraints/mco:MD_Constraints/mco:UseLimitation
        useLimitations( xmlw, restrictions);
        useLimitations( xmlw, citationrequirements);
        useLimitations(xmlw, depositorrequirements);
        useLimitations(xmlw, conditions);
        useLimitations(xmlw, disclaimer);

        if (termsOfUse != null && !termsOfUse.isEmpty()) {
            xmlw.writeStartElement("mri:resourceConstraints");
            xmlw.writeStartElement("mco:MD_LegalConstraints");
            xmlw.writeStartElement("mco:otherConstraints");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(termsOfUse);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //mco:otherConstraints
            xmlw.writeEndElement(); //mco:MD_LegalConstraints
            xmlw.writeEndElement(); //mri:resourceConstraints
        }


//        if (termsOfUse != null && !termsOfUse.isEmpty()) {
//            xmlw.writeStartElement("mri:resourceConstraints");
//            xmlw.writeStartElement("mco:MD_LegalConstraints");
//            xmlw.writeStartElement("mco:useConstraints");
//            xmlw.writeStartElement("mco:MD_RestrictionCode");
//            xmlw.writeAttribute("codeList", "standards.iso.org/19115/-3/lan/1.0/codelists.xml#MD_RestrictionCode");
//            xmlw.writeAttribute("codeListValue", termsOfUse);
//            xmlw.writeCharacters(termsOfUse);
//            xmlw.writeEndElement(); //mco:MD_RestrictionCode
//            xmlw.writeEndElement(); //mco:useConstraints
//            xmlw.writeEndElement(); //mco:MD_LegalConstraints
//            xmlw.writeEndElement(); //mri:resourceConstraints
//        }
        logger.info("writeResourceConstrains End");
        //        mri:resourceConstraints>
//                <mco:MD_LegalConstraints>
//                <mco:accessConstraints>
//                <mco:MD_RestrictionCode codeList="standards.iso.org/19115/-3/lan/1.0/codelists.xml#MD_RestrictionCode" codeListValue="license">license</mco:MD_RestrictionCode>
//                </mco:accessConstraints>
//                <mco:useConstraints>
//                <mco:MD_RestrictionCode codeList="standards.iso.org/19115/-3/lan/1.0/codelists.xml#MD_RestrictionCode" codeListValue="restricted">restricted</mco:MD_RestrictionCode>
//                </mco:useConstraints>
    }

    private static void writeSoftware(XMLStreamWriter xmlw, Field softwareF) throws XMLStreamException {
        logger.info("writeSoftware");
        int i = 0;
        String software = "";
        if (softwareF != null) {
            for (HashMap<String, Field> foo : ((CompoundField) softwareF).getMultipleValues()) {
                String softwareName = "";
                String softwareVersion = "";

                Field softwareNameF = foo.get("softwareName");
                Field softwareVersionF = foo.get("softwareVersion");
                if (softwareNameF != null) {
                    softwareName = ((PrimitiveField) softwareNameF).getSingleValue();
                }
                if (softwareVersionF != null) {
                    softwareVersion = ((PrimitiveField) softwareVersionF).getSingleValue();
                }

                String software1 = softwareName + " Version: " + softwareVersion;
                if (i > 0) {
                    software = software + ";" + software1;
                } else {
                    software = software1;
                }
                i++;
            }
            if (software != null && !software.isEmpty()) {
                xmlw.writeStartElement("mri:environmentDescription"); //not repeatable
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(software);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //mri:environmentDescription
            }
        }
        logger.info("writeSoftware End");
    }

    private static void writeNote(XMLStreamWriter xmlw, Field noteF) throws XMLStreamException {
        if (noteF != null) {
            String note = ((PrimitiveField) noteF).getSingleValue();
            xmlw.writeStartElement("mri:supplementalInformation");
            xmlw.writeAttribute("xsi:type","lan:PT_FreeText_PropertyType");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(note);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //mri:supplementalInformation
        }
    }

    private static void writeDistributionInfo(XMLStreamWriter xmlw, Field distributionF, Field distributorF, ArrayList<DataverseFiles> dataverseFiles, String newOtherId, Properties prop) throws XMLStreamException {
        logger.info("writeDistributionInfo");
        List<HashMap<String, Field>> distribution = null;
        List<HashMap<String, Field>> distibutor = null;
        if (distributionF != null || distributionF != null) {
            if (distributionF != null) {
                distribution = ((CompoundField) distributionF).getMultipleValues();
            }
            if (distributorF != null) {
                distibutor = ((CompoundField) distributorF).getMultipleValues();
            }
            if ((distribution != null && distribution.size() > 0) || (distibutor != null && distibutor.size() > 0)) {
                xmlw.writeStartElement("mdb:distributionInfo");
                xmlw.writeStartElement("mrd:MD_Distribution");
                //mrd:distributor/mrd:distributorContact/cit:CI_Responsibility/cit:party/cit:CI_Organisation/cit:individual/
                // cit:CI_Individual/cit:name/gco:CharacterString
                if (distibutor != null && distibutor.size() > 0) {
                    writeDistributors(xmlw, distibutor);
                }

                xmlw.writeStartElement("mrd:transferOptions");
                xmlw.writeStartElement("mrd:MD_DigitalTransferOptions");
                for ( HashMap<String, Field> foo : distribution) {
                    String distributionLinkLabel = "";
                    String distributionLink = "";
                    String protocol = "";

                    Field distributionLinkLabelF = foo.get("distributionLinkLabel");
                    Field distributionLinkF = foo.get("distributionLink");
                    Field protocolF = foo.get("protocol");
                    if (distributionLinkLabelF != null) {
                        distributionLinkLabel = ((PrimitiveField) distributionLinkLabelF).getSingleValue();
                    }
                    if (distributionLinkF != null) {
                        distributionLink = ((PrimitiveField) distributionLinkF).getSingleValue();
                    }
                    if (protocolF != null) {
                        protocol = ((PrimitiveField) protocolF).getSingleValue();
                    }

                    xmlw.writeStartElement("mrd:onLine");
                    writeOnlineResource(xmlw, distributionLink, protocol, distributionLinkLabel, null,"fileAccess");
                    xmlw.writeEndElement(); //mrd:onLine
                    //onLine(xmlw, distributionLinkLabel, distributionLink, protocol);

                }
                //Add zip file link
                if (dataverseFiles != null) {
                    logger.info("newOtherId:" + newOtherId);
                    String serverName = prop.getProperty("DATAVERSE_URL");
                    String url = serverName + "/api/access/datafiles/";
                    ArrayList fileIds = new ArrayList();
                    boolean restricted = false;
                    for (DataverseFiles file : dataverseFiles) {
                        //logger.info(file.getDirectoryLabel());
                        if (file.getDirectoryLabel() != null && file.getDirectoryLabel().startsWith(newOtherId)) {
                            if (file.getRestricted()) {
                                restricted = true;
                            }
                            String file_id = Integer.toString(file.getDataFile().getId());
                            fileIds.add(file_id);
                        }
                    }
                    int i = 1;
                    int numOfFiles = fileIds.size();
                    for (String fileId : (ArrayList<String>) fileIds) {
                        url = url + fileId;
                        if (i < numOfFiles) {
                            url = url + ",";
                        }
                        i++;
                    }
                    if (i > 1) {
                        url = url + "?format=original";

                        xmlw.writeStartElement("mrd:onLine");
                        String description = "Zip file";
                        if (restricted) {
                            description = description + " (restricted)";
                        }
                        writeOnlineResource(xmlw, url, "https", description, null, null);
                        xmlw.writeEndElement(); //mrd:onLine
                    }
                }

                xmlw.writeEndElement(); //mrd:MD_DigitalTransferOptions
                xmlw.writeEndElement(); //mrd:transferOptions
                xmlw.writeEndElement(); //mrd:MD_Distribution
                xmlw.writeEndElement(); //mdb:distributionInfo

            }
        }
        logger.info("writeDistributionInfo End");
    }

    private static void writeDistributors(XMLStreamWriter xmlw, List<HashMap<String, Field>> distibutor) throws XMLStreamException {
        logger.info("writeDistributors");
        for (HashMap<String, Field> foo : distibutor) {
            String distributorName = "";
            String distributorAffiliation = "";
            Field distributorNameF = foo.get("distributorName");
            Field distributorAffiliationF = foo.get("distributorAffiliation");

            if (distributorNameF != null) {
                distributorName = ((PrimitiveField) distributorNameF).getSingleValue();
            }
            if (distributorAffiliationF != null) {
                distributorAffiliation = ((PrimitiveField) distributorAffiliationF).getSingleValue();
            }

            xmlw.writeStartElement("mrd:distributor");
            xmlw.writeStartElement("mrd:MD_Distributor");
            xmlw.writeStartElement("mrd:distributorContact");
            xmlw.writeStartElement("cit:CI_Responsibility");
            xmlw.writeStartElement("cit:role");
            xmlw.writeStartElement("cit:CI_RoleCode");
            xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codelists.xml#CI_RoleCode");
            xmlw.writeAttribute("codeListValue", "distributor");
            xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
            xmlw.writeCharacters("distributor");
            xmlw.writeEndElement(); // cit:CI_RoleCode
            xmlw.writeEndElement(); // cit:role
            xmlw.writeStartElement("cit:party");
            xmlw.writeStartElement("cit:CI_Organisation");

            if (distributorAffiliation != null && !distributorAffiliation.isEmpty()) {
                xmlw.writeStartElement("cit:name");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(distributorAffiliation);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:name
            }
            logger.info("distributorNameAffiliation after");

            if (distributorName != null && !distributorName.isEmpty()) {
                logger.info("brfore cit:individual");
                xmlw.writeStartElement("cit:individual");
                logger.info("brfore cit:CI_Individual");
                xmlw.writeStartElement("cit:CI_Individual");
                logger.info("brfore cit:name");
                xmlw.writeStartElement("cit:name");
                logger.info("brfore gco:CharacterString");
                xmlw.writeStartElement("gco:CharacterString");
                logger.info("brfore writeCharacters " + distributorName);
                xmlw.writeCharacters(distributorName);
                logger.info("brfore end doc");
                xmlw.writeEndElement(); //gco:CharacterString
                logger.info("brfore end doc2");
                xmlw.writeEndElement(); //cit:name
                logger.info("brfore end doc3");
                xmlw.writeEndElement(); //cit:CI_Individual
                logger.info("brfore end doc4");
                xmlw.writeEndElement(); //cit:individual
                logger.info("Before Organisation End");


            }
            xmlw.writeEndElement(); //cit:CI_Organisation
            logger.info("After Organisation End");
            xmlw.writeEndElement(); //cit:party
            logger.info("party after");
            xmlw.writeEndElement(); //cit:CI_Responsibility
            logger.info("Responsibility after");
            xmlw.writeEndElement(); //mrd:distributorContact
            logger.info("distributorContact after");
            xmlw.writeEndElement(); //mrd:MD_Distributor
            xmlw.writeEndElement(); //mrd:distributor
            logger.info("distributor after");

        }
        logger.info("writeDistributors End");
    }

//    private static void  onLine(XMLStreamWriter xmlw, String distributionLinkLabel, String distributionLink, String protocol) throws XMLStreamException {
//        xmlw.writeStartElement("mrd:onLine");
//        xmlw.writeStartElement("cit:CI_OnlineResource");
//        xmlw.writeStartElement("cit:linkage");
//        xmlw.writeStartElement("gco:CharacterString");
//        xmlw.writeCharacters(distributionLink);
//        xmlw.writeEndElement(); //gco:CharacterString
//        xmlw.writeEndElement(); //cit:linkage
//
//        xmlw.writeStartElement("cit:protocol");
//        xmlw.writeStartElement("gco:CharacterString");
//        xmlw.writeCharacters(protocol);
//        xmlw.writeEndElement(); //gco:CharacterString
//        xmlw.writeEndElement(); //cit:protocol
//
//        xmlw.writeStartElement("cit:name");
//        xmlw.writeStartElement("gco:CharacterString");
//        xmlw.writeCharacters(distributionLinkLabel);
//        xmlw.writeEndElement(); //gco:CharacterString
//        xmlw.writeEndElement(); //cit:name
//
//        xmlw.writeStartElement("cit:description");
//        xmlw.writeStartElement("gco:CharacterString");
//        xmlw.writeCharacters(description);
//        xmlw.writeEndElement(); //gco:CharacterString
//        xmlw.writeEndElement(); //cit:description
//
//        xmlw.writeStartElement("cit:function");
//        xmlw.writeStartElement("cit:CI_OnLineFunctionCode");
//        xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_OnLineFunctionCode" );
//        xmlw.writeAttribute( "codeListValue",function);
//        xmlw.writeAttribute("codeSpace", "http://standards.iso.org/iso/19115");
//        xmlw.writeCharacters("fileAccess");
//        xmlw.writeEndElement(); //cit:CI_OnLineFunctionCode
//        xmlw.writeEndElement(); //cit:function
//        xmlw.writeEndElement(); //cit:CI_OnlineResource
//        xmlw.writeEndElement(); //mrd:onLine
//    }

    private static void writeResourceLineage(XMLStreamWriter xmlw, Field lineageStatementF,
                                       Field processStepF, Field characteristicOfSourcesF) throws XMLStreamException {
        if (lineageStatementF != null || processStepF != null || characteristicOfSourcesF != null) {
            xmlw.writeStartElement("mdb:resourceLineage");
            xmlw.writeStartElement("mrl:LI_Lineage");
            if (lineageStatementF != null) {
                String lineageStatement = lineageStatementF.getTypeName();
                if (lineageStatement != null && !lineageStatement.isEmpty()) {
                    xmlw.writeStartElement("mrl:statement");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(lineageStatement);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mrl:statement
                }
            }
            if (characteristicOfSourcesF != null) {
                String sourceDescription = ((PrimitiveField) characteristicOfSourcesF).getSingleValue();
                if (sourceDescription != null && !sourceDescription.isEmpty()) {
                    xmlw.writeStartElement("mrl:source");
                    xmlw.writeStartElement("mrl:LI_Source");
                    xmlw.writeStartElement("mrl:description");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(sourceDescription);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mrl:description
                    xmlw.writeEndElement(); //mrl:LI_Source
                    xmlw.writeEndElement(); //mrl:source
                }
            }
            //mdb:MD_Metadata/mdb:resourceLineage/mrl:LI_Lineage/mrl:sourceDescription

            if (processStepF != null) {
                for (String process : ((PrimitiveField) processStepF).getMultipleValues()) {
                    xmlw.writeStartElement("mrl:processStep");
                    xmlw.writeStartElement("mrl:LI_ProcessStep");
                    xmlw.writeStartElement("mrl:description");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(process);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mrl:description
                    xmlw.writeEndElement(); //mrl:LI_ProcessStep
                    xmlw.writeEndElement(); //mrl:processStep

                }
            }
            xmlw.writeEndElement(); //mdb:resourceLineage
            xmlw.writeEndElement(); //mrl:LI_Lineage
        }
    }

    private static void writeMetadataMaintenance(XMLStreamWriter xmlw, String originalArchive) throws XMLStreamException {
        //mdb:metadataMaintenance/mmi:MD_MaintenanceInformation/mmi:maintenanceNote/gco:CharacterString
        if (originalArchive != null && !originalArchive.isEmpty()) {
            xmlw.writeStartElement("mdb:metadataMaintenance");
            xmlw.writeStartElement("mmi:MD_MaintenanceInformation");
            xmlw.writeStartElement("mmi:maintenanceNote");
            xmlw.writeStartElement("gco:CharacterString");
            xmlw.writeCharacters(originalArchive);
            xmlw.writeEndElement(); //gco:CharacterString
            xmlw.writeEndElement(); //mmi:maintenanceNote
            xmlw.writeEndElement(); //mmi:MD_MaintenanceInformation
            xmlw.writeEndElement(); //mdb:metadataMaintenance
        }
    }


    //    private static String getFieldValue(List<Field> fields, String typeName) {
//        return fields.stream()
//                .filter(f -> typeName.equals(f.typeName))
//                .map(f -> f.value.toString())
//                .findFirst()
//                .orElse("");
//    }

    private static String detectLanguage(TitleAndDescription td) {
        String lang = "en"; //default language
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result1 = detector.detect(td.Title );
        String lang1 = result1.getLanguage();
        if (result1.isReasonablyCertain()) {
            lang = lang1;
        } else {
            LanguageResult result2 = detector.detect(td.Description);
            if (result2.isReasonablyCertain()) {
                lang = result2.getLanguage();
            }
        }

        URL found = ISO19115_3ExportUtil.class.getResource("messages_" + lang + ".properties.xml");

        if (found != null) {
                return lang;
        } else {
                return null;
        }
    }

    private static TitleAndDescription getTitleAndDescription(InputStream datafile)  {

        TitleAndDescription titleAndDescription = new TitleAndDescription();
        String lang = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(datafile);
            try {
                lang = doc.getDocumentElement().getAttribute("xml:lang");
            } catch (DOMException e) {
                lang = null;
                logger.warn("No language attribute");
            }
            if (lang != null && !lang.equals("") ) {
                titleAndDescription.Language = lang;
            } else {
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                try {
                    XPathExpression expr = xpath.compile("/codeBook/stdyDscr/citation/titlStmt/titl/text()");
                    titleAndDescription.Title = (String) expr.evaluate(doc, XPathConstants.STRING);
                    expr = xpath.compile("/codeBook/stdyDscr/stdyInfo/abstract/text()");
                    titleAndDescription.Description = (String) expr.evaluate(doc, XPathConstants.STRING);
                } catch (XPathExpressionException e) {
                    logger.error("Error finding title and description");
                    logger.error(e.getMessage());
                }
            }

            return titleAndDescription;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.warn(e.getMessage());
            return null;
        }

    }
    
    public static void datasetISO19115_3(InputStream datafile, OutputStream outputStream) throws XMLStreamException {
        try {
            String localeEnvVar = "en"; //default language
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            datafile.transferTo(baos);

            byte[] buffer = baos.toByteArray();
            InputStream clone1 = new ByteArrayInputStream(buffer);
            InputStream clone2 = new ByteArrayInputStream(buffer);

            TitleAndDescription td = getTitleAndDescription(clone1);
            if (td != null) {
                if (td.Language != null) {
                    localeEnvVar = td.Language;
                } else {
                    String lang = detectLanguage(td);
                    if (lang != null && !lang.equals("")) {
                        localeEnvVar = lang;
                    }
                }
            }

            InputStream  styleSheetInput = ISO19115_3ExportUtil.class.getResourceAsStream("ddi-to-fo.xsl");

            final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outputStream);
                // Setup XSLT
                TransformerFactory factory = TransformerFactory.newInstance();
                Source mySrc = new StreamSource(styleSheetInput);
                factory.setURIResolver(new FileResolver());
                Transformer transformer = factory.newTransformer(mySrc);

                transformer.setParameter("language-code", localeEnvVar);

                // Setup input for XSLT transformation
                Source src = new StreamSource(clone2);

                // Resulting SAX events (the generated FO) must be piped through to FOP
                Result res = new SAXResult(fop.getDefaultHandler());

                // Start XSLT transformation and FOP processing
                transformer.transform(src, res);

            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }  catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
