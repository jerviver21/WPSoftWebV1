/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vbrothers.usuarios.managed;

import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.common.managed.GeneralController;
import com.vbrothers.usuarios.dominio.Menu;
import com.vbrothers.usuarios.dominio.Resource;
import com.vbrothers.usuarios.services.ResourcesServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="recursosController")
@RequestScoped
public class RecursosController {

    private ServiceLocator locator;//Sirve de Cache

    private Resource recurso;
    private List<Resource> recursos;
    private List<SelectItem> menus;
    private List<SelectItem> idiomas
;

    @EJB
    private ResourcesServicesLocal recursoService;

    @PostConstruct
    public void init(){
        setRecurso(new Resource());
        getRecurso().setMenu(new Menu(1l));
        locator = ServiceLocator.getInstance();
        GeneralController sessionController = (GeneralController)FacesUtil.getManagedBean("#{generalController}");
        setRecursos(recursoService.findAll(sessionController.getLocale()));
        setMenus(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_MENU)));
        setIdiomas(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_IDIOMA)));
        
        System.out.println("En el inicio: "+getRecurso().getId());
    }

    public String guardar(){
        try {
            System.out.println("Antes de guardar: "+getRecurso().getId());
            recursoService.edit(getRecurso());
            GeneralController sessionController = (GeneralController)FacesUtil.getManagedBean("#{generalController}");
            setRecursos(recursoService.findAll(sessionController.getLocale()));
            FacesUtil.addMessage(FacesUtil.INFO, "Recurso guardado con exito!!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el recurso");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void borrar(Resource r){
        try {
            recursos.remove(r);
            recursoService.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO,  "Recurso borrado con exito!!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el recurso");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Resource r){
        this.recurso = r;
    }

    
    public void nuevo(){
        init();
    }

    /**
     * @return the recurso
     */
    public Resource getRecurso() {
        return recurso;
    }

    /**
     * @param recurso the recurso to set
     */
    public void setRecurso(Resource recurso) {
        this.recurso = recurso;
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

    /**
     * @return the menus
     */
    public List<SelectItem> getMenus() {
        return menus;
    }

    /**
     * @param menus the menus to set
     */
    public void setMenus(List<SelectItem> menus) {
        this.menus = menus;
    }

    /**
     * @return the lenguajes
     */
    public List<SelectItem> getIdiomas() {
        return idiomas;
    }

    /**
     * @param lenguajes the lenguajes to set
     */
    public void setIdiomas(List<SelectItem> lenguajes) {
        this.idiomas = lenguajes;
    }


}
