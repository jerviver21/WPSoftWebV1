package com.vbrothers.permisostrabajo.managed;


import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.dominio.Equipo;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.Proyecto;
import com.vbrothers.permisostrabajo.dominio.Sector;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.permisostrabajo.services.PermisoTrabajoServicesLocal;
import com.vbrothers.permisostrabajo.services.ProyectoServicesLocal;
import com.vbrothers.permisostrabajo.to.PermisoTrabajoTO;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

/**
 *
 * @author Jerson Viveros
 */

@ManagedBean(name="permisoController")
@SessionScoped
public class PermisoController {
    ServiceLocator locator;
    @EJB
    PermisoTrabajoServicesLocal permisoTrabajoService;
    @EJB
    EmpleadoServicesLocal empleadoServices;  
    @EJB
    ProyectoServicesLocal proyectoServices;
    @EJB
    ContratistaServicesLocal contratistaServices;
    
    
    
    
    SessionController sesion;
    
    /*Permite la creación y consulta de permisos de trabajo*/
    private PermisoTrabajoTO trabajo;
    private List<SelectItem> proyectos;
    private List<SelectItem> equipos;
    private List<SelectItem> sectores;
    private List<SelectItem> contratistas;
    private List<SelectItem> empleados;
    
    private List<PermisoTrabajo> permisosProyecto;
    
    //Parametros de definicion del trabajo
    private final int PROYECTO = 0;
    private final int ORDEN_TRABAJO = 1;
    private final int CORRECTIVO_SIN_ORDEN = 2;
    private int tipoTrabajo = PROYECTO;
    //parametros de definición de tipo de ejecutante
    private final int CONTRATISTA = 0;
    private final int EMPLEADO = 1;
    private int tipoEjecutante = EMPLEADO;
    
    /*Permite el listamiento de todos los trabajos asignados al usuario*/
    private List<PermisoTrabajo> trabajos;

    //Permite seleccionar varios empleados
    private long idEmpleado;

