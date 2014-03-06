/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.security.spring.dao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * @author Jerson Viveros
 */
public class CustomJdbcDaoImpl extends JdbcDaoImpl implements IChangePassword {

    public CustomJdbcDaoImpl()throws NamingException{
        super();
        InitialContext initCtx = new InitialContext();
        DataSource datasource = (DataSource)(initCtx).lookup("java:/jboss/datasources/PostgreSQLDS3"); 
        setDataSource(datasource);
        
    }

    @Override
    public void changePassword(String username, String password) {
        getJdbcTemplate().update("UPDATE users SET pwd = ? WHERE usr = ? ",password, username);
    }
}
