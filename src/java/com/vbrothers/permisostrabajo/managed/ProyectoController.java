
package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.ContratistasProyecto;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.dominio.EmpleadosProyecto;
import com.vbrothers.permisostrabajo.dominio.Proyecto;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.permisostrabajo.services.ProyectoServicesLocal;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros Aguirre
 */

@ManagedBean(name="proyectoController")
@SessionScoped
public class ProyectoController {
    @EJB
    ProyectoServicesLocal proyectoService;
    @EJB
    ContratistaServicesLocal contratistaService;
    @EJB
    EmpleadoServicesLocal empleadoService;
   

    private Long idContratista;
    private List<SelectItem> contratistas;
    private Long idEmpleado;
    private List<SelectItem> empleados;

    private Proyecto proyecto;
    private List<Proyecto> proyectos;
    ServiceLocator locator;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        proyecto = new Proyecto(null);
        proyecto.setContratistas(new ArrayList<ContratistasProyecto>());
        proyecto.setEmpleados(new ArrayList<EmpleadosProyecto>());
        setProyectos(proyectoService.findAll());
        contratistas = FacesUtil.getSelectsItem(contratistaService.findContratistasActivos());
        empleados = FacesUtil.getSelectsItem(empleadoService.findEmpleadosActivosPlanta());
        
    }

    public String createProyecto(){
        try {
            proyectoService.edit(proyecto);
            FacesUtil.addMessage(FacesUtil.INFO, "Proyecto guardado con exito!");
            init();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }
    
    public void borrar(ActionEvent event){
        try {
            Proyecto r  = (Proyecto) event.getComponent().getAttributes().get("proyectoCambiar");
            proyectoService.remove(r);
            proyectos.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO, "Proyecto borrado con exito!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
    }

    public void actualizar(ActionEvent event){
        Proyecto r  = (Proyecto) event.getComponent().getAttributes().get("proyectoCambiar");
        this.proyecto = r;
    }
    
    public void agregarContratista(ActionEvent event){
        if(getIdContratista() == null || getIdContratista() == -1){
            return;
        }
        Contratista contratista = contratistaService.find(getIdContratista());
        ContratistasProyecto cp = new ContratistasProyecto();
        cp.setContratista(contratista);
        proyecto.getContratistas().add(cp);
    }

    public void agregarEmpleado(ActionEvent event){
        if(getIdEmpleado() == null || getIdEmpleado() == -1){
            return;
        }
        Empleado empleado = empleadoService.find(getIdEmpleado());
        EmpleadosProyecto ep = new EmpleadosProyecto();
        ep.setEmpleado(empleado);
        proyecto.getEmpleados().add(ep);
    }
    
    public void borrarContratista(ActionEvent event){
        ContratistasProyecto r  = (ContratistasProyecto) event.getComponent().getAttributes().get("itemCambiar");
        proyecto.getContratistas().remove(r);
    }

    public void borrarEmpleado(ActionEvent event){
        EmpleadosProyecto r  = (EmpleadosProyecto) event.getComponent().getAttributes().get("itemCambiar");
        proyecto.getEmpleados().remove(r);
    }

    public String navProyecto(){
        return "/permisostrabajo/proyecto.xhtml";
    }

    /**
     * @return the proyecto
     */
    public Proyecto getProyecto() {
        return proyecto;
    }

    /**
     * @param proyecto the proyecto to set
     */
    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }

    /**
     * @return the proyectos
     */
    public List<Proyecto> getProyectos() {
        return proyectos;
    }

    /**
     * @param proyectos the proyectos to set
     */
    public void setProyectos(List<Proyecto> proyectos) {
        this.proyectos = proyectos;
    }

    /**
     * @return the contratistas
     */
    public List<SelectItem> getContratistas() {
        return contratistas;
    }

    /**
     * @param contratistas the contratistas to set
     */
    public void setContratistas(List<SelectItem> contratistas) {
        this.contratistas = contratistas;
    }

    /**
     * @return the idContratista
     */
    public Long getIdContratista() {
        return idContratista;
    }

    /**
     * @param idContratista the idContratista to set
     */
    public void setIdContratista(Long idContratista) {
        this.idContratista = idContratista;
    }

    
    /**
     * @return the idEmpleado
     */
    public Long getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * @param idEmpleado the idEmpleado to set
     */
    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    /**
     * @return the empleados
     */
    public List<SelectItem> getEmpleados() {
        return empleados;
    }

    /**
     * @param empleados the empleados to set
     */
    public void setEmpleados(List<SelectItem> empleados) {
        this.empleados = empleados;
    }



}
