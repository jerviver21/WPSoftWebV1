package com.vbrothers.permisostrabajo.managed;

import com.vi.comun.exceptions.EstadoException;
import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vi.comun.exceptions.ValidacionException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.dominio.Equipo;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.Proyecto;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.permisostrabajo.services.PermisoServicesLocal;
import com.vbrothers.permisostrabajo.services.ProyectoServicesLocal;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.primefaces.event.DateSelectEvent;

/**
 * @author Jerson Viveros Aguirre
 */
@ManagedBean(name="proyectoController")
@SessionScoped
public class ProyectoController {
    ServiceLocator locator;
    @EJB
    PermisoServicesLocal permisoService;
    @EJB
    ProyectoServicesLocal proyectoService;
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
    private long idEmpleado;//Permite seleccionar varios empleados
    
    /*Permite la creación y consulta de permisos de trabajo*/
    private PermisoTrabajo permiso;
    private List<SelectItem> equipos = new ArrayList<SelectItem>();
    private List<SelectItem> sectores= new ArrayList<SelectItem>();
    private List<SelectItem> contratistas= new ArrayList<SelectItem>();
    private List<SelectItem> empleados= new ArrayList<SelectItem>();
    private Map equiposXgrupo; //Este mapa permite la selección del subcombo equipos, que depende del sector.
    
    //Paginas de Navegación en el flujo de creación de proyectos
    String PAG_PROYECTOS = "/permisostrabajo/proyectos.xhtml";
    String PAG_PROY_DATOS = "/permisostrabajo/proyecto_datos.xhtml";
    String PAG_PROY_PERMISOS = "/permisostrabajo/proyecto_permisos.xhtml";
    String PAG_DATOS_PERMISO = "/permisostrabajo/proyecto_actividad.xhtml";
    String PAG_PROY_BUSQUEDA = "/permisostrabajo/proyecto_busqueda.xhtml";
    
