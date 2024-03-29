/*
 * Copyright (C) 2014 Seth Shaw.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.dataaccessioner;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.apache.tika.metadata.DublinCore;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.transform.XSLTransformer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Seth Shaw
 */
public class MetadataManager {

    public final static int STATUS_FAILURE = -20;
    public final static int STATUS_SUCCESS = 30;
    
    private static Logger logger;
    
    private static File xmlFile;
    private static Document document;
    private static Element currentElement;
    private static final Namespace DEFAULT_NAMESPACE = Namespace.getNamespace("http://dataaccessioner.org/schema/dda-1-1");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    private static final Namespace DC_NAMESPACE = Namespace.getNamespace("dc","http://purl.org/dc/elements/1.1/");
    private static final Namespace DC_XML = Namespace.getNamespace("dcx","http://purl.org/dc/xml/");
    protected static final List<Property> DC_ELEMENTS = Arrays.asList(
        DublinCore.CONTRIBUTOR,
        DublinCore.COVERAGE,
//        DublinCore.CREATED,  //TERMS element, not currently supported
        DublinCore.CREATOR,
        DublinCore.DATE,
        DublinCore.DESCRIPTION,
        DublinCore.FORMAT,
        DublinCore.IDENTIFIER,
        DublinCore.LANGUAGE,
//        DublinCore.MODIFIED, //TERMS element, not currently supported
        DublinCore.PUBLISHER,
        DublinCore.RELATION,
        DublinCore.RIGHTS,
        DublinCore.SOURCE,
        DublinCore.SUBJECT,
        DublinCore.TITLE,
        DublinCore.TYPE
    );

    private String DEFAULT_XSLT = "xml/metadataManager.xsl";
    
    private File metadataFile = null;
    private String collectionName = "";
    private String accessionNumber = "";
    private String submitterName = "";
    private String srcNote = "";
    private String addNote = "";
    private String now = "";
    private HashMap<String, Metadata> annotatedFiles = new HashMap<String, Metadata>(); //Hash key is the file's absolute path
    
    public static String getName() {
        return "Full Metadata";
    }
    private Transformer transformer;

