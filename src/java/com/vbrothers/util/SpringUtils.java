/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.util;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @author Jerson Viveros
 */
public class SpringUtils {

    public static PasswordEncoder getPasswordEncoder(){
        return (PasswordEncoder)getSpringContext().getBean("passwordEncoder");
    }

    public static WebApplicationContext getSpringContext(){
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext());
        return springContext;
    }

}
