package com.vbrothers.security.spring.managed;

import com.vbrothers.common.managed.GeneralController;
import com.vbrothers.util.FacesUtil;
import java.io.IOException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="loginController")
@RequestScoped
//public class LoginController implements PhaseListener {
public class LoginController{
    private String mensaje;
    public LoginController(){
        Object ex = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get(WebAttributes.AUTHENTICATION_EXCEPTION);
        if(ex != null){
            if(ex instanceof BadCredentialsException){
                GeneralController generalControler = (GeneralController)FacesUtil.getManagedBean("#{generalController}");
                BadCredentialsException bce = (BadCredentialsException)ex;
                //Queda pendiente la internacionalización del spring security, porque es un recamello, 
                //mensaje = bde.getMessage();
                if(generalControler.getLocale().equals("es")){
                   mensaje="Usuario o Clave invalidos";
                }else if(generalControler.getLocale().equals("pt")){
                   mensaje="Usuario o Sehna invalido";
                }else{
                   mensaje = "Bad Credentials";
                }
            }if(ex instanceof DisabledException){
                mensaje =  "El usuario se encuentra inactivo";
            }else{
                Exception exc = (Exception) ex;
                mensaje =  "Error al tratar de autenticar "+ex.toString();
                exc.printStackTrace();
            }
        }
    }

    public String doLogin() throws ServletException, IOException{
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        ServletRequest request = (ServletRequest)context.getRequest();
        RequestDispatcher dispatcher = request.getRequestDispatcher("/j_spring_security_check");
        dispatcher.forward((ServletRequest) context.getRequest(),
				(ServletResponse) context.getResponse());
        FacesContext.getCurrentInstance().responseComplete();
        return null;
    }


    /**
     * @return the mensaje
     */
    public String getMensaje() {
        return mensaje;
    }

    /**
     * @param mensaje the mensaje to set
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

}
