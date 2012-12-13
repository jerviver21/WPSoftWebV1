package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.EstadoException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.TrazabilidadPermiso;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.CreacionPermisoServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.permisostrabajo.services.GestionPermisoServiceLocal;
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
 * @author Jerson Viveros
 */

@ManagedBean(name="creaPermisoController")
@SessionScoped
public class CreaPermisoController {
    ServiceLocator locator;
    @EJB
    CreacionPermisoServicesLocal permisoService;
    @EJB
    GestionPermisoServiceLocal gestionPermisoService;
    @EJB
    EmpleadoServicesLocal empleadoServices;  
    @EJB
    ContratistaServicesLocal contratistaServices;

    SessionController sesion;
    
    /*Permite la creación y consulta de permisos de trabajo*/
    private PermisoTrabajoTO permiso;
    private List<SelectItem> equipos;
    private List<SelectItem> sectores;
    private List<SelectItem> contratistas;
    private List<SelectItem> empleados;
    private Map equiposXgrupo;//Este mapa permite la selección del subcombo equipos, que depende del sector.
    
    //Parametros de definicion del trabajo
    private final int ORDEN_TRABAJO = 1;
    private final int CORRECTIVO_SIN_ORDEN = 2;
    private int tipoPermiso = CORRECTIVO_SIN_ORDEN;
    //parametros de definición de tipo de ejecutante
    private final int CONTRATISTA = 0;
    private final int EMPLEADO = 1;
    private int tipoEjecutante = EMPLEADO;
    private long idEmpleado;//Permite seleccionar varios empleados
    
    /*Permite listar los permisos creados por el usuario*/
    private List<PermisoTrabajo> permisos;
    
    //Parámetros para la búsquedad de permisos
    private List<SelectItem> estados;
    private int estadoBusqueda;
    private Date fechaDesde;
    private Date fechaHasta;
    
    //Datos de trazabilidad
    private List<TrazabilidadPermiso> traz;
    
    String PAG_PERMISOS = "/permisostrabajo/permisos.xhtml";
    String PAG_SEL_TIPO = "/permisostrabajo/creacion_sel_tipo.xhtml";
    String PAG_TRAZA = "/permisostrabajo/creacion_trazabilidad.xhtml";
    String PAG_BUSQUEDA = "/permisostrabajo/creacion_busquedad.xhtml";
    String PAG_DATOS = "/permisostrabajo/creacion_datos.xhtml";
    

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        permisos = permisoService.findPermisosEnProceso(sesion.getUsuario().getUsr());
        equiposXgrupo = locator.getDataForSubcombo(ServiceLocator.SUBC_SECTOR_EQUIPO);
        sectores = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR));
        equipos = FacesUtil.getSelectsItem((Map)equiposXgrupo.get(sectores.get(0).getValue()));
        empleados = FacesUtil.getSelectsItem(empleadoServices.findEmpleadosActivosPlanta());
        contratistas = FacesUtil.getSelectsItem(contratistaServices.findContratistasActivos());
        setEstados(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_ESTADOSPERMISOS)));
        crearNuevoPermiso();
    }
    
    //Métodos para procesar eventos de permisos.xhtml
    public String crearNuevoPermiso(){
        setPermiso(new PermisoTrabajoTO());
        permiso.setUsr(sesion.getUsuario());
        tipoEjecutante = EMPLEADO;
        return PAG_SEL_TIPO;
    }
    
    public void borrarPermiso(PermisoTrabajo r){
        try {
            permisoService.deletePermiso(r);
            permisos.remove(r);
        } catch (EstadoException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar la tarea del permiso de trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String consultarTrazabilidad(PermisoTrabajo p){
        setTraz(gestionPermisoService.findTrazabilidadPermiso(p));
        permiso = permisoService.findPermisoTrabajo(p.getId());
        if(permiso.getPermiso().getNumOrden() != null){
            tipoPermiso = ORDEN_TRABAJO;
        }else{
            tipoPermiso = CORRECTIVO_SIN_ORDEN;
        }
        if(permiso.getPermiso().isEjecutorContratista()){
            tipoEjecutante = CONTRATISTA;
        }else{
            tipoEjecutante = EMPLEADO;
        }
        return PAG_TRAZA;
    }
    
    public String navBusqueda(){
        return PAG_BUSQUEDA;
    }
    //Métodos para procesar eventos de creacion_sel_tipo.xhtml
    
    public void seleccionarTipoPermiso(ValueChangeEvent event){
        setTipoPermiso((int) (Integer)event.getNewValue());
        permiso.getPermiso().setProyecto(null);
        if(getTipoPermiso() == getCORRECTIVO_SIN_ORDEN()){
            permiso.getPermiso().setNumOrden(null);
        }
    }
    
    public String navDatosPermiso(){
        return PAG_DATOS;
    }
    
    //Métodos para procesar eventos de creacion_trazabilidad.xhtml
    public String consultarDatosPermiso(){
        if(permiso.getPermiso().getProyecto() != null){
            ProyectoController pc = (ProyectoController)FacesUtil.getManagedBean("#{proyectoController}");
            pc.consultarProyecto(permiso.getPermiso().getProyecto());
            return pc.consultarPermiso(permiso.getPermiso());
        }
        return PAG_DATOS;
    }
    //Métodos para procesar eventos de creacion_datos.xhtml
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
            if(permiso.getPermiso().getId() == null){
                permisoService.crearPermiso(permiso);
            }else{
                permisoService.actualizarPermiso(permiso);
                permisos.remove(permiso.getPermiso());
            }
            permisos.add(permiso.getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "El permiso de trabajo fue guardado!");  
        } catch (ParseException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El formato de hora es incorrecto, debe ser  HH:mm");
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return PAG_PERMISOS;
    }
    //Métodos para controlar eventos de creacion_busqeudad.xhtml
    public String buscarPermisos(){
        permisos = permisoService.findPermisos(sesion.getUsuario().getUsr(), estadoBusqueda, fechaDesde, fechaHasta);
        return "/permisostrabajo/permisos.xhtml";
    }


    /**
     * @return the PermisoTrabajo
     */
    public PermisoTrabajoTO getPermiso() {
        return permiso;
    }

    /**
     * @param PermisoTrabajo the PermisoTrabajo to set
     */
    public void setPermiso(PermisoTrabajoTO PermisoTrabajo) {
        this.permiso = PermisoTrabajo;
    }

    /**
     * @return the PermisoTrabajos
     */
    public List<PermisoTrabajo> getPermisos() {
        return permisos;
    }

    /**
     * @param PermisoTrabajos the PermisoTrabajos to set
     */
    public void setPermisos(List<PermisoTrabajo> PermisoTrabajos) {
        this.permisos = PermisoTrabajos;
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
     * @return the tipoPermiso
     */
    public int getTipoPermiso() {
        return tipoPermiso;
    }

    /**
     * @param tipoPermiso the tipoPermiso to set
     */
    public void setTipoPermiso(int tipoPermiso) {
        this.tipoPermiso = tipoPermiso;
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
     * @return the estadoBusqueda
     */
    public int getEstadoBusqueda() {
        return estadoBusqueda;
    }

    /**
     * @param estadoBusqueda the estadoBusqueda to set
     */
    public void setEstadoBusqueda(int estadoBusqueda) {
        this.estadoBusqueda = estadoBusqueda;
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
     * @return the traz
     */
    public List<TrazabilidadPermiso> getTraz() {
        return traz;
    }

    /**
     * @param traz the traz to set
     */
    public void setTraz(List<TrazabilidadPermiso> traz) {
        this.traz = traz;
    }
}
