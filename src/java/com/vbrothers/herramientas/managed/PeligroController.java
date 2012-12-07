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
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.ArrayList;
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

@ManagedBean(name="riesgoController")
@SessionScoped
public class PeligroController {

    private Peligro item;
    private Control control;
    private List<Peligro> items;


    @EJB
    private PeligrosServicesLocal peligroService;
    @EJB
    private ControlesServicesLocal controlService;

    @PostConstruct
    public void init(){
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
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Riesgo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    

    public void borrar(ActionEvent event){
        try {
            Peligro r  = (Peligro) event.getComponent().getAttributes().get("itemCambiar");
            peligroService.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO,  "Riesgo borrado con exito!!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el riesgo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void actualizar(ActionEvent event){
        Peligro r  = (Peligro) event.getComponent().getAttributes().get("itemCambiar");
        this.setItem(r);
    }


    public void addControl(ActionEvent event){
        control.setPeligro(item);
        control.setConsecutivo(item.getControles().size()+1);
        item.getControles().add(control);
        control = new Control();
    }

    public void removeControl(ActionEvent event){
        Control r  = (Control) event.getComponent().getAttributes().get("itemBorrar");
        item.getControles().remove(r);
        controlService.remove(r);
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