    public MetadataManager(File metadataFile, HashMap<String,String> daMetadata) {
        this.metadataFile = metadataFile;
        this.collectionName = daMetadata.get("collectionName");
        this.accessionNumber = daMetadata.get("accessionNumber");
        this.submitterName = daMetadata.get("submitterName");
        this.srcNote = daMetadata.get("aboutSourceNote");
        this.addNote = daMetadata.get("addNote");

        logger = LoggerFactory.getLogger(MetadataManager.class);
        try {
            InputStream inp = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_XSLT);
            TransformerFactory tf = getTransformerFactory();
            transformer = tf.newTransformer(new StreamSource(inp));
        } catch ( XPathException | TransformerConfigurationException ex) {
            logger.warn("Unable to setup FITS XSLT. Metadata will include full FITS xml.");
        }
    }
    
    public void open() throws Exception {
        Date rightNow  = new Date();
        this.now = rightNow.toString();

        xmlFile = metadataFile;
        xmlFile.getParentFile().mkdirs(); //In case it doesn't exist
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            Element collection = new Element("collection", DEFAULT_NAMESPACE);
            collection.setAttribute("name", collectionName);
            document = new Document(collection);
            Element accessionElement = new Element("accession", DEFAULT_NAMESPACE);
            accessionElement.setAttribute("number", accessionNumber);
            collection.addContent(accessionElement);
            Element ingest_note = new Element("ingest_note", DEFAULT_NAMESPACE);
            ingest_note.setText(collectionName + " transferred by " + submitterName + " on " + now);
            accessionElement.addContent(ingest_note);
            if (! srcNote.isEmpty()) {
                Element source_note = new Element("source_note", DEFAULT_NAMESPACE);
                source_note.setText(srcNote);
                accessionElement.addContent(source_note);
            }
            if (! addNote.isEmpty()) {
                Element add_notes = new Element("additional_notes", DEFAULT_NAMESPACE);
                add_notes.setText(addNote);
                accessionElement.addContent(add_notes);
            }
        } else {
            try {
                document = new SAXBuilder().build(xmlFile);
            } catch (JDOMException ex) {
                throw new Exception("Could not read the metadata file "
                        + xmlFile.getPath() + ": " + ex.getLocalizedMessage());
            }
        }

        currentElement = document.getRootElement()
                .getChild("accession", DEFAULT_NAMESPACE);
    }
    
    public void close(long elapsedTime) {
        setElapsedTime(elapsedTime);
        FileOutputStream stream = null;
        OutputStreamWriter osw = null;
        PrintWriter output = null;
        try {
            stream = new FileOutputStream(xmlFile);
            osw = new OutputStreamWriter(stream, "UTF-8");
            output = new PrintWriter(osw);
            Format format = Format.getPrettyFormat();
            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(document, output);
        } catch (IOException ex) {
            System.err.println("Could not write to metadata file " 
                    + xmlFile.getPath() + ": " + ex.getMessage());
        } finally {
            try {
                output.close();
                osw.close();
                stream.close();
            } catch (Exception ex) {
                System.err.println("Failed to close output streams when writting out " 
                        + xmlFile.getName() + ": " + ex.getMessage());
            }
        }
    }

    private void setElapsedTime(long elapsedTime) {
        Element ingestTime = new Element("ingest_time", DEFAULT_NAMESPACE);
        ingestTime.setText(getDuration(elapsedTime));
        Element parent = document.getRootElement()
                .getChild("accession", DEFAULT_NAMESPACE);
        Element ingestNote = parent.getChild("ingest_note", DEFAULT_NAMESPACE);
        int index = parent.indexOf(ingestNote);
        if (index > -1) {
            parent.addContent(index + 1, ingestTime);
        }else {
            parent.addContent(ingestTime);
        }
    }

    public String getDuration(long elapsedTime) {
        String hhmmssms = String.format("%02d:%02d:%02d.%03d",
                TimeUnit.MILLISECONDS.toHours(elapsedTime),
                TimeUnit.MILLISECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)),
                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)),
                elapsedTime - TimeUnit.MILLISECONDS.toSeconds(elapsedTime));
        return hhmmssms;
    }

    public void cancel() {
        // Free up everything.
        xmlFile = null;
        currentElement = null;
        document.removeContent();
        document = null;
        System.gc();
    }

    public void startFile(File file) {
        Element fileNode = new Element("file", DEFAULT_NAMESPACE);
        currentElement.addContent(fileNode);
        fileNode.setAttribute("name", file.getName());

        String last_modified = DATE_FORMAT.format(new Date(file.lastModified()));
        fileNode.setAttribute("last_modified", last_modified);

        if (file.isHidden() == true) {
            fileNode.setAttribute("hidden", "true");
        }
        fileNode.setAttribute("size", String.valueOf(file.length()));
        currentElement = fileNode;
        processFileAnnotations(file);
    }

    public void startFile(File file, String newName){
        startFile(file);
        currentElement.setAttribute("name", newName);
    }
    
    public void startDirectory(File directory) {
        Element newDir = new Element("folder", DEFAULT_NAMESPACE);
        newDir.setAttribute("name", directory.getName());

        String last_modified = DATE_FORMAT.format(new Date(directory.lastModified()));
        newDir.setAttribute("last_modified", last_modified);

        if (directory.isHidden()) {
            newDir.setAttribute("hidden", "true");
        }
        
        currentElement.addContent(newDir);
        currentElement = newDir;
        processFileAnnotations(directory);
    }
    
    public void startDirectory(File directory, String newName){
        startDirectory(directory);
        //Overwrite the existing name element.
        currentElement.setAttribute("name", newName);
    }

    public void closeCurrentElement() {
        //TODO: Add Additional Metadata
        currentElement = currentElement.getParentElement();
    }

    public void addAttribute(String name, String value){
        currentElement.setAttribute(name, value);
    }
    public void addElement(Element element){
        currentElement.addContent(element);
    }

    public boolean addDocumentXSLT(Document document){
        try {
            if (transformer != null) {
                JDOMResult premis = new JDOMResult();
                transformer.transform(new JDOMSource(document),premis);
                addElement(premis.getDocument().detachRootElement());
            } else {
                addElement(document.detachRootElement());
            }
        } catch (TransformerException te) {
            logger.error("Failed to transform or add FITS XML to metadata.");
            return false;
        }

        return true;
    }
    
       
    public Metadata getFileAnnotation(File file){
        if(annotatedFiles.containsKey(file.getAbsolutePath())){
            return annotatedFiles.get(file.getAbsolutePath());
        } else {
            return new Metadata();
        }
    }
    
    public void setFileAnnotation(File file, Metadata metadata){
        annotatedFiles.put(file.getAbsolutePath(), metadata);
    }
    
    private void processFileAnnotations(File file){
        Metadata metadata = getFileAnnotation(file);
        if(metadata.size() < 1){return;} //Nothing to do here.
        Element dcDescription = new Element("description", DC_XML);
        for(Property property: DC_ELEMENTS){
            for(String value: metadata.getValues(property)){
                
                Element dcStatement = new Element(property.getName()
                        .substring(property.getName().indexOf(":")+1), //Ugly hack to rip off the prefix that doesn't belong here.
                        DC_NAMESPACE);
                dcStatement.addContent(value);
                dcDescription.addContent(dcStatement);
            }
        }
        currentElement.addContent(dcDescription);
    }

    // Saxon-HE custom integrated function to return random UUID

    private TransformerFactory getTransformerFactory() throws net.sf.saxon.trans.XPathException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        if(tFactory instanceof TransformerFactoryImpl) {
            TransformerFactoryImpl tFactoryImpl = (TransformerFactoryImpl) tFactory;
            net.sf.saxon.Configuration saxonConfig = tFactoryImpl.getConfiguration();
            saxonConfig.registerExtensionFunction(new XSLuuid());
        }
        return tFactory;
    }
    public class XSLuuid extends ExtensionFunctionDefinition {

        private final SequenceType[] argumentTypes = new SequenceType[]{};

        @Override
        public StructuredQName getFunctionQName() {
            return new StructuredQName("da", "http://dataaccessioner.org/saxon-extension", "get-uuid");
        }

        @Override
        public SequenceType[] getArgumentTypes() {
            return argumentTypes;
        }

        public int getMinimumNumberOfArguments() {
            return 0;
        }

        @Override
        public int getMaximumNumberOfArguments() {
            return 0;
        }

        @Override
        public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
            return SequenceType.SINGLE_STRING;
        }

        @Override
        public ExtensionFunctionCall makeCallExpression() {
            return new CreateUUID();
        }
    }
    private class CreateUUID extends ExtensionFunctionCall {

        @Override
        public Sequence call(XPathContext xPathContext, Sequence[] arguments) throws XPathException {
            UUID uuid = UUID.randomUUID();
            return StringValue.makeStringValue(uuid.toString());
        }
    }
}