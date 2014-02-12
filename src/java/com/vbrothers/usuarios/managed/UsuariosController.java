package com.vbrothers.usuarios.managed;

import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.usuarios.dominio.Groups;
import com.vbrothers.usuarios.dominio.Users;
import com.vbrothers.usuarios.services.GruposServicesLocal;
import com.vbrothers.usuarios.services.UsuariosServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import com.vbrothers.util.SpringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 * @author Jerson Viveros Aguirre
 */

@ManagedBean(name="usuariosController")
@RequestScoped
public class UsuariosController {
    ServiceLocator locator;
    private Users usuario;
    private String busqueda;
    private List<Users> usuarios;
    private List<Groups> grupos;
    private int gridFilasGrupos = 1;
    private int gridColumnasGrupos = 1;
    


    @EJB
    UsuariosServicesLocal usersServices;
    @EJB
    GruposServicesLocal gruposServices;

    @PostConstruct
    public void init(){
        setUsuario(new Users());
        usuario.setPwd("");
        locator = ServiceLocator.getInstance();
        setGrupos(gruposServices.findAllE());
        if(usuarios == null){
            setUsuarios(usersServices.findAll());
        }
        
        if(!grupos.isEmpty()){
                gridColumnasGrupos = (int)Math.sqrt(grupos.size());
                gridFilasGrupos = grupos.size();   
        }
        System.out.println("En el inicio: "+getUsuario().getId());
    }

    public UsuariosController(){
        
    }


    public String guardar(){
        try {
            List<Groups> gruposUsuarios = new ArrayList<Groups>();
            for(Groups grupo:grupos){
                if(grupo.isAddUser()){
                    gruposUsuarios.add(grupo);
                }
                grupo.setAddUser(false);
            }
            usuario.setGrupos(gruposUsuarios);
            usuario.setEstado(1);
            usuario.setPwd(SpringUtils.getPasswordEncoder().encodePassword(usuario.getPwd(), null));
            System.out.println("EAntes de guardar: "+getUsuario().getId());
            usersServices.edit(getUsuario());
            setUsuarios(usersServices.findAll());
            FacesUtil.addMessage(FacesUtil.INFO, "Usuario guardado con exito");
        }catch (LlaveDuplicadaException e){
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el usuario");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }


    public void borrar(Users r){
        try {
            usuarios.remove(r);
            usersServices.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO, "Usuario borrado con exito!!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.INFO, "Error al borrar el usuario");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
    }

    public void actualizar(Users r){
        List<Groups> gruposUsuario = usersServices.findGroupsUser(r);
        for(Groups g : grupos){
            if(gruposUsuario.contains(g)){
              g.setAddUser(true);   
            }else{
              g.setAddUser(false);
            }
        }
        
        this.usuario = r;
    }
    
    public void nuevo(){
        init();
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
     * @return the usuarios
     */
    public List<Users> getUsuarios() {
        return usuarios;
    }

    /**
     * @param usuarios the usuarios to set
     */
    public void setUsuarios(List<Users> usuarios) {
        this.usuarios = usuarios;
    }

    /**
     * @return the busqueda
     */
    public String getBusqueda() {
        return busqueda;
    }

    /**
     * @param busqueda the busqueda to set
     */
    public void setBusqueda(String busqueda) {
        this.busqueda = busqueda;
    }

    /**
     * @return the grupos
     */
    public List<Groups> getGrupos() {
        return grupos;
    }

    /**
     * @param grupos the grupos to set
     */
    public void setGrupos(List<Groups> grupos) {
        this.grupos = grupos;
    }

    /**
     * @return the gridFilasGrupos
     */
    public int getGridFilasGrupos() {
        return gridFilasGrupos;
    }

    /**
     * @param gridFilasGrupos the gridFilasGrupos to set
     */
    public void setGridFilasGrupos(int gridFilasGrupos) {
        this.gridFilasGrupos = gridFilasGrupos;
    }

    /**
     * @return the gridColumnasGrupos
     */
    public int getGridColumnasGrupos() {
        return gridColumnasGrupos;
    }

    /**
     * @param gridColumnasGrupos the gridColumnasGrupos to set
     */
    public void setGridColumnasGrupos(int gridColumnasGrupos) {
        this.gridColumnasGrupos = gridColumnasGrupos;
    }


}
