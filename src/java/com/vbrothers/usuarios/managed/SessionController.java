package com.vbrothers.usuarios.managed;

import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.usuarios.dominio.Groups;
import com.vbrothers.usuarios.dominio.Users;
import com.vbrothers.usuarios.services.UsuariosServicesLocal;
import com.vbrothers.util.FacesUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.primefaces.model.MenuModel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Jerson Viveros Aguirre
 */
@ManagedBean(name="sessionController")
@SessionScoped
public class SessionController {
    private MenuModel model;
    private Users usuario;
    @EJB
    private UsuariosServicesLocal usersServices;

    private UsernamePasswordAuthenticationToken principal;
    private Collection<GrantedAuthority> grantedAuthorities;


    
    @PostConstruct
    public void init(){
        System.out.println("Iniciando session");
        principal = (UsernamePasswordAuthenticationToken)FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();

        grantedAuthorities = principal.getAuthorities();
        Set roles = new HashSet<String>();
        for(GrantedAuthority autoridad: grantedAuthorities){
            roles.add(autoridad.getAuthority());
        }
        String nombreUsuario = principal.getName();
        setUsuario(usersServices.findFullUser(nombreUsuario));
        Set grupos = new HashSet<String>();
        for(Groups g : usuario.getGrupos()){
            grupos.add(g.getCodigo());
        }
        usuario.setRolesUsr(roles);
        usuario.setGruposUsr(grupos);
        restartModel();
    }

    public void restartModel(){
        setModel(FacesUtil.getMenu(getUsuario().getRecursos()));
    }
    

    /**
     * @return the model
     */
    public MenuModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(MenuModel model) {
        this.model = model;
    }

    /**
     * @return the usuario
     */
    public Users getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Users usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the principal
     */
    public UsernamePasswordAuthenticationToken getPrincipal() {
        return principal;
    }

    /**
     * @param principal the principal to set
     */
    public void setPrincipal(UsernamePasswordAuthenticationToken principal) {
        this.principal = principal;
    }

    /**
     * @return the grantedAuthorities
     */
    public Collection<GrantedAuthority> getGrantedAuthorities() {
        return grantedAuthorities;
    }

    /**
     * @param grantedAuthorities the grantedAuthorities to set
     */
    public void setGrantedAuthorities(Collection<GrantedAuthority> grantedAuthorities) {
        this.grantedAuthorities = grantedAuthorities;
    }

    
    
}
