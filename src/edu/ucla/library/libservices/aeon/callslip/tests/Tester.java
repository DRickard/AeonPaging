package edu.ucla.library.libservices.aeon.callslip.tests;

import edu.ucla.library.libservices.aeon.callslip.xml.UploadWriter;

public class Tester
{
  public Tester()
  {
    super();
  }

  public static void main( String[] args )
  {
    UploadWriter writer;
    writer = new UploadWriter();
    writer.getRequestBody();
  }
}
