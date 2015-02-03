package edu.ucla.library.libservices.aeon.callslip.main;

import edu.ucla.library.libservices.aeon.callslip.beans.Item;
import edu.ucla.library.libservices.aeon.callslip.beans.Patron;
import edu.ucla.library.libservices.aeon.callslip.beans.Request;
import edu.ucla.library.libservices.aeon.callslip.beans.SftpUserInfo;
import edu.ucla.library.libservices.aeon.callslip.db.mappers.ItemMapper;
import edu.ucla.library.libservices.aeon.callslip.db.mappers.PatronMapper;
import edu.ucla.library.libservices.aeon.callslip.db.source.DataSourceFactory;
import edu.ucla.library.libservices.aeon.callslip.email.ErrorMailer;
import edu.ucla.library.libservices.aeon.callslip.webclients.SendRequestClient;
import edu.ucla.library.libservices.aeon.callslip.xml.DownloadReader;
import edu.ucla.library.libservices.aeon.callslip.xml.UploadWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import org.springframework.jdbc.core.JdbcTemplate;

public class ProcessCallslip
{
  
  private static DataSource ds;
  private static File keyFile;
  private static FileObject src = null;
  private static FileSystemManager fsManager = null;
  private static FileSystemOptions opts = null;
  private static Patron bio = null; //barcode = 21158000008590
  private static Patron yrl = null; //barcode = 28285
  private static Properties props;

  final static Logger logger = Logger.getLogger(ProcessCallslip.class);
  private static final String ITEM_QUERY =
    "SELECT i.item_id,i.copy_number,bi.bib_id,mi.mfhd_id FROM " +
    "ucladb.item_barcode ib INNER JOIN ucladb.item i ON ib.item_id = " +
    "i.item_id INNER JOIN ucladb.mfhd_item mi ON ib.item_id = mi.item_id " +
    "INNER JOIN ucladb.bib_item bi ON ib.item_id = bi.item_id WHERE item_barcode = ?";
  private static final String PATRON_QUERY =
    "SELECT pb.patron_barcode,p.last_name,p.patron_id FROM " +
    "ucladb.patron_barcode pb INNER JOIN ucladb.patron p ON pb.patron_id = " +
    "p.patron_id WHERE pb.patron_barcode = ?";

  public ProcessCallslip()
  {
  }

  public static void main( String[] args )
  {
    //get config properties for application
    loadProperties( args[ 0 ] );

    //get sftp connection
    getSftpConnect();

    if ( downloadFiles() )
    {
      File localDir;
      File[] files;

      //get db connection
      makeDbConnection();

      //build YRL & BIO users
      buildPatrons();

      localDir = new File( props.getProperty( "sftp.directory.local" ) );
      files = localDir.listFiles();

      if ( files != null && files.length != 0 )
      {
        for ( File theFile: files )
        {
          Item theItem;
          Request thePull;
          SendRequestClient theClient;
          String result;
          UploadWriter writer;

          if ( theFile.isFile() )
          {
            try
            {
              logger.info( "working with file " +
                                  theFile.getName() );
              thePull = getReqFromFile( theFile );
              if ( thePull.getBarcode() == null ||
                   thePull.getBarcode().equals( "" ) )
              {
                logger.error( "file error occurred" );
                mailError( "file", theFile.getName(), thePull.getReqID(),
                           true );
                removeFile( theFile );
                theFile.renameTo( new File( props.getProperty( "sftp.directory.local" ).concat( "/error/" ).concat( theFile.getName() ) ) );
                continue;
              }

              theItem = getItem( thePull.getBarcode() );
              if ( theItem == null || theItem.getBibID() == -1 )
              {
                logger.error( "item retrieval error occurred" );
                mailError( "db", thePull.getBarcode(), thePull.getReqID(),
                           true );
                removeFile( theFile );
                theFile.renameTo( new File( props.getProperty( "sftp.directory.local" ).concat( "/error/" ).concat( theFile.getName() ) ) );
                continue;
              }

              writer = setupWriter( thePull, theItem );
              theClient = new SendRequestClient();
              theClient.setRequetURL( props.getProperty( "voyager.url" ) );
              theClient.setWriter( writer );
              result = theClient.postCallSlip();
              logger.info( "result = \n" +
                  result );
              if ( result == null || result.toLowerCase().contains( "type=\"error\"" ) )
              {
                logger.error( "mailing paging error message" );
                mailError( "error", thePull.getBarcode(),
                           thePull.getReqID(),
                           thePull.getLibrary().contains( "YRL" ) );
                removeFile( theFile );
                theFile.renameTo( new File( props.getProperty( "sftp.directory.local" ).concat( "/error/" ).concat( theFile.getName() ) ) );
              }
              else if ( result.toLowerCase().contains( "type=\"blocked\"" ) )
              {
                logger.error( "mailing paging blocked message" );
                mailError( "block", thePull.getBarcode(),
                           thePull.getReqID(),
                           thePull.getLibrary().contains( "YRL" ) );
                removeFile( theFile );
                theFile.renameTo( new File( props.getProperty( "sftp.directory.local" ).concat( "/error/" ).concat( theFile.getName() ) ) );
              }
              else
              {
                removeFile( theFile );
                theFile.delete();
              }
            }
            catch ( Exception e )
            {
              //e.printStackTrace();
              logger.fatal( "fatal error in ProcessCallslip.main" +
                                  e );
            }
          }
        }
      }
    }
  }

