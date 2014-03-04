/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.herramientas.managed;

import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Disciplina;
import com.vbrothers.herramientas.services.DisciplinasServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="disciplinaController")
@SessionScoped
public class DisciplinaController {
    private ServiceLocator locator;//Sirve de Cache

    private Disciplina item;
    private List<Disciplina> items;


    @EJB
    private DisciplinasServicesLocal disciplinaService;

    @PostConstruct
    public void init(){
        setItem(new Disciplina());
        locator = ServiceLocator.getInstance();
        setItems(disciplinaService.findAll());
    }

    public String guardar(){
        try {
            //System.out.println("--> "+getItem().getId());
            if(getItem().getId() == 0){
                getItem().setId(null);
            }
            disciplinaService.edit(getItem());
            setItems(disciplinaService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO, "Disciplina guardado con exito!!");
            locator.restartCache();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Disciplina");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void crearNuevo(){
        setItem(new Disciplina());
    }

    public void borrar(Disciplina r){
        try {
            disciplinaService.remove(r);
            setItems(disciplinaService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO,  "Recurso borrado con exito!!");
            locator.restartCache();
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar la disciplina, debe estarse usando en otra parte del proceso");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Disciplina r){
        this.setItem(r);
    }

    /**
     * @return the item
     */
    public Disciplina getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Disciplina item) {
        this.item = item;
    }

    /**
     * @return the items
     */
    public List<Disciplina> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Disciplina> items) {
        this.items = items;
    }
}
