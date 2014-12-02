package edu.ucla.library.libservices.aeon.callslip.xml;

import edu.ucla.library.libservices.aeon.callslip.beans.Request;

import java.io.File;
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
      theRequest.setBarcode( document.getElementsByTagName( "item-barcode" ).item( 0 ).getChildNodes().item( 0 ).getTextContent() );
      theRequest.setLibrary( document.getElementsByTagName( "pickup-location" ).item( 0 ).getChildNodes().item( 0 ).getTextContent() );
      theRequest.setNote( document.getElementsByTagName( "request-note" ).item( 0 ).getChildNodes().item( 0 ).getTextContent() );
    }
    catch ( ParserConfigurationException pce )
    {
      pce.printStackTrace();
    }
    catch ( SAXException saxe )
    {
      saxe.printStackTrace();
    }
    catch ( IOException ioe )
    {
      ioe.printStackTrace();
    }
    return theRequest;
  }

  public void setTheFile( File theFile )
  {
    this.theFile = theFile;
  }

  private File getTheFile()
  {
    return theFile;
  }
}
