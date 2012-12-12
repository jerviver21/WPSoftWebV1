
package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.EstadoException;
import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.Proyecto;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.CreacionPermisoServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.permisostrabajo.services.ProyectoServicesLocal;
import com.vbrothers.permisostrabajo.to.PermisoTrabajoTO;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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
 * @author Jerson Viveros Aguirre
 */

@ManagedBean(name="proyectoController")
@SessionScoped
public class ProyectoController {
    ServiceLocator locator;
    @EJB
    CreacionPermisoServicesLocal permisoService;
    @EJB
    ProyectoServicesLocal proyectoService;
    @EJB
    EmpleadoServicesLocal empleadoService;
    @EJB
    EmpleadoServicesLocal empleadoServices;  
    @EJB
    ContratistaServicesLocal contratistaServices;
    
    SessionController sesion;
   
    //Atributos para diligenciar datos del Proyecto
    private Proyecto proyecto;
    private List<Proyecto> proyectos;
    private List<SelectItem> estados;
    
    //Parámetros para búsqueda de proyectos
    private Date fechaDesde;
    private Date fechaHasta;
    
    //Parametros para agregar las actividades - permisos de trabajo al proyecto
    //parametros de definición de tipo de ejecutante
    private final int CONTRATISTA = 0;
    private final int EMPLEADO = 1;
    private int tipoEjecutante = EMPLEADO;
    
    /*Permite la creación y consulta de permisos de trabajo*/
    private PermisoTrabajoTO permiso;
    private List<SelectItem> equipos;
    private List<SelectItem> sectores;
    private List<SelectItem> contratistas;
    private List<SelectItem> empleados;
    
    //Este mapa permite la selección del subcombo equipos, que depende del sector.
    private Map equiposXgrupo;
    
    //Permite seleccionar varios empleados
    private long idEmpleado;
    
    //Parametros de Navegación en el flujo de creación de proyectos
    int PAG_PROYECTOS = 1;
    int PAG_PROY_DATOS = 2;
    int PAG_PROY_PERMISOS = 3;
    int PAG_DATOS_PERMISO = 4;
    int PAG_PROY_BUSQUEDA = 5;
    int PAG_ACTUAL = PAG_PROYECTOS;
    
    private int MODO_EDICION = 1;
    private int MODO_CREACION = 2;
    int MODO_PROYECTO = getMODO_CREACION();
    private int MODO_PERMISO = getMODO_CREACION();

    

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        setProyectos(proyectoService.findAll());
        
