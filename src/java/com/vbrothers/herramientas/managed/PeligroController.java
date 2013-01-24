/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.herramientas.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.permisostrabajo.dominio.Control;
import com.vbrothers.permisostrabajo.dominio.Peligro;
import com.vbrothers.herramientas.services.ControlesServicesLocal;
import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="riesgoController")
@SessionScoped
public class PeligroController {
    ServiceLocator locator;
    
    private Peligro item;
    private Control control;
    private List<Peligro> items;


    @EJB
    private PeligrosServicesLocal peligroService;
    @EJB
    private ControlesServicesLocal controlService;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        crearNuevo();
    }

    public void crearNuevo(){
        item = new Peligro();
        item.setControles(new ArrayList<Control>());
        control = new Control();
        setItems(peligroService.findAll());
    }

    public String guardar(){
        try {
            //System.out.println("--> "+getItem().getId());
            if(getItem().getId() == 0){
                getItem().setId(null);
            }
            peligroService.edit(getItem());
            setItems(peligroService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO, "Peligro guardado con exito!!");
            crearNuevo();
            locator.restartCache();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Riesgo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    

    public void borrar(Peligro r){
        try {
            peligroService.remove(r);
            setItems(peligroService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO,  "Riesgo borrado con exito!!");
            locator.restartCache();
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "No se puede borrar el peligro, debe estarse usando en otra parte del proceso");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(Peligro r ){
        this.setItem(r);
    }


    public void addControl(){
        control.setPeligro(item);
        control.setConsecutivo(item.getControles().size()+1);
        item.getControles().add(control);
        control = new Control();
    }

    public void removeControl(Control r ){
       for(int i = 0 ; i < item.getControles().size();i++){
            Control equ = item.getControles().get(i);
            if(equ.getNombre().equals(r.getNombre())){
                item.getControles().remove(i);
                i--;
            }
            equ.setConsecutivo(i+1);
        }
        controlService.remove(r);
        locator.restartCache();
    }

    /**
     * @return the item
     */
    public Peligro getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Peligro item) {
        this.item = item;
    }

    /**
     * @return the items
     */
    public List<Peligro> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<Peligro> items) {
        this.items = items;
    }

    /**
     * @return the control
     */
    public Control getControl() {
        return control;
    }

    /**
     * @param control the control to set
     */
    public void setControl(Control control) {
        this.control = control;
    }
}