    //Este mapa permite la selección del subcombo equipos, que depende del sector.
    private Map equiposXgrupo;
 

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        setTrabajos(permisoTrabajoService.findAll());
        setProyectos(FacesUtil.getSelectsItem(proyectoServices.findProyectosActivos()));
        setSectores(FacesUtil.getSelectsItem(locator.getReferenceTable(ServiceLocator.SECTORES_X_ID)));
        equiposXgrupo = locator.getReferenceTable(ServiceLocator.EQUIPOS_X_SECTOR_ID);
        setEquipos(FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sectores.get(0).getValue())));
        setEmpleados(FacesUtil.getSelectsItem(empleadoServices.findEmpleadosActivosXContratita(null)));
        setContratistas(FacesUtil.getSelectsItem(contratistaServices.findActivos()));
        crearNuevo();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        permisosProyecto = new ArrayList<PermisoTrabajo>();
    }
    
    //Métodos para el listado de permisos de trabajo
    public String crearNuevo(){
        setTrabajo(new PermisoTrabajoTO());
        trabajo.setPermiso(new PermisoTrabajo());
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        trabajo.setUsr(sesion.getUsuario());
        return "/permisostrabajo/permiso.xhtml";
    }

    public void findPermisoTrabajo(ActionEvent event){
        trabajo.setPermiso((PermisoTrabajo) event.getComponent().getAttributes().get("itemCambiar"));
        trabajo = permisoTrabajoService.findPermisoTrabajo(trabajo.getPermiso().getId());
        if(trabajo.getPermiso().getProyecto() != null){
            tipoTrabajo = PROYECTO;
        }else if(trabajo.getPermiso().getNumOrden() != null){
            tipoTrabajo = ORDEN_TRABAJO;
        }else{
            tipoTrabajo = CORRECTIVO_SIN_ORDEN;
        }
        if(trabajo.getPermiso().isEjecutorContratista()){
            tipoEjecutante = CONTRATISTA;
        }
    }

    public String navPermisoTrabajo(){
        return "/permisostrabajo/permiso.xhtml";
    }
    
    public void borrarPermiso(ActionEvent event){
        try {
            PermisoTrabajo r  = (PermisoTrabajo) event.getComponent().getAttributes().get("itemCambiar");
            permisoTrabajoService.remove(r);
            trabajos.remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar la tarea del permiso de trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
    }
    
    public String guardarPermisoTrabajo(){
        try {
            trabajo.getPermiso().setEjecutorContratista(tipoEjecutante == CONTRATISTA ? true : false);
            trabajo.getPermiso().setUsuarioCreacion(sesion.getUsuario().getUsr());
            permisoTrabajoService.crearPermiso(trabajo);
            setTrabajos(permisoTrabajoService.findAll());
            FacesUtil.addMessage(FacesUtil.INFO, "El permiso de trabajo fue guardado!");  
        } catch (ParseException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El formato de hora es incorrecto, debe ser  HH:mm");
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        if(tipoTrabajo == PROYECTO){
            permisosProyecto.add(trabajo.getPermiso());
            return crearNuevo();
        }
        return "/permisostrabajo/permisos.xhtml";
    }
    
    public void findPermisoProyecto(ActionEvent event){
        trabajo.setPermiso((PermisoTrabajo) event.getComponent().getAttributes().get("itemCambiar"));
        trabajo = permisoTrabajoService.findPermisoTrabajo(permisoTrabajoService.find(trabajo.getPermiso().getId()));
    }
    
    public void borrarPermisoProyecto(ActionEvent event){
        try {
            PermisoTrabajo r  = (PermisoTrabajo) event.getComponent().getAttributes().get("itemCambiar");
            permisoTrabajoService.remove(r);
            permisosProyecto.remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar la tarea del permiso de trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    //Métodos de control de visualización
    public void addEmpleado(){
        Empleado empleado = empleadoServices.find(idEmpleado);
        trabajo.getEmpleados().add(empleado);
    }
    
    public void buscarEquiposXSector(ValueChangeEvent event){
        int sector = (int) (Integer)event.getNewValue();
        if(equiposXgrupo.get(sector) != null){
            setEquipos(FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sector)));
        }else{
            setEquipos(new ArrayList<SelectItem>());
        }
        
    }

    public void seleccionarTipoTrabajo(ValueChangeEvent event){
        setTipoTrabajo((int) (Integer)event.getNewValue());
        if(getTipoTrabajo() == getPROYECTO()){
            trabajo.getPermiso().setNumOrden(null);
        }else{
            trabajo.setHoraIni(null);
            trabajo.setHoraFin(null);
            if(getTipoTrabajo() == getORDEN_TRABAJO()){
                trabajo.getPermiso().setProyecto(null);
            }else{
                trabajo.getPermiso().setProyecto(null);
                trabajo.getPermiso().setNumOrden(null);
            }
        }
    }

    public void seleccionarTipoEjecutante(ValueChangeEvent event){
        setTipoEjecutante((int) (Integer)event.getNewValue());
        if(getTipoEjecutante() == getCONTRATISTA()){
            trabajo.setEmpleados(new ArrayList<Empleado>());
            trabajo.setContratista(new Contratista());
        }else{
            trabajo.setContratista(null);
        }
    }


    /**
     * @return the PermisoTrabajo
     */
    public PermisoTrabajoTO getTrabajo() {
        return trabajo;
    }

    /**
     * @param PermisoTrabajo the PermisoTrabajo to set
     */
    public void setTrabajo(PermisoTrabajoTO PermisoTrabajo) {
        this.trabajo = PermisoTrabajo;
    }

    /**
     * @return the PermisoTrabajos
     */
    public List<PermisoTrabajo> getTrabajos() {
        return trabajos;
    }

    /**
     * @param PermisoTrabajos the PermisoTrabajos to set
     */
    public void setTrabajos(List<PermisoTrabajo> PermisoTrabajos) {
        this.trabajos = PermisoTrabajos;
    }

    /**
     * @return the proyectos
     */
    public List<SelectItem> getProyectos() {
        return proyectos;
    }

    /**
     * @param proyectos the proyectos to set
     */
    public void setProyectos(List<SelectItem> proyectos) {
        this.proyectos = proyectos;
    }

    /**
     * @return the equipos
     */
    public List<SelectItem> getEquipos() {
        return equipos;
    }

    /**
     * @param equipos the equipos to set
     */
    public void setEquipos(List<SelectItem> equipos) {
        this.equipos = equipos;
    }

    /**
     * @return the areas
     */
    public List<SelectItem> getSectores() {
        return sectores;
    }

    /**
     * @param areas the areas to set
     */
    public void setSectores(List<SelectItem> areas) {
        this.sectores = areas;
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

    /**
     * @return the permisosProyecto
     */
    public List<PermisoTrabajo> getPermisosProyecto() {
        return permisosProyecto;
    }

    /**
     * @param permisosProyecto the permisosProyecto to set
     */
    public void setPermisosProyecto(List<PermisoTrabajo> permisosProyecto) {
        this.permisosProyecto = permisosProyecto;
    }



    /**
     * @return the PROYECTO
     */
    public int getPROYECTO() {
        return PROYECTO;
    }

    /**
     * @return the ORDEN_TRABAJO
     */
    public int getORDEN_TRABAJO() {
        return ORDEN_TRABAJO;
    }

    /**
     * @return the CORRECTIVO_SIN_ORDEN
     */
    public int getCORRECTIVO_SIN_ORDEN() {
        return CORRECTIVO_SIN_ORDEN;
    }

    /**
     * @return the tipoTrabajo
     */
    public int getTipoTrabajo() {
        return tipoTrabajo;
    }

    /**
     * @param tipoTrabajo the tipoTrabajo to set
     */
    public void setTipoTrabajo(int tipoTrabajo) {
        this.tipoTrabajo = tipoTrabajo;
    }

    /**
     * @return the CONTRATISTA
     */
    public int getCONTRATISTA() {
        return CONTRATISTA;
    }

    /**
     * @return the EMPLEADO
     */
    public int getEMPLEADO() {
        return EMPLEADO;
    }

    /**
     * @return the tipoEjecutante
     */
    public int getTipoEjecutante() {
        return tipoEjecutante;
    }

    /**
     * @param tipoEjecutante the tipoEjecutante to set
     */
    public void setTipoEjecutante(int tipoEjecutante) {
        this.tipoEjecutante = tipoEjecutante;
    }

    /**
     * @return the idEmpleado
     */
    public long getIdEmpleado() {
        return idEmpleado;
    }

    /**
     * @param idEmpleado the idEmpleado to set
     */
    public void setIdEmpleado(long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

}
