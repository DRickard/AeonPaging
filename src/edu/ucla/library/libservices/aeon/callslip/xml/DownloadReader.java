package edu.ucla.library.libservices.aeon.callslip.xml;

import edu.ucla.library.libservices.aeon.callslip.beans.Request;

import edu.ucla.library.libservices.aeon.callslip.main.ProcessCallslip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;
import org.apache.log4j.Logger;

public class DownloadReader
{
  private Request theRequest;
  private File theFile;

  final static Logger logger = Logger.getLogger(DownloadReader.class);

  public DownloadReader()
  {
    super();
  }

  public Request getTheRequest()
  {
    Document document;

    theRequest = new Request();

    try
    {
      document =
          DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( getTheFile() );
      theRequest.setBarcode( document.getElementsByTagName( "barcode" ).item( 0 ).getChildNodes().item( 0 ).getTextContent() );
      theRequest.setLibrary( document.getElementsByTagName( "readingroom" ).item( 0 ).getChildNodes().item( 0 ).getTextContent() );
      theRequest.setNote( "Aeon request # " + getAeonNumber() );
      theRequest.setReqID( getAeonNumber() );
    }
    catch ( ParserConfigurationException pce )
    {
      //pce.printStackTrace();
      logger.error( "error processing file ".concat( getTheFile().getName() ), pce );
      getAsText();
    }
    catch ( SAXException saxe )
    {
      //saxe.printStackTrace();
      logger.error( "error processing file ".concat( getTheFile().getName() ), saxe );
      getAsText();
    }
    catch ( IOException ioe )
    {
      //ioe.printStackTrace();
      logger.error( "error processing file ".concat( getTheFile().getName() ), ioe );
    }
    catch ( Exception e )
    {
      logger.error( "error processing file ".concat( getTheFile().getName() ), e );
      getAsText();
    }
    //System.out.println( "in DownloadReader.getTheRequest, barcode = " + theRequest.getBarcode() + "\tlibrary = " + theRequest.getLibrary() + "\tnote = " + theRequest.getNote() );
    return theRequest;
  }

  private String getAeonNumber()
  {
    String fileName;
    fileName = getTheFile().getName();
    return fileName.substring( fileName.indexOf( "-" ) + 1,
                               fileName.lastIndexOf( "-" ) );
  }

  public void setTheFile( File theFile )
  {
    this.theFile = theFile;
  }

  private File getTheFile()
  {
    return theFile;
  }

  private void getAsText()
  {
    BufferedReader reader;
    String line;

    try
    {
      reader = new BufferedReader( new FileReader( getTheFile() ) );
      line = null;
      
      while ( ( line = reader.readLine() ) != null )
      {
        if ( line.contains( "barcode" ) )
          theRequest.setBarcode( line.substring( line.indexOf( ">" ) + 1, line.lastIndexOf( "<" ) ) );
        if ( line.contains( "readingroom" ) )
          theRequest.setLibrary( line.substring( line.indexOf( ">" ) + 1, line.lastIndexOf( "<" ) ) );
      }
      theRequest.setNote( "Aeon request # " + getAeonNumber() );
      theRequest.setReqID( getAeonNumber() );
      //System.out.println( "in DownloadReader.getAsText, barcode = " + theRequest.getBarcode() + "\tlibrary = " + theRequest.getLibrary() + "\tnote = " + theRequest.getNote() );
    }
    catch ( FileNotFoundException fnfe )
    {
      logger.error( "error processing file ".concat( getTheFile().getName() ), fnfe );
    }
    catch ( IOException ioe )
    {
      logger.error( "error processing file ".concat( getTheFile().getName() ), ioe );
    }
  }
}
