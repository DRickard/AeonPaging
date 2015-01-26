package edu.ucla.library.libservices.aeon.callslip.xml;

import edu.ucla.library.libservices.aeon.callslip.beans.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public class DownloadReader
{
  private Request theRequest;
  private File theFile;

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
      System.out.println( pce.getMessage() );
      getAsText();
    }
    catch ( SAXException saxe )
    {
      //saxe.printStackTrace();
      System.out.println( saxe.getMessage() );
      getAsText();
    }
    catch ( IOException ioe )
    {
      //ioe.printStackTrace();
      System.out.println( ioe.getMessage() );
    }
    catch ( Exception e )
    {
      System.out.println( e.getMessage() );
      getAsText();
    }
    System.out.println( "in DownloadReader.getTheRequest, barcode = " + theRequest.getBarcode() + "\tlibrary = " + theRequest.getLibrary() + "\tnote = " + theRequest.getNote() );
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
      System.out.println( "in DownloadReader.getAsText, barcode = " + theRequest.getBarcode() + "\tlibrary = " + theRequest.getLibrary() + "\tnote = " + theRequest.getNote() );
    }
    catch ( FileNotFoundException fnfe )
    {
      System.out.println( fnfe.getMessage() );
    }
    catch ( IOException ioe )
    {
      System.out.println( ioe.getMessage() );
    }
  }
}
