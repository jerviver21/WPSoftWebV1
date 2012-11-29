/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.common.managed;

import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="generalController")
@SessionScoped
public class GeneralController {
    private String locale = "es";
    public void changeLanguage(ActionEvent event){
        locale = (String) event.getComponent().getAttributes().get("language");
        System.out.println("--> "+locale);
        SessionController sessionController = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        sessionController.restartModel();
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getExternalContext().getSessionMap().remove("recursosController");
        fc.getExternalContext().getSessionMap().remove("menusController");
        fc.getExternalContext().getSessionMap().remove("rolesController");

    }

    /**
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }
}
