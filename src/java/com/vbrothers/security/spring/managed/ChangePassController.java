
package com.vbrothers.security.spring.managed;

import com.vbrothers.security.spring.dao.IChangePassword;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.SpringUtils;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="changePassController")
@RequestScoped
public class ChangePassController {
    private String confirmPass;
    private String newPass;
    
    @ManagedProperty(value="#{userService}")
    private IChangePassword changePasswordDao;

    public String changePassword(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Clave cambiada con exito", "");
        SessionController sessionControler = (SessionController)FacesUtil.getManagedBean("#{sessionController}");

        if(!confirmPass.equals(newPass)){
            mensaje.setSeverity(FacesMessage.SEVERITY_ERROR);
            mensaje.setSummary("Las claves deben ser iguales!!");
            facesContext.addMessage(null, mensaje);
            return null;
        }

        String username = sessionControler.getPrincipal().getName();
        getChangePasswordDao().changePassword(username, SpringUtils.getPasswordEncoder().encodePassword(newPass, null));
        SecurityContextHolder.clearContext();
        facesContext.addMessage(null, mensaje);
        return null;
    }



    /**
     * @return the oldPass
     */
    public String getConfirmPass() {
        return confirmPass;
    }

    /**
     * @param oldPass the oldPass to set
     */
    public void setConfirmPass(String oldPass) {
        this.confirmPass = oldPass;
    }

    /**
     * @return the newPass
     */
    public String getNewPass() {
        return newPass;
    }

    /**
     * @param newPass the newPass to set
     */
    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    /**
     * @return the changePasswordDao
     */
    public IChangePassword getChangePasswordDao() {
        return changePasswordDao;
    }

    /**
     * @param changePasswordDao the changePasswordDao to set
     */
    public void setChangePasswordDao(IChangePassword changePasswordDao) {
        this.changePasswordDao = changePasswordDao;
    }

}
