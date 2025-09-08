package io.gdcc.export.iso19115_3;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gdcc.spi.export.ExportException;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;

import java.net.URL;

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

    @JsonProperty("termsOfUse")
    private String termsOfUse;

    @JsonProperty("originalArchive")
    private String originalArchive;

    public Map<String, MetadataBlock> getMetadataBlocks() {
        return metadataBlocks;
    }

    public String getTermsOfUse() {
        return termsOfUse;
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
    public static class TitleAndDescription  {
        public String Title;
        public String Description;
        public String Language;
    }
    
    private ISO19115_3ExportUtil() {
        // As this is a util class, adding a private constructor disallows instances of this class.
    }

    public  static void parseDataverseJson(InputStream jsonInputStream, OutputStream outputStream)  {
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Read json dataverse");
            Dataset dataset = mapper.readValue(jsonInputStream, Dataset.class);

            MetadataBlock citation = dataset.getDatasetVersion().getMetadataBlocks().get("citation");
            MetadataBlock geospatial = dataset.getDatasetVersion().getMetadataBlocks().get("geospatial");

            logger.info("Block: " + citation.getDisplayName());
            logger.info("Block: " + geospatial.getDisplayName());

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
            Field geoReferenceDate = null;
            Field topicClassification = null;
            Field note = null;
            Field series = null;
            Field software = null;
            Field lineageStatement  = null;
            Field sourceDescription  = null;
            Field processStep  = null;
            Field spatialResolution  = null;
            Field spatialRepresentationType = null;
            Field geometricObjectCount  = null;
            Field geometricObjectTypeCode  = null;
            Field datasetContact  = null;
            Field description  = null;
            
            

            for (Field f : citation.getFields()) {
                if (f.getTypeName().equals("otherId")) {
                    otherId = f;
                }
                if (f.getTypeName().equals("language")){
                    language = f;
                }
                if (f.getTypeName().equals("keyword")) {
                    keyword = f;
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
                if (f.getTypeName().equals("notesText"))  {
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

            }

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
                if (f.getTypeName().equals("geoReferenceDate")) {
                    geoReferenceDate = f;
                }
                if (f.getTypeName().equals("lineageStatement")) {
                    lineageStatement = f;
                }
                if (f.getTypeName().equals("sourceDescription")) {
                    sourceDescription = f;
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
                if (f.getTypeName().equals("geometricObjectTypeCode")) {
                    geometricObjectTypeCode = f;
                }
            }


            logger.info("Finished reading json");



            XMLStreamWriter xmlw = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            xmlw.writeStartDocument();
            xmlw.writeStartElement("mdb:MD_Metadata");
            writeNamespaces(xmlw);
            writeDatasetPersistentId(xmlw, dataset.getIdentifier(), dataset.getAuthority(), dataset.getProtocol());
            writeDefaultLocale(xmlw, language);
            writeEmptyPrereq(xmlw);
            logger.info("Before writeDateInfo");
            writeDateInfo(xmlw, description);
            writeAlternativeMetadataReference(xmlw, otherId);
            writeSpatialRepresentationInfo(xmlw, geometricObjectCount,  geometricObjectTypeCode);
            writeReferenceSystemInfo(xmlw, referenceSystemInfo);
            writeIdentificationInfo(xmlw, geographicBoundingBox, keyword, author,
                    title,  alternativeTitle,  distributionDate,  geoReferenceDate,
                    topicClassification, note, series, software,
                    spatialResolution, spatialRepresentationType, dataset.getDatasetVersion().getTermsOfUse(),
                    datasetContact, description);
            writeDistributionInfo(xmlw, distribution);
            writeResourceLineage(xmlw, lineageStatement, sourceDescription, processStep ); //unclear
            writeMetadataMaintenance(xmlw, dataset.getDatasetVersion().getOriginalArchive());

            xmlw.writeEndElement(); // MD_Metadata
            xmlw.writeEndDocument();
            xmlw.flush();
            logger.info("Befor validation starting");
            ISO_Validator.validate(outputStream, "iso19115-3");
            logger.info("After validation ending");

        } catch (XMLStreamException xse) {
            throw new RuntimeException(xse);
        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            logger.error("XML Validation Error: " + e.getMessage());
            throw new RuntimeException(e);
        }


//        } catch (Exception e) {
//            logger.info("XML Error");
//            logger.error(e.getMessage());
//        }

    }

    private static void writeNamespaces(XMLStreamWriter xmlw) throws XMLStreamException {
        xmlw.writeAttribute("xmlns:cat", "http://standards.iso.org/iso/19115/-3/cat/1.0");
        xmlw.writeAttribute("xmlns:cit","http://standards.iso.org/iso/19115/-3/cit/2.0");
        //xmlw.writeAttribute("xmlns:dc","http://purl.org/dc/terms/");
        xmlw.writeAttribute("xmlns:gcx", "http://standards.iso.org/iso/19115/-3/gcx/1.0");
        xmlw.writeAttribute("xmlns:gex", "http://standards.iso.org/iso/19115/-3/gex/1.0");
        xmlw.writeAttribute("xmlns:lan", "http://standards.iso.org/iso/19115/-3/lan/1.0");
        xmlw.writeAttribute("xmlns:srv", "http://standards.iso.org/iso/19115/-3/srv/2.0");
        xmlw.writeAttribute("xmlns:mac", "http://standards.iso.org/iso/19115/-3/mac/2.0");
        xmlw.writeAttribute("xmlns:mas", "http://standards.iso.org/iso/19115/-3/mas/1.0");
        xmlw.writeAttribute("xmlns:mcc", "http://standards.iso.org/iso/19115/-3/mcc/1.0");
        xmlw.writeAttribute("xmlns:mco", "http://standards.iso.org/iso/19115/-3/mco/1.0" );
        xmlw.writeAttribute("xmlns:mda", "http://standards.iso.org/iso/19115/-3/mda/1.0");
        xmlw.writeAttribute("xmlns:mdb","http://standards.iso.org/iso/19115/-3/mdb/2.0");
        xmlw.writeAttribute("xmlns:mdt", "http://standards.iso.org/iso/19115/-3/mdt/1.0");
        xmlw.writeAttribute("xmlns:mex", "http://standards.iso.org/iso/19115/-3/mex/1.0");
        xmlw.writeAttribute("xmlns:mrl", "http://standards.iso.org/iso/19115/-3/mrl/1.0");
        xmlw.writeAttribute("xmlns:mds","http://standards.iso.org/iso/19115/-3/mds/1.0");
        xmlw.writeAttribute("xmlns:mmi", "http://standards.iso.org/iso/19115/-3/mmi/1.0");
        xmlw.writeAttribute("xmlns:mpc", "http://standards.iso.org/iso/19115/-3/mpc/1.0");
        xmlw.writeAttribute("xmlns:mrc", "http://standards.iso.org/iso/19115/-3/mrc/2.0");
        xmlw.writeAttribute("xmlns:mrd", "http://standards.iso.org/iso/19115/-3/mrd/1.0");
        xmlw.writeAttribute("xmlns:mri", "http://standards.iso.org/iso/19115/-3/mri/1.0");
        xmlw.writeAttribute("xmlns:mrs", "http://standards.iso.org/iso/19115/-3/mrs/1.0");
        xmlw.writeAttribute("xmlns:msr","http://standards.iso.org/iso/19115/-3/msr/2.0");
        xmlw.writeAttribute("xmlns:mdq", "http://standards.iso.org/iso/19157/-2/mdq/1.0" );
        xmlw.writeAttribute("xmlns:dqc", "http://standards.iso.org/iso/19157/-2/dqc/1.0");
        xmlw.writeAttribute("xmlns:gco", "http://standards.iso.org/iso/19115/-3/gco/1.0");
        xmlw.writeAttribute("xmlns:gfc", "http://standards.iso.org/iso/19110/gfc/1.1");
        xmlw.writeAttribute("xmlns:gml", "http://www.opengis.net/gml/3.2" );
        xmlw.writeAttribute("xmlns:xlink","http://www.w3.org/1999/xlink" );
        xmlw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
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
        xmlw.writeAttribute("codeList" ,"http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode");
        xmlw.writeAttribute("codeListValue","multimediaHardcopy");
        xmlw.writeCharacters("multimediaHardcopy");
        xmlw.writeEndElement(); //cit:CI_PresentationFormCode
        xmlw.writeEndElement(); //cit:presentationForm
        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mcc:authority
        xmlw.writeStartElement("mcc:code");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(protocol+ ":"+ authority + "/" + persistentId);
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
        xmlw.writeAttribute("codeList","http://www.loc.gov/standards/iso639-2/");
        xmlw.writeAttribute("codeListValue", "eng");
        xmlw.writeCharacters(language);
        xmlw.writeEndElement(); //LanguageCode
        xmlw.writeEndElement(); //language
        xmlw.writeStartElement("lan:country");
        xmlw.writeStartElement("lan:CountryCode");
        xmlw.writeAttribute("codeList","http://www.loc.gov/standards/iso366-1/");
        xmlw.writeAttribute("codeListValue", "can");
        xmlw.writeCharacters("can");
        xmlw.writeEndElement(); //CountryCode
        xmlw.writeEndElement(); //country
        xmlw.writeStartElement("lan:characterEncoding");
        xmlw.writeStartElement("lan:MD_CharacterSetCode");
        xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#MD_CharacterSetCode");
        xmlw.writeAttribute("codeListValue", "utf8");
        xmlw.writeCharacters("utf8");
        xmlw.writeEndElement(); //MD_CharacterSetCode
        xmlw.writeEndElement(); //characterEncoding
        xmlw.writeEndElement(); //lan:PT_Locale
        xmlw.writeEndElement(); //mdb:defaultLocale

    }

    private static void writeEmptyPrereq(XMLStreamWriter xmlw) throws XMLStreamException {

        xmlw.writeStartElement("mdb:parentMetadata");
        xmlw.writeEndElement();

        xmlw.writeStartElement("mdb:metadataScope");
        xmlw.writeEndElement();

        xmlw.writeStartElement("mdb:contact");
        xmlw.writeEndElement();

//        <mdb:parentMetadata/>        <!-- optional, empty -->
//                <mdb:metadataScope/>         <!-- optional, empty -->
//                <mdb:contact/>               <!-- optional, empty -->
    }

    private static void writeDateInfo(XMLStreamWriter xmlw, Field descriptionF) throws XMLStreamException {
        logger.info("writeDateInfo");
        if (descriptionF != null) {

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
                    xmlw.writeStartElement("mdb:dateInfo");
                    dateISO(xmlw, date, "publication"); //For description (abstract) date code is not clear
                    xmlw.writeEndElement(); //mdb:dateInfo
                }
            }
        }
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
        xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_DateTypeCode");
        xmlw.writeAttribute("codeListValue", code);
        xmlw.writeCharacters(code);
        xmlw.writeEndElement(); //cit:CI_DateTypeCode
        xmlw.writeEndElement(); //cit:dateType
        xmlw.writeEndElement(); //cit:CI_Date
        logger.info("dateISO end");

    }

    private static void writeAlternativeMetadataReference(XMLStreamWriter xmlw, Field otherIdF) throws XMLStreamException {
        String otherId = "";
        String otherIdAgency = "";
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

                if (otherId != null && !otherId.isEmpty()) {
                    xmlw.writeStartElement("mdb:alternativeMetadataReference");
                    xmlw.writeStartElement("cit:CI_Citation");
                    //mcc:codeSpace/gco:CharacterString
//                    xmlw.writeStartElement("cit:title");
//                    xmlw.writeAttribute("xsi:type", "lan:PT_FreeText_PropertyType");
//                    xmlw.writeStartElement("gco:CharacterString");
//                    xmlw.writeCharacters(otherIdAgency);
//                    xmlw.writeEndElement(); //gco:CharacterString
//                    xmlw.writeEndElement(); //cit:title
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
                    xmlw.writeEndElement(); //mcc:MD_Identifier
                    xmlw.writeEndElement(); //cit:identifier
                    xmlw.writeStartElement("cit:presentationForm");
                    xmlw.writeStartElement("cit:CI_PresentationFormCode");
                    xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode");
                    xmlw.writeAttribute("codeListValue", "documentDigital");
                    xmlw.writeCharacters("documentDigital");
                    xmlw.writeEndElement(); //cit:CI_PresentationFormCode
                    xmlw.writeEndElement(); //cit:presentationForm
                    xmlw.writeEndElement(); //cit:CI_Citation
                    xmlw.writeEndElement(); //mdb:alternativeMetadataReference
                }
            }
        }
    }

    private static void writeSpatialRepresentationInfo(XMLStreamWriter xmlw, Field geometricObjectCountF, Field geometricObjectTypeCodeF) throws XMLStreamException {
        String geometricObjectCount = "";
        if (geometricObjectCountF != null) {
            geometricObjectCount = ((PrimitiveField) geometricObjectCountF).getSingleValue();
        }
        String geometricObjectTypeCode = "";
        if (geometricObjectTypeCodeF!=null) {
            geometricObjectTypeCode = ((ControlledVocabularyField) geometricObjectTypeCodeF).getSingleValue();
        }

        if ((geometricObjectCount != null && !geometricObjectCount.isEmpty()) ||
                (geometricObjectTypeCode != null && !geometricObjectTypeCode.isEmpty())) {
            xmlw.writeStartElement("mdb:spatialRepresentationInfo");
            //xmlw.writeStartElement("msr:MD_SpatialRepresentation");
            xmlw.writeStartElement("msr:MD_VectorSpatialRepresentation");
            xmlw.writeStartElement("msr:geometricObjects");
            xmlw.writeStartElement("msr:MD_GeometricObjects");

            if (geometricObjectTypeCode != null && !geometricObjectTypeCode.isEmpty()) {
                xmlw.writeStartElement("msr:geometricObjectType");
                xmlw.writeStartElement("msr:MD_GeometricObjectTypeCode");
                xmlw.writeAttribute("codeSpace","ISOTC211/19115");
                xmlw.writeAttribute("codeList","http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_GeometricObjectTypeCode");
                xmlw.writeAttribute("codeListValue",geometricObjectTypeCode);
                xmlw.writeCharacters(geometricObjectTypeCode);
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
            //xmlw.writeEndElement(); //msr:MD_SpatialRepresentation
            xmlw.writeEndElement(); //mdb:spatialRepresentationInfo
        }

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
                                         Field title, Field alternativeTitle, Field distributionDate, Field geoReferenceDate,
                                         Field topicClass, Field note, Field series, Field software,
                                         Field spatialResolution, Field spatialRepresentationType, String termsOfuse,
                                         Field datasetContact, Field description) throws XMLStreamException {
        xmlw.writeStartElement("mdb:identificationInfo");
        xmlw.writeStartElement("mri:MD_DataIdentification");
        writeCitation(xmlw, author, title, alternativeTitle, distributionDate, geoReferenceDate, series);

        writeAbstractAndPurpose(xmlw, description);
        writePointOfContact(xmlw, datasetContact);
        writeSpatialRepresentationType(xmlw, spatialRepresentationType);
        writeSpatialResolution(xmlw,spatialResolution);
        writeTopicClass(xmlw, topicClass);
        writeExtent(xmlw, geographicBoundingBox);
        writeDescritiveKeywords(xmlw, keyword);
        writeResourceConstrains(xmlw, termsOfuse);
        writeSoftware(xmlw, software);
        writeNote(xmlw, note);
        xmlw.writeEndElement();
        xmlw.writeEndElement();
    }

    private static void writeCitation(XMLStreamWriter xmlw, Field authorF,Field titleF,
                               Field alternativeTitleF, Field distributionDateF,
                               Field geoReferenceDateF, Field series ) throws XMLStreamException {
        xmlw.writeStartElement("mri:citation");
        xmlw.writeStartElement("cit:CI_Citation");
        title(xmlw, titleF);
        alternativeTitle(xmlw, alternativeTitleF);
        if (distributionDateF != null) {
            String distributionDate = ((PrimitiveField) distributionDateF).getSingleValue();
            if (distributionDate != null && !distributionDate.isEmpty()) {
                distributionDate(xmlw, distributionDate);
            }
        }
        if (geoReferenceDateF != null) {
            geoReferenceDate(xmlw, geoReferenceDateF);
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


        if (series != null) {
            writeSeries(xmlw, series);
        }
        xmlw.writeEndElement(); //cit:CI_Citation
        xmlw.writeEndElement(); //mri:citation
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

    private static void distributionDate(XMLStreamWriter xmlw, String distributionDate) throws XMLStreamException {
        xmlw.writeStartElement("cit:date");
        dateISO(xmlw, distributionDate, "distribution");
        xmlw.writeEndElement();//cit:date
    }

    private static void geoReferenceDate(XMLStreamWriter xmlw, Field geoReferenceDateF) throws XMLStreamException {
        for (HashMap<String, Field> foo : ((CompoundField) geoReferenceDateF).getMultipleValues()) {
            String geoReferenceDateType = "";
            String geoReferenceDateValue = "";

            Field geoReferenceDateTypeF = foo.get("geoReferenceDateType");
            Field geoReferenceDateValueF = foo.get("geoReferenceDateValue");
            if (geoReferenceDateTypeF != null) {
                geoReferenceDateType = ((ControlledVocabularyField) geoReferenceDateTypeF).getSingleValue();
            }
            if (geoReferenceDateValueF != null) {
                geoReferenceDateValue = ((PrimitiveField) geoReferenceDateValueF).getSingleValue();
            }


            if (geoReferenceDateValue != null && !geoReferenceDateValue.isEmpty()) {
                xmlw.writeStartElement("cit:date");
                dateISO(xmlw, geoReferenceDateValue, geoReferenceDateType);
                xmlw.writeEndElement();//cit:date
            }
        }
    }

    private static void responsibleParty(XMLStreamWriter xmlw, String name, String role, String affiliation) throws XMLStreamException {
        xmlw.writeStartElement("cit:citedResponsibleParty");
        xmlw.writeStartElement("cit:CI_Responsibility");
        xmlw.writeStartElement("cit:role");
        xmlw.writeStartElement("cit:CI_RoleCode");
        xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_RoleCode");
        xmlw.writeAttribute("codeListValue", role);
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
    }

    private static void writeSeries(XMLStreamWriter xmlw, Field seriesF) throws XMLStreamException {
        for (HashMap<String, Field> foo : ((CompoundField) seriesF).getMultipleValues()) {
            String seriesName = "";
            Field seriesNameF = foo.get("seriesName");

            if (seriesNameF != null) {
                seriesName = ((PrimitiveField) seriesNameF).getSingleValue();
            }

            if (seriesName != null && !seriesName.isEmpty()) {
                xmlw.writeStartElement("cit:series");
                xmlw.writeStartElement("cit:CI_Series");
                xmlw.writeStartElement("cit:name");
                xmlw.writeStartElement("gco:CharacterString");
                xmlw.writeCharacters(seriesName);
                xmlw.writeEndElement(); //gco:CharacterString
                xmlw.writeEndElement(); //cit:name
                xmlw.writeEndElement(); //cit:CI_Series
                xmlw.writeEndElement(); //cit:series
            }
        }
        //cit:series/cit:CI_Series/cit:name/gco:CharacterString
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
                xmlw.writeAttribute("codeSpace", "ISOTC211/19115");
                xmlw.writeAttribute("codeList", "mcc:MD_SpatialRepresentationTypeCode");
                xmlw.writeAttribute("codeListValue", spatialRepresentationType);
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

                Field spatialResolutionValueF = foo.get("spatialResolutionValue");
                Field spatialResolutionTypeF = foo.get("spatialResolutionType");

                if (spatialResolutionValueF != null) {
                    spatialResolutionValue = ((PrimitiveField) spatialResolutionValueF).getSingleValue();
                }
                if (spatialResolutionTypeF != null) {
                    spatialResolutionType = ((ControlledVocabularyField) spatialResolutionTypeF).getSingleValue();
                }

                if (spatialResolutionValue != null && !spatialResolutionValue.isEmpty()) {
                    xmlw.writeStartElement("mri:spatialResolution");
                    xmlw.writeStartElement("mri:MD_Resolution");
                    if (spatialResolutionType.equals("equivalentScale")) {
                        xmlw.writeStartElement("mri:equivalentScale");
                        xmlw.writeStartElement("mri:MD_RepresentativeFraction");
                        xmlw.writeStartElement("mri:denominator");
                        xmlw.writeStartElement("gco:Integer");
                        xmlw.writeCharacters(spatialResolutionValue);
                        xmlw.writeEndElement(); //gco:Integer
                        xmlw.writeEndElement(); //mri:denominator
                        xmlw.writeEndElement(); //mri:MD_RepresentativeFraction
                        xmlw.writeEndElement(); //mri:equivalentScale

                    }
                    xmlw.writeEndElement(); //mri:MD_Resolution
                    xmlw.writeEndElement(); //mri:spatialResolution
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
                    xmlw.writeCharacters(topicClassificationValue);
                    xmlw.writeEndElement(); //mri:MD_TopicCategoryCode
                    xmlw.writeEndElement(); //mri:topicCategory
                }
            }
        }
    }

    private static void writeExtent(XMLStreamWriter xmlw, Field geographicBoundingBoxF) throws XMLStreamException {
        /* Only 1 geoBndBox is
           So, I'm just going to arbitrarily use the first one, and ignore the rest! */
        if (geographicBoundingBoxF != null) {
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

            xmlw.writeStartElement("mri:extent");
            xmlw.writeStartElement("gex:EX_Extent");
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
    }

    private static void writeDescritiveKeywords(XMLStreamWriter xmlw, Field keywordsF) throws XMLStreamException {
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
                }
                if (keywordURIF != null) {
                    keywordURI = ((PrimitiveField) keywordURIF).getSingleValue();
                }


                if (keywordValue != null && !keywordValue.isEmpty()) {

                    xmlw.writeStartElement("mri:descriptiveKeywords");
                    xmlw.writeStartElement("mri:MD_Keywords");
                    xmlw.writeStartElement("mri:keyword");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(keywordValue);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mri:keyword
                    xmlw.writeStartElement("mri:type");
                    xmlw.writeStartElement("mri:MD_KeywordTypeCode");
                    xmlw.writeAttribute("codeSpace", "ISOTC211/19115");
                    xmlw.writeAttribute("codeList", "http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#MD_KeywordTypeCode");
                    xmlw.writeAttribute("codeListValue", "theme");
                    xmlw.writeCharacters("theme");
                    xmlw.writeEndElement(); //mri:MD_KeywordTypeCode
                    xmlw.writeEndElement(); //mri:type
                    xmlw.writeStartElement("mri:thesaurusName");
                    xmlw.writeStartElement("cit:CI_Citation");
                    xmlw.writeStartElement("cit:title");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(keywordVocab);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //cit:title
                    xmlw.writeStartElement("cit:presentationForm");
                    xmlw.writeStartElement("cit:CI_PresentationFormCode");
                    xmlw.writeAttribute("codeList", "http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_PresentationFormCode");
                    xmlw.writeAttribute("codeListValue", "documentDigital");
                    xmlw.writeCharacters("documentDigital");
                    xmlw.writeEndElement(); //cit:CI_PresentationFormCode
                    xmlw.writeEndElement(); //cit:presentationForm
                    xmlw.writeEndElement(); //cit:CI_Citation
                    xmlw.writeEndElement(); //mri:thesaurusName
                    xmlw.writeEndElement(); //mri:MD_Keywords
                    xmlw.writeEndElement(); //mri:descriptiveKeywords
                }
            }
        }
    }

    private static void writeResourceConstrains(XMLStreamWriter xmlw, String termsOfUse ) throws XMLStreamException {
        logger.info("writeResourceConstrains");
        if (termsOfUse != null && !termsOfUse.isEmpty()) {
            xmlw.writeStartElement("mri:resourceConstraints");
            xmlw.writeStartElement("mco:MD_LegalConstraints");
            xmlw.writeStartElement("mco:useConstraints");
            xmlw.writeStartElement("mco:MD_RestrictionCode");
            xmlw.writeAttribute("codeList", "standards.iso.org/19115/-3/lan/1.0/codelists.xml#MD_RestrictionCode");
            xmlw.writeAttribute("codeListValue", termsOfUse);
            xmlw.writeCharacters(termsOfUse);
            xmlw.writeEndElement(); //mco:MD_RestrictionCode
            xmlw.writeEndElement(); //mco:useConstraints
            xmlw.writeEndElement(); //mco:MD_LegalConstraints
            xmlw.writeEndElement(); //mri:resourceConstraints
        }
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

    private static void writeDistributionInfo(XMLStreamWriter xmlw, Field distributionF) throws XMLStreamException {
        if (distributionF != null) {
            List<HashMap<String, Field>> distibution = ((CompoundField) distributionF).getMultipleValues();
            if (distibution.size() > 0) {
                xmlw.writeStartElement("mdb:distributionInfo");
                xmlw.writeStartElement("mrd:MD_Distribution");
                xmlw.writeStartElement("mrd:transferOptions");
                xmlw.writeStartElement("mrd:MD_DigitalTransferOptions");
                for ( HashMap<String, Field> foo : distibution) {
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



                    onLine(xmlw, distributionLinkLabel, distributionLink, protocol);

                }
                xmlw.writeEndElement(); //mrd:MD_DigitalTransferOptions
                xmlw.writeEndElement(); //mrd:transferOptions
                xmlw.writeEndElement(); //mrd:MD_Distribution
                xmlw.writeEndElement(); //mdb:distributionInfo

            }
        }
    }

    private static void  onLine(XMLStreamWriter xmlw, String distributionLinkLabel, String distributionLink, String protocol) throws XMLStreamException {
        xmlw.writeStartElement("mrd:onLine");
        xmlw.writeStartElement("cit:CI_OnlineResource");
        xmlw.writeStartElement("cit:linkage");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(distributionLink);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:linkage
        xmlw.writeStartElement("cit:protocol");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(protocol);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:protocol
        xmlw.writeStartElement("cit:name");
        xmlw.writeStartElement("gco:CharacterString");
        xmlw.writeCharacters(distributionLinkLabel);
        xmlw.writeEndElement(); //gco:CharacterString
        xmlw.writeEndElement(); //cit:name
        xmlw.writeStartElement("cit:function");
        xmlw.writeStartElement("cit:CI_OnLineFunctionCode");
        xmlw.writeAttribute("codeList","http://standards.iso.org/iso/19115/resources/Codelist/cat/codeLists.xml#CI_OnLineFunctionCode" );
        xmlw.writeAttribute( "codeListValue","fileAccess");
        xmlw.writeCharacters("fileAccess");
        xmlw.writeEndElement(); //cit:CI_OnLineFunctionCode
        xmlw.writeEndElement(); //cit:function
        xmlw.writeEndElement(); //cit:CI_OnlineResource
        xmlw.writeEndElement(); //mrd:onLine
    }

    private static void writeResourceLineage(XMLStreamWriter xmlw, Field lineageStatementF,
                                      Field sourceDescriptionF, Field processStepF) throws XMLStreamException {
        if (lineageStatementF != null || sourceDescriptionF != null || processStepF != null) {
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
            if (sourceDescriptionF != null) {
                for (String source : ((PrimitiveField) sourceDescriptionF).getMultipleValues()) {
                    xmlw.writeStartElement("mrl:source");
                    xmlw.writeStartElement("mrl:LI_Source");
                    xmlw.writeStartElement("mrl:description");
                    xmlw.writeStartElement("gco:CharacterString");
                    xmlw.writeCharacters(source);
                    xmlw.writeEndElement(); //gco:CharacterString
                    xmlw.writeEndElement(); //mrl:description
                    xmlw.writeEndElement(); //mrl:LI_Source
                    xmlw.writeEndElement(); //mrl:source
                }
            }
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
