package edu.ucla.library.libservices.aeon.callslip.tests;

import edu.ucla.library.libservices.aeon.callslip.xml.DownloadReader;

import java.io.File;

public class Tester
{

  public Tester()
  {
    super();
  }

  public static void main( String[] args )
  {
    DownloadReader reader;
    File theFile;
    
    theFile = new File( "C:\\Temp\\aeon\\download\\UCLA-1084-pull.txt" );
    reader = new DownloadReader();
    reader.setTheFile( theFile );
    reader.getTheRequest();
  }
}
