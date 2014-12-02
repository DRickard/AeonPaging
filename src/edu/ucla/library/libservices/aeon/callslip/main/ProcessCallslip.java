package edu.ucla.library.libservices.aeon.callslip.main;

import edu.ucla.library.libservices.aeon.callslip.beans.Item;
import edu.ucla.library.libservices.aeon.callslip.beans.Patron;

import edu.ucla.library.libservices.aeon.callslip.beans.Request;
import edu.ucla.library.libservices.aeon.callslip.db.mappers.ItemMapper;
import edu.ucla.library.libservices.aeon.callslip.db.mappers.PatronMapper;
import edu.ucla.library.libservices.aeon.callslip.db.source.DataSourceFactory;

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

import org.springframework.jdbc.core.JdbcTemplate;

public class ProcessCallslip
{
  private static Properties props;
  private static File keyFile;
  private static FileObject src = null;
  private static FileSystemManager fsManager = null;
  private static FileSystemOptions opts = null;
  private static Patron yrl = null; //barcode = 28285
  private static Patron bio = null; //barcode = 21158000008590
  private static DataSource ds;

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

    //check for files in paging directory
    if ( download( props.getProperty( "sftp.directory.remote" ) ) )
    {
      File localDir;
      File[] files;

      //get db connection
      makeDbConnection();

      //build YRL & BIO users
      buildPatrons();

      localDir = new File( props.getProperty( "sftp.directory.local" ) );
      files = localDir.listFiles();
      
      for ( File theFile : files )
      {
        Item theItem;
        Request thePull;
        SendRequestClient theClient;
        UploadWriter writer;
        
        thePull = getReqFromFile(theFile);
        theItem = getItem(thePull.getBarcode());
        writer = setupWriter(thePull, theItem);
        theClient = new SendRequestClient();
        theClient.setRequetURL( props.getProperty( "voyager.url" ) );
        theClient.setWriter( writer );
        theClient.postCallSlip();
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
      System.out.println( "problem with props file: " + ioe.getMessage() );
      System.exit( -1 );
    }
  }

  private static void getSftpConnect()
  {
    try
    {
      opts = new FileSystemOptions();
      SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking( opts,
                                                                          "no" );
      keyFile = new File( props.getProperty( "keyfile.path" ) );
      SftpFileSystemConfigBuilder.getInstance().setIdentities( opts,
                                                               new File[]
          { keyFile } );
      fsManager = VFS.getManager();
    }
    catch ( FileSystemException fse )
    {
      System.out.println( "problem with sftp connect: " +
                          fse.getMessage() );
      System.exit( -2 );
    }
  }

  private static boolean download( String directory )
  {
    FileObject[] children;
    FileObject sftpFile;
    String startPath;

    startPath = "sftp://" + props.getProperty( "sftp.host" ) + directory;
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
      System.err.println( "error during download process: " +
                          fse.getMessage() );
      System.exit( -3 );
    }
    return true;
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
    bean =
        ( Item ) new JdbcTemplate( ds ).queryForObject( ITEM_QUERY, new Object[]
          { barcode }, new ItemMapper() );
    return bean;
  }

  private static UploadWriter setupWriter( Request thePull, Item theItem )
  {
    UploadWriter local;
    local = new UploadWriter();
    
    local.setComment( thePull.getNote() );
    local.setDbID( props.getProperty( "voyager.dbkey" ) );
    if ( thePull.getLibrary().equalsIgnoreCase( "YRL" ) )
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
}
/*
 * props file will have SFTP host URL, SFTP user, path-to-key-file, 
 * SFTP client to download XML files
 * need to process XML to get item barcode, reading room, note
 * retrieve location ID, Voyager key, patron barcode from props file
 * lookup bibId, copy, itemId, mfhdId from item barcode
 * build request XML
 * need WS client to POST request to Voyager
 * 
 * load props
 * connect to sftp
 * check for files in srlf directory
 * for each file do:
 *    read file
 *    retrieve values per sql below
 *    build request xml
 *    POST request to Tomcat
 * done
 * 
select
  i.item_id,
  i.copy_number,
  bi.bib_id,
  mi.mfhd_id
from
  ucladb.item_barcode ib
  inner join ucladb.item i ON ib.item_id = i.item_id
  inner join ucladb.mfhd_item mi ON ib.item_id = mi.item_id
  inner join ucladb.bib_item bi ON ib.item_id = bi.item_id
where 
  item_barcode = ?

String uri = "sftp://username:password@hostname:22";

FileObject from = fsManager.resolveFile(uri, options);
FileObject to = fsManager.resolveFile("/tmp", options);
to.copyFrom(from, new AllFileSelector());
 */