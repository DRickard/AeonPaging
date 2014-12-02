package edu.ucla.library.libservices.aeon.callslip.db.source;

import java.util.Properties;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataSourceFactory
{
  public static DriverManagerDataSource createVgerSource(Properties props)
  {
    DriverManagerDataSource ds;

    ds = new DriverManagerDataSource();
    ds.setDriverClassName( props.getProperty( "db.driver" ) );
    ds.setUrl( props.getProperty( "db.url" ) );
    ds.setUsername( props.getProperty( "db.user" ) );
    ds.setPassword( props.getProperty( "db.password" ) );

    return ds;
  }
}
