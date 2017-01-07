/*
    Mango - Open Source M2M - http://mango.serotoninsoftware.com
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc.
    @author Matthew Lohbihler
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.serotonin.mango.db;

import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.swing.plaf.basic.BasicLabelUI;

import org.apache.commons.dbcp.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.springframework.dao.DataAccessException;
import org.test.MyDAO;

import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.mango.Common;

public class MySQLAccess extends BasePooledAccess {
    public MySQLAccess(ServletContext ctx) {
        super(ctx);
    }

    @Override
    protected void initializeImpl(String propertyPrefix) {
        super.initializeImpl(propertyPrefix);
        ((BasicDataSource) dataSource).setInitialSize(3);
        ((BasicDataSource) dataSource).setMaxWait(-1);
        ((BasicDataSource) dataSource).setTestWhileIdle(true);
        ((BasicDataSource) dataSource).setTimeBetweenEvictionRunsMillis(10000);
        ((BasicDataSource) dataSource).setMinEvictableIdleTimeMillis(60000);
    }

    @Override
    protected void initializeImpl(String propertyPrefix, String dataSourceName)
    {
        super.initializeImpl(propertyPrefix, dataSourceName);
    }

    @Override
    protected String getUrl(String propertyPrefix) {
        String url = super.getUrl(propertyPrefix);
        if (url.indexOf('?') > 0)
            url += "&";
        else
            url += "?";
        url += "useUnicode=yes&characterEncoding=" + Common.UTF8;
        return url;
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.MYSQL;
    }

    @Override
    protected String getDriverClassName() {
        return "com.mysql.jdbc.Driver";
    }

    @Override
    protected boolean newDatabaseCheck(ExtendedJdbcTemplate ejt) {
    	
    	boolean shemaExist = true;
    	boolean baseLineNotExist = false;
    	
    	
        try {
            ejt.execute("select count(*) from users");
        } catch (DataAccessException e) {
            shemaExist = false;
        }
        
        try {
           ejt.execute("select count(*) from schema_version");
        } catch (DataAccessException e) {
        	baseLineNotExist = true;
        }
        
        
        Flyway flyway = new Flyway();
		flyway.setLocations("org.scada_lts.dao.migration.mysql");
		flyway.setDataSource(getDataSource());
		
        if (shemaExist) {
        	if (baseLineNotExist) {
        		flyway.setBaselineOnMigrate(true);
         		flyway.baseline();
        	}
        }
        
        flyway.migrate();
           
        return false;
    }

    @Override
    public double applyBounds(double value) {
        if (Double.isNaN(value))
            return 0;
        if (value == Double.POSITIVE_INFINITY)
            return Double.MAX_VALUE;
        if (value == Double.NEGATIVE_INFINITY)
            return -Double.MAX_VALUE;

        return value;
    }

    @Override
    public void executeCompress(ExtendedJdbcTemplate ejt) {
        // no op
    }
}