    //Validaciones de Fechas
    SimpleDateFormat fd = new SimpleDateFormat("yyyy-MM-dd");
    private String fechaActual;
    private String fechaIniProy;
    private String fechaFinProy;
    private String fechaIniPer;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        proyectos = proyectoService.findProyectosActivos(sesion.getUsuario());
        equiposXgrupo = locator.getDataForSubcombo(ServiceLocator.SUBC_SECTOR_EQUIPO);
        sectores = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR));
        if(!sectores.isEmpty()){
            equipos = FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sectores.get(0).getValue()));
        }else{
            FacesUtil.addMessage(FacesUtil.ERROR, "Para crear un proyecto debe existir al menos un sector creado");
        }
        empleados = FacesUtil.getSelectsItem(empleadoServices.findEmpleadosActivosPlanta());
        contratistas = FacesUtil.getSelectsItem(contratistaServices.findContratistasActivos());
        if(contratistas.isEmpty()  && empleados.isEmpty()){
            FacesUtil.addMessage(FacesUtil.ERROR, "Para crear un proyecto debe existir al menos un contratista o un empleado creado");
        }
        estados = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_ESTADOSPROYECTO));
        fechaActual = fd.format(new Date());
        crearNuevoProyecto();
    }
    
    //Eventos de la página proyectos.xhtml
    public String crearNuevoProyecto(){
        proyecto = new Proyecto();
        proyecto.setUsuarioCreacion(sesion.getUsuario().getUsr());
        return PAG_PROY_DATOS;
    }
    
    public void borrarProyecto(Proyecto r){
        try {
            proyectoService.borrarProyecto(r);
            proyectos.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO, "Proyecto borrado con exito!");
        } catch (EstadoException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().info(e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String consultarProyecto(Proyecto proyecto){
        this.proyecto = proyectoService.findProyecto(proyecto.getId());
        fechaIniProy = fd.format(proyecto.getFechaIni());
        fechaFinProy = fd.format(proyecto.getFechaFin());
        return PAG_PROY_PERMISOS;
    }
    
    public String navBusqueda(){
        return PAG_PROY_BUSQUEDA;
    }

    //Eventos de la página proyecto_datos.xhtml
    public String guardarProyecto(){
        try {
            proyectoService.edit(proyecto);
            proyectos.add(proyecto);
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el proyecto");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return PAG_PROY_PERMISOS;
    }
    
    public void formatearFechaIni(DateSelectEvent event){
        fechaIniProy = fd.format(proyecto.getFechaIni());
    }
    
    public void formatearFechaFin(DateSelectEvent event){
        fechaFinProy = fd.format(proyecto.getFechaFin());
    }
    
    //Métodos para eventos de proyecto_busqueda.xhtml    
    public String buscarProyectos(){
        proyectos = proyectoService.findProyectos(sesion.getUsuario(), fechaDesde, fechaHasta);
        return PAG_PROYECTOS;
    }
    
    //Métodos para el manejo de eventos de la página proyecto_permisos.xhtml
    public String crearNuevoPermiso(){
        setPermiso(new PermisoTrabajo(sesion.getUsuario()));
        permiso.setProyecto(proyecto);
        if(permiso.getSector().getId() != null && permiso.getSector().getId() != 0){
            equipos = FacesUtil.getSelectsItem((Map)equiposXgrupo.get(permiso.getSector().getId()));
        }
        tipoEjecutante = EMPLEADO;
        return PAG_DATOS_PERMISO;
    }
    
    public String consultarPermiso(PermisoTrabajo p){
        permiso = permisoService.findPermisoForCreacion(p.getId());
        equipos = FacesUtil.getSelectsItem((Map)equiposXgrupo.get(permiso.getSector().getId()));
        permiso.setEquipo(permiso.getEquipo() == null ? new Equipo(null) : permiso.getEquipo());
        if(permiso.isEjecutorContratista()){
            tipoEjecutante = CONTRATISTA;
        }else{
            tipoEjecutante = EMPLEADO;
        }
        return PAG_DATOS_PERMISO;
    }
    
    public void borrarPermiso(PermisoTrabajo p){
        try {
            permisoService.deletePermiso(p);
            proyecto.getPermisos().remove(p);
            FacesUtil.restartBean("creaPermisoController");
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
        FacesUtil.addMessage(FacesUtil.INFO, "Proyecto guardado!"); 
        return PAG_PROYECTOS;
    }
    
    public void selectEstado(ValueChangeEvent event){
        int estado = (int) (Integer)event.getNewValue();
        proyecto.setEstado(proyectoService.findEstadoById(estado));
    }
    
    //Métodos para el manejo de eventos de la página proyecto_actividad.xhtml
    public void addEmpleado(){
        Empleado empleado = empleadoServices.find(idEmpleado);
        if(!permiso.getEmpleados().contains(empleado)){
            permiso.getEmpleados().add(empleado);
        }
    }
    
    public void removeEmpleado(Empleado empleado){
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
            empleados = new ArrayList<SelectItem>();
            permiso.setContratista(new Contratista());
        }else{
            permiso.setContratista(null);
            empleados = FacesUtil.getSelectsItem(empleadoServices.findEmpleadosActivosPlanta());
        }
        System.out.println("Tipo de ejecutante: "+getTipoEjecutante());
    }
    
    public void formatearFechaIniPer(DateSelectEvent event){
        SimpleDateFormat fd1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        fechaIniPer = fd1.format(permiso.getFechaHoraIni());
    }
    
    public String guardarPermiso(){
        try {
            if(permiso.getId() == null){
                permisoService.crearPermiso(permiso);              
            }else{
                if(permiso.getEquipo().getId() == null || 
                        permiso.getEquipo().getId() == 0 ){
                    permiso.setEquipo(null);
                }
                permisoService.actualizarPermiso(permiso);
                proyecto.getPermisos().remove(permiso);
            }
            proyecto.getPermisos().add(permiso);
            FacesUtil.restartBean("creaPermisoController");
            FacesUtil.addMessage(FacesUtil.INFO, "La actividad fue guardado!");  
        } catch (ValidacionException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error de validación: "+e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return PAG_PROY_PERMISOS;
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
    public PermisoTrabajo getPermiso() {
        return permiso;
    }

    /**
     * @param permiso the permiso to set
     */
    public void setPermiso(PermisoTrabajo permiso) {
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
     * @return the fechaActual
     */
    public String getFechaActual() {
        return fechaActual;
    }

    /**
     * @return the fechaIniProy
     */
    public String getFechaIniProy() {
        return fechaIniProy;
    }

    /**
     * @return the fechaFinProy
     */
    public String getFechaFinProy() {
        return fechaFinProy;
    }

    /**
     * @return the fechaIniPer
     */
    public String getFechaIniPer() {
        return fechaIniPer;
    }

}
