package io.gdcc.export.iso19115_3;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import jakarta.ws.rs.core.MediaType;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

@AutoService(Exporter.class)
public class ISO19115_3Exporter implements Exporter {
    private static final Logger logger = Logger.getLogger(ISO19115_3Exporter.class.getCanonicalName());

    @Override
    public String getFormatName() {
        return "iso19115_3";
    }

    @Override
    public String getDisplayName(Locale locale) {
        //String displayName = BundleUtil.getStringFromBundle("dataset.exportBtn.itemLabel.pdf", locale);
        String displayName = null;
        return Optional.ofNullable(displayName).orElse("ISO 19115-3");
    }

    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        Optional<InputStream> ddiInputStreamOptional = dataProvider.getPrerequisiteInputStream();
        logger.info("ISO19115_3Exporter: exportDataset called with prerequisite input stream");
        if (ddiInputStreamOptional.isPresent()) {
            try (InputStream jsonInputStream = ddiInputStreamOptional.get()) {
                ISO19115_3ExportUtil.parseDataverseJson(jsonInputStream, outputStream);
//                XMLStreamWriter xmlw = null;
//                xmlw = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
//                xmlw.writeStartDocument();
//                xmlw.flush();
//                logger.info("Finish writing xml header");
//                xmlw.writeStartElement("MD_Metadata");
//                xmlw.writeEndElement();
//                xmlw.writeEndDocument();
//                if (xmlw != null) {
//                    logger.info("Closing xml writer");
//                    try {
//                        xmlw.close();
//                    } catch (XMLStreamException e) {
//                        // Log this exception, but don't rethrow as it's not the primary issue
//                        e.printStackTrace();
//                    }
//                }
                logger.info("GOOD");

                // ISO19115_3ExportUtil.datasetISO19115_3(ddiInputStream, outputStream);
            } catch (IOException e) {
                throw new ExportException("Cannot open export_dataverse_json cached file");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//            } catch (XMLStreamException xse) {
//                throw new ExportException("Caught XMLStreamException performing DDI export");
//            }
        } else {
            throw new ExportException("No prerequisite input stream found");
        }
    }

    @Override
    public Boolean isHarvestable() {
        // No, we don't want this format to be harvested!
        // For datasets with tabular data the <data> portions of the DDIs
        // become huge and expensive to parse; even as they don't contain any
        // metadata useful to remote harvesters. -- L.A. 4.5
        return false;
    }

    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    @Override
    public  Optional<String> getPrerequisiteFormatName() {
        //This exporter relies on being able to get the output of the dataverse_json exporter
        return Optional.of("dataverse_json");
    }

    @Override
    public String  getMediaType() {
        return MediaType.APPLICATION_XML;
    }
}



