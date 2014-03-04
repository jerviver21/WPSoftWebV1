package com.vbrothers.usuarios.managed;

import com.vbrothers.common.managed.GeneralController;
import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vi.usuarios.dominio.Resource;
import com.vi.usuarios.dominio.Rol;
import com.vi.usuarios.services.ResourcesServicesLocal;
import com.vi.usuarios.services.RolesServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * @author Jerson Viveros
 * Todos los derechos reservados!!
 */
@ManagedBean(name="rolesController")
@RequestScoped
public class RolesController {
    private Rol rol;
    private List<Rol> roles;
    private List<Resource> recursos;
    private int gridFilasRec = 1;
    private int gridColumnasRec = 1;

    @EJB
    private RolesServicesLocal rolService;
    @EJB
    private ResourcesServicesLocal resourceService;


    @PostConstruct
    public void init(){
        rol = new Rol();
        setRoles(rolService.findAll());
        GeneralController generalController = (GeneralController)FacesUtil.getManagedBean("#{generalController}");
        setRecursos(resourceService.findAll(generalController.getLocale()));
        if(!getRecursos().isEmpty()){
            setGridColumnasRec((int) Math.sqrt(getRecursos().size()));
            setGridFilasRec(recursos.size());
        }
    }
    
    public void nuevo(){
        init();
    }

    public RolesController() {
    }

    /**
     * @return the rol
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * @param rol the rol to set
     */
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * @return the roles
     */
    public List<Rol> getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }


    public String guardar(){
        try {
            Set<Resource> recursosRol = new HashSet<Resource>();
            for(Resource recurso:getRecursos()){
                if(recurso.isAddRol()){
                    recursosRol.add(recurso);
                }
                recurso.setAddRol(false);
            }
            rol.setRecursos(recursosRol);
            rolService.edit(getRol());
            FacesUtil.addMessage(FacesUtil.INFO,"Rol guardado con exito!!");
            init();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR,e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR,"Error al guardar el Rol");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void borrar(Rol r){
        try {
            roles.remove(r);
            rolService.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO,"Rol borrado con exito!!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR,"Error al borrar el Rol");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Rol r){
        Set<Resource> recursosRol = rolService.findResourceByRol(r);
        for(Resource g : getRecursos()){
            if(recursosRol.contains(g)){
              g.setAddRol(true);
            }else{
              g.setAddRol(false);
            }
        }
        this.rol = r;
    }


    /**
     * @return the gridFilasRec
     */
    public int getGridFilasRec() {
        return gridFilasRec;
    }

    /**
     * @param gridFilasRec the gridFilasRec to set
     */
    public void setGridFilasRec(int gridFilasRec) {
        this.gridFilasRec = gridFilasRec;
    }

    /**
     * @return the gridColumnasRec
     */
    public int getGridColumnasRec() {
        return gridColumnasRec;
    }

    /**
     * @param gridColumnasRec the gridColumnasRec to set
     */
    public void setGridColumnasRec(int gridColumnasRec) {
        this.gridColumnasRec = gridColumnasRec;
    }

    /**
     * @return the recursos
     */
    public List<Resource> getRecursos() {
        return recursos;
    }

    /**
     * @param recursos the recursos to set
     */
    public void setRecursos(List<Resource> recursos) {
        this.recursos = recursos;
    }
    
}
