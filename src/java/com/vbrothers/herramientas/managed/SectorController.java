/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.herramientas.managed;

import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vbrothers.herramientas.services.EquiposServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Equipo;
import com.vbrothers.permisostrabajo.dominio.Sector;
import com.vi.usuarios.services.RolesServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="sectorController")
@SessionScoped
public class SectorController {
    ServiceLocator locator;

    private Sector item;
    private Equipo equipo;
    private List<Sector> items;
    private List<SelectItem> grupos;


    @EJB
    private SectoresServicesLocal sectorService;
    @EJB
    private EquiposServicesLocal equipoService;
    @EJB
    private RolesServicesLocal rolService;

    @PostConstruct
    public void init(){
        crearNuevo();
        setGrupos(FacesUtil.getSelectsItem(rolService.findGruposByRol(locator.getParameter("rolAutArea")),"getCodigo" ,"getCodigo" ));
    }

    public void crearNuevo(){
        locator = ServiceLocator.getInstance();
        item = new Sector();
        item.setEquipos(new ArrayList<Equipo>());
        equipo = new Equipo();
        setItems(sectorService.findAll());
    }

    
    

    public String guardar(){
        try {
            if(getItem().getId() == 0){
                getItem().setId(null);
            }
            sectorService.edit(getItem());
            setItems(sectorService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO, "Sector guardado con exito!!");
            crearNuevo();
            locator.restartCache();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Area de trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void borrar(Sector r){
        try {
            sectorService.remove(r);
            setItems(sectorService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO,  "Recurso borrado con exito!!");
            locator.restartCache();
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "No se puede borrar el sector, debe estarse usando en otra parte del proceso");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Sector r){
        this.setItem(r);
    }

    public void addEquipo(){
        equipo.setArea(item);
        item.getEquipos().add(equipo);
        equipo = new Equipo();
    }

    public void removeEquipo(Equipo r){
        for(int i = 0 ; i < item.getEquipos().size();i++){
            Equipo equ = item.getEquipos().get(i);
            if(equ.getNombre().equals(r.getNombre())){
                item.getEquipos().remove(i);
                break;
            }
        }
        equipoService.remove(r);
        locator.restartCache();
    }

    /**
     * @return the item
     */
    public Sector getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Sector item) {
        this.item = item;
    }

    /**
     * @return the items
     */
    public List<Sector> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Sector> items) {
        this.items = items;
    }

    /**
     * @return the equipo
     */
    public Equipo getEquipo() {
        return equipo;
    }

    /**
     * @param equipo the equipo to set
     */
    public void setEquipo(Equipo equipo) {
        this.equipo = equipo;
    }

    /**
     * @return the grupos
     */
    public List<SelectItem> getGrupos() {
        return grupos;
    }

    /**
     * @param grupos the grupos to set
     */
    public void setGrupos(List<SelectItem> grupos) {
        this.grupos = grupos;
    }



}