  private static Request getReqFromFile( File theFile )
  {
    Request thePull;
    DownloadReader theReader;

    theReader = new DownloadReader();
    theReader.setTheFile( theFile );
    thePull = theReader.getTheRequest();
    return thePull;
  }

  private static void loadProperties( String propFile )
  {
    props = new Properties();
    try
    {
      props.load( new FileInputStream( new File( propFile ) ) );
    }
    catch ( IOException ioe )
    {
      logger.fatal( "problem with props file" + ioe );
      //ioe.printStackTrace();
      System.exit( -1 );
    }
  }

  private static void makeDbConnection()
  {
    ds = DataSourceFactory.createVgerSource( props );
  }

  private static void buildPatrons()
  {
    yrl =
        ( Patron ) new JdbcTemplate( ds ).queryForObject( PATRON_QUERY, new Object[]
          { props.getProperty( "patron.yrl" ) }, new PatronMapper() );
    bio =
        ( Patron ) new JdbcTemplate( ds ).queryForObject( PATRON_QUERY, new Object[]
          { props.getProperty( "patron.bio" ) }, new PatronMapper() );
  }

  private static Item getItem( String barcode )
  {
    Item bean;
    try
    {
      bean =
          ( Item ) new JdbcTemplate( ds ).queryForObject( ITEM_QUERY, new Object[]
            { barcode.trim() }, new ItemMapper() );
    }
    catch ( Exception e )
    {
      logger.error( "db problem with item retrieval", e );
      bean = new Item();
    }
    return bean;
  }

  private static UploadWriter setupWriter( Request thePull, Item theItem )
  {
    UploadWriter local;
    local = new UploadWriter();

    local.setComment( thePull.getNote() );
    local.setDbID( props.getProperty( "voyager.dbkey" ) );
    if ( thePull.getLibrary().contains( "YRL" ) )
    {
      local.setPickUp( props.getProperty( "pickup.yrl" ) );
      local.setThePatron( yrl );
    }
    else
    {
      local.setPickUp( props.getProperty( "pickup.bio" ) );
      local.setThePatron( bio );
    }
    local.setTheItem( theItem );

    return local;
  }

  private static void getSftpConnect()
  {
    try
    {
      opts = new FileSystemOptions();
      SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking( opts,
                                                                          "no" );
      SftpFileSystemConfigBuilder.getInstance().setUserInfo( opts,
                                                             new SftpUserInfo() );
      keyFile = new File( props.getProperty( "keyfile.path" ) );
      SftpFileSystemConfigBuilder.getInstance().setIdentities( opts,
                                                               new File[]
          { keyFile } );
      fsManager = VFS.getManager();
    }
    catch ( FileSystemException fse )
    {
      logger.fatal( "problem with sftp connect" +
                          fse );
      //fse.printStackTrace();
      System.exit( -2 );
    }
  }

  private static boolean downloadFiles()
  {
    FileObject[] children;
    FileObject sftpFile;
    String startPath;

    startPath =
        "sftp://" + props.getProperty( "sftp.user" ) + "@" + props.getProperty( "sftp.host" ) +
        "/" + props.getProperty( "sftp.directory.remote" );
    try
    {
      sftpFile = fsManager.resolveFile( startPath, opts );
      children = sftpFile.getChildren();
      if ( children.length > 0 )
      {
        for ( FileObject theChild: children )
        {
          LocalFile localFile;
          StringBuffer localUrl;

          localUrl =
              new StringBuffer( "file://" ).append( props.getProperty( "sftp.directory.local" ) ).append( File.separatorChar ).append( theChild.getName().getBaseName() );
          localFile =
              ( LocalFile ) fsManager.resolveFile( localUrl.toString() );
          if ( !localFile.getParent().exists() )
          {
            localFile.getParent().createFolder();
          }
          localFile.copyFrom( theChild, new AllFileSelector() );
        }
        src = children[ 0 ];
        return true;
      }
      else
      {
        return false;
      }
    }
    catch ( FileSystemException fse )
    {
      logger.fatal( "error during download process" +
                          fse );
      //fse.printStackTrace();
      System.exit( -3 );
    }
    return true;
  }

  private static void mailError( String type, String item, String reqID,
                                 boolean isYRL )
  {
    ErrorMailer theMailer;

    theMailer = new ErrorMailer();
    theMailer.sendMessage( type, item, reqID,
                           ( isYRL ? props.getProperty( "mail.toaddress.yrl" ):
                             props.getProperty( "mail.toaddress.bio" ) ) );
  }

  private static void removeFile( File theFile )
  {
    FileObject sftpFile;
    String startPath;

    startPath =
        "sftp://" + props.getProperty( "sftp.user" ) + "@" + props.getProperty( "sftp.host" ) +
        "/" + props.getProperty( "sftp.directory.remote" );
    try
    {
      sftpFile =
          fsManager.resolveFile( startPath.concat( "/".concat( theFile.getName() ) ),
                                 opts );
      if ( sftpFile.exists() )
      {
        sftpFile.delete();
      }
    }
    catch ( FileSystemException fse )
    {
      logger.fatal( "error during file delete process" +
                          fse );
      //fse.printStackTrace();
      System.exit( -4 );
    }
  }
}