        equiposXgrupo = locator.getDataForSubcombo(ServiceLocator.SUBC_SECTOR_EQUIPO);
        sectores = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR));
        equipos = FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sectores.get(0).getValue()));
        empleados = FacesUtil.getSelectsItem(empleadoServices.findEmpleadosActivosPlanta());
        contratistas = FacesUtil.getSelectsItem(contratistaServices.findContratistasActivos());
        estados = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_ESTADOSPROYECTO));
        crearNuevo();
    }
    
    //Eventos de la página proyectos.xhtml
    public String crearNuevo(){
        proyecto = new Proyecto();
        proyecto.setUsuarioCreacion(sesion.getUsuario().getUsr());
        PAG_ACTUAL = PAG_PROY_DATOS; 
        MODO_PROYECTO = getMODO_CREACION();
        return "/permisostrabajo/proyecto_datos.xhtml";
    }
    
    public void borrarProyecto(ActionEvent event){
        try {
            Proyecto r  = (Proyecto) event.getComponent().getAttributes().get("itemCambiar");
            proyectoService.borrarProyecto(r);
            proyectos.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO, "Proyecto borrado con exito!");
        } catch (EstadoException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String consultarProyecto(Proyecto proyecto){
        this.proyecto = proyectoService.findProyecto(proyecto.getId());
        PAG_ACTUAL = PAG_PROY_PERMISOS;
        MODO_PROYECTO = getMODO_EDICION();
        return "/permisostrabajo/proyecto_permisos.xhtml";
    }

    //Eventos de la página proyecto_datos.xhtml
    public String createProyecto(){
        try {
            proyectoService.edit(proyecto);
            proyectos.add(proyecto);
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        PAG_ACTUAL = PAG_PROY_PERMISOS;
        return "/permisostrabajo/proyecto_permisos.xhtml";
    }
    
    //Métodos para eventos de proyecto_busqueda.xhtml
    
    public String navBusqueda(){
        PAG_ACTUAL = PAG_PROY_BUSQUEDA;
        return "/permisostrabajo/proyecto_busqueda.xhtml";
    }
    
    public String navProyectos(){
        PAG_ACTUAL = PAG_PROYECTOS;
        return "/permisostrabajo/proyectos.xhtml";
    }
    
    public String consultarProyectos(){
        proyectos = proyectoService.findProyectos(sesion.getUsuario().getUsr(), fechaDesde, fechaHasta);
        PAG_ACTUAL = PAG_PROYECTOS;
        return "/permisostrabajo/proyectos.xhtml";
    }
    
    //Métodos para el manejo de eventos de la página proyecto_permisos.xhtml
    public String crearPermiso(){
        setPermiso(new PermisoTrabajoTO());
        permiso.setUsr(sesion.getUsuario());
        permiso.getPermiso().setProyecto(proyecto);
        tipoEjecutante = EMPLEADO;
        PAG_ACTUAL = PAG_DATOS_PERMISO;
        MODO_PERMISO = getMODO_CREACION();
        return "/permisostrabajo/proyecto_actividad.xhtml";
    }
    
    public String consultarPermiso(PermisoTrabajo p){
        permiso = permisoService.findPermisoTrabajo(p.getId());
        if(permiso.getPermiso().isEjecutorContratista()){
            tipoEjecutante = CONTRATISTA;
        }else{
            tipoEjecutante = EMPLEADO;
        }
        MODO_PERMISO = getMODO_EDICION();
        PAG_ACTUAL = PAG_DATOS_PERMISO;
        return "/permisostrabajo/proyecto_actividad.xhtml";
    }
    
    public void borrarPermiso(PermisoTrabajo p){
        try {
            permisoService.deletePermiso(p);
            proyecto.getPermisos().remove(p);
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso borrado con exito!");
        } catch (EstadoException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String finalizar(){
        try {
            proyectoService.edit(proyecto);
            proyectos = proyectoService.findProyectosActivos();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }      
        PAG_ACTUAL = PAG_PROYECTOS;
        FacesUtil.addMessage(FacesUtil.INFO, "Proyecto guardado!"); 
        return "/permisostrabajo/proyectos.xhtml";
    }
    
    public void selectEstado(ValueChangeEvent event){
        int estado = (int) (Integer)event.getNewValue();
        proyecto.setEstado(proyectoService.findEstadoById(estado));
    }
    
    //Métodos para el manejo de eventos de la página proyecto_actividad.xhtml
    public void addEmpleado(){
        Empleado empleado = empleadoServices.find(idEmpleado);
        permiso.getEmpleados().add(empleado);
    }
    
    public void removeEmpleado(ActionEvent event){
        Empleado empleado = (Empleado) event.getComponent().getAttributes().get("itemCambiar");
        permiso.getEmpleados().remove(empleado);
    }
    
    public void buscarEquiposXSector(ValueChangeEvent event){
        int sector = (int) (Integer)event.getNewValue();
        if(equiposXgrupo.get(sector) != null){
            setEquipos(FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sector)));
        }else{
            setEquipos(new ArrayList<SelectItem>());
        }
        
    }
    
    public void seleccionarTipoEjecutante(ValueChangeEvent event){
        setTipoEjecutante((int) (Integer)event.getNewValue());
        if(getTipoEjecutante() == getCONTRATISTA()){
            permiso.setEmpleados(new ArrayList<Empleado>());
            permiso.setContratista(new Contratista());
        }else{
            permiso.setContratista(null);
        }
    }
    
    public String guardarPermiso(){
        try {
            permiso.getPermiso().setEjecutorContratista(tipoEjecutante == CONTRATISTA ? true : false);
            permiso.getPermiso().setUsuarioCreacion(sesion.getUsuario().getUsr());
            if(MODO_PERMISO == MODO_CREACION){
                permisoService.crearPermiso(permiso);
                proyecto.getPermisos().add(permiso.getPermiso());
            }else{
                proyecto.getPermisos().remove(permiso.getPermiso());
                permisoService.actualizarPermiso(permiso);
                proyecto.getPermisos().add(permiso.getPermiso());
            }
            FacesUtil.addMessage(FacesUtil.INFO, "La actividad fue guardado!");  
        } catch (ParseException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El formato de hora es incorrecto, debe ser  HH:mm");
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        PAG_ACTUAL = PAG_PROY_PERMISOS;
        return "/permisostrabajo/proyecto_permisos.xhtml";
    }
    
    //Eventos para la navegación en el flujo de creación de proyectos
    public String navBotonAtras(){
        if(PAG_ACTUAL == PAG_PROY_PERMISOS && MODO_PROYECTO == getMODO_CREACION()){
            PAG_ACTUAL = PAG_PROY_DATOS;
            return "/permisostrabajo/proyecto_datos.xhtml";
        }else if(PAG_ACTUAL == PAG_DATOS_PERMISO){
            PAG_ACTUAL = PAG_PROY_PERMISOS;
            return "/permisostrabajo/proyecto_permisos.xhtml";
        }
        return  "/permisostrabajo/proyectos.xhtml";
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
     * @return the fechaDesde
     */
    public Date getFechaDesde() {
        return fechaDesde;
    }

    /**
     * @param fechaDesde the fechaDesde to set
     */
    public void setFechaDesde(Date fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    /**
     * @return the fechaHasta
     */
    public Date getFechaHasta() {
        return fechaHasta;
    }

    /**
     * @param fechaHasta the fechaHasta to set
     */
    public void setFechaHasta(Date fechaHasta) {
        this.fechaHasta = fechaHasta;
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
     * @return the permiso
     */
    public PermisoTrabajoTO getPermiso() {
        return permiso;
    }

    /**
     * @param permiso the permiso to set
     */
    public void setPermiso(PermisoTrabajoTO permiso) {
        this.permiso = permiso;
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
     * @return the sectores
     */
    public List<SelectItem> getSectores() {
        return sectores;
    }

    /**
     * @param sectores the sectores to set
     */
    public void setSectores(List<SelectItem> sectores) {
        this.sectores = sectores;
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
     * @return the equiposXgrupo
     */
    public Map getEquiposXgrupo() {
        return equiposXgrupo;
    }

    /**
     * @param equiposXgrupo the equiposXgrupo to set
     */
    public void setEquiposXgrupo(Map equiposXgrupo) {
        this.equiposXgrupo = equiposXgrupo;
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

    /**
     * @return the estados
     */
    public List<SelectItem> getEstados() {
        return estados;
    }

    /**
     * @param estados the estados to set
     */
    public void setEstados(List<SelectItem> estados) {
        this.estados = estados;
    }

    /**
     * @return the MODO_PERMISO
     */
    public int getMODO_PERMISO() {
        return MODO_PERMISO;
    }

    /**
     * @param MODO_PERMISO the MODO_PERMISO to set
     */
    public void setMODO_PERMISO(int MODO_PERMISO) {
        this.MODO_PERMISO = MODO_PERMISO;
    }

    /**
     * @return the MODO_EDICION
     */
    public int getMODO_EDICION() {
        return MODO_EDICION;
    }

    /**
     * @param MODO_EDICION the MODO_EDICION to set
     */
    public void setMODO_EDICION(int MODO_EDICION) {
        this.MODO_EDICION = MODO_EDICION;
    }

    /**
     * @return the MODO_CREACION
     */
    public int getMODO_CREACION() {
        return MODO_CREACION;
    }

    /**
     * @param MODO_CREACION the MODO_CREACION to set
     */
    public void setMODO_CREACION(int MODO_CREACION) {
        this.MODO_CREACION = MODO_CREACION;
    }



}
