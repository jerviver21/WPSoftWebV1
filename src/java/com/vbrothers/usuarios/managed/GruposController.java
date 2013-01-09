package com.vbrothers.usuarios.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.usuarios.dominio.Groups;
import com.vbrothers.usuarios.dominio.Rol;
import com.vbrothers.usuarios.services.GruposServicesLocal;
import com.vbrothers.usuarios.services.RolesServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="gruposController")
@RequestScoped
public class GruposController {
    private Groups grupo;
    private List<Groups> grupos;
    private List<Rol> roles;
    private int gridFilasRoles = 1;
    private int gridColumnasRoles = 1;

    @EJB
    private GruposServicesLocal groupsService;
    @EJB
    RolesServicesLocal rolesServices;


    @PostConstruct
    public void init(){
        grupo = new Groups();
        setGrupos(groupsService.findAll());
        setRoles(rolesServices.findAll());
        if(!roles.isEmpty()){
            setGridColumnasRoles((int) Math.sqrt(roles.size()));
            setGridFilasRoles(roles.size());    
        }
    }
    
    public void nuevo(){
        init();
    }

    public GruposController() {
    }

    /**
     * @return the Groups
     */
    public Groups getGrupo() {
        return grupo;
    }

    /**
     * @param Groups the Groups to set
     */
    public void setGrupo(Groups Groups) {
        this.grupo = Groups;
    }

    /**
     * @return the Groupses
     */
    public List<Groups> getGrupos() {
        return grupos;
    }

    /**
     * @param Groupses the Groupses to set
     */
    public void setGrupos(List<Groups> Groupses) {
        this.grupos = Groupses;
    }


    public String guardar(){
        try {
            List<Rol> rolesGrupo = new ArrayList<Rol>();
            for(Rol rol:roles){
                if(rol.isAddGroup()){
                    rolesGrupo.add(rol);
                }
                rol.setAddGroup(false);
            }
            grupo.setRoles(rolesGrupo);
            groupsService.edit(grupo);
            FacesUtil.addMessage(FacesUtil.INFO,"Grupo guardado con exito!!");
            init();
        }catch (LlaveDuplicadaException e){
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR,"Error al guardar el grupo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void borrar(Groups r){
        try {
            grupos.remove(r);
            groupsService.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO,"Grupo borrado con exito!!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR,"Error al borrar el grupo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Groups r){
        List<Rol> rolesGrupo = groupsService.findRolesByGroup(r);
        for(Rol rol : roles){
            if(rolesGrupo.contains(rol)){
              rol.setAddGroup(true);
            }else{
              rol.setAddGroup(false);
            }
        }
        this.grupo = r;
        if(grupo == null){
            grupo = new Groups();
        }
    }

    public String recargar(){
        return null;
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

    /**
     * @return the gridFilasRoles
     */
    public int getGridFilasRoles() {
        return gridFilasRoles;
    }

    /**
     * @param gridFilasRoles the gridFilasRoles to set
     */
    public void setGridFilasRoles(int gridFilasRoles) {
        this.gridFilasRoles = gridFilasRoles;
    }

    /**
     * @return the gridColumnasRoles
     */
    public int getGridColumnasRoles() {
        return gridColumnasRoles;
    }

    /**
     * @param gridColumnasRoles the gridColumnasRoles to set
     */
    public void setGridColumnasRoles(int gridColumnasRoles) {
        this.gridColumnasRoles = gridColumnasRoles;
    }
}
