package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Peligro;
import com.vbrothers.permisostrabajo.dominio.PeligrosTarea;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.RiesgosPeligroTarea;
import com.vbrothers.permisostrabajo.dominio.Sector;
import com.vbrothers.permisostrabajo.dominio.Tarea;
import com.vbrothers.permisostrabajo.dominio.TrazabilidadPermiso;
import com.vbrothers.permisostrabajo.services.PermisoServicesLocal;
import com.vbrothers.permisostrabajo.to.PermisoTrabajoTO;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="gestionPermisoController")
@SessionScoped
public class GestionPermisoController {
    ServiceLocator locator;
    @EJB
    SectoresServicesLocal sectoresServices;
    @EJB
    PermisoServicesLocal permisoServices;
    @EJB
    PeligrosServicesLocal peligroServices;
    
    SessionController sesion;
    
    //Permite la gestion del permiso en todas sus etapas
    private PermisoTrabajoTO permiso;
    
    //Permite listar los permisos de trabajo pendientes
    private List<PermisoTrabajo> permisosPendientes;
    
    //Permiten diligenciar los datos generales del permiso
    private List<SelectItem> sectores;
    private List<SelectItem> disciplinas;
    private int idSector = 1;//Para poder agregar varios sectores afectados
    
    //Permite diligenciar los riesgos del permiso
    private List<SelectItem> peligros;
    private Tarea tarea;
    private PeligrosTarea peligro;
    private String riesgo;
    private boolean renderRiesgo = false;
    private int idPeligro = 1;//Permite agregar varios riesgos
    
    //Datos de trazabilidad
    private List<TrazabilidadPermiso> traz;
    
    String PAG_PERMISOS = "/permisostrabajo/mis_permisos.xhtml";
    String PAG_TRAZABILIDAD = "/permisostrabajo/gestion_trazabilidad.xhtml";
    String PAG_DATOS = "/permisostrabajo/gestion_datos.xhtml";

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        setPermisosPendientes(permisoServices.findPermisosPendientes(sesion.getUsuario()));
        setSectores(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR)));
        setDisciplinas(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_DISCIPLINA)));
        setPeligros(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_PELIGRO)));
        permiso = new PermisoTrabajoTO(sesion.getUsuario());
        tarea = new Tarea();
    }
    
    //Métodos para el manejo de eventos de la página mis_permisos.xhtml
    public String consultarTrazabilidad(PermisoTrabajo r){
        permiso = permisoServices.findPermisoForGestion(r.getId());
        setTraz(permisoServices.findTrazabilidadPermiso(r));
        return PAG_TRAZABILIDAD;
    }
    
    //Métodos para el manejo de eventos de la página gestion_trazabilidad.xhtml
    public String gestionarPermiso(){
        return PAG_DATOS;
    }

    
    public void guardar(){
        /*try {
            permisoServices.guardarPermiso(getPermiso());
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }*/
    }

    public String solicitarAprobacion(){
        /*try {
            permisoServices.solicitarAprobacion(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Solicitud de aprobación enviada!!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String aprobar(){
        /*try {
            permisoServices.aprobarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso aprobado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String noAprobar(){
        /*try {
            permisoServices.noAprobarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso no aprobado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String terminar(){
        /*try {
            permisoServices.terminarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso terminado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }


    public String cancelar(){
        /*try {
            permisoServices.cancelarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String finalizar(){
        /*try {
            permisoServices.finalizarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }*/
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }


    //Métodos de control de visualización de Riesgos
    public void borrarRiesgo(ActionEvent event){
        try {
            PeligrosTarea p  = (PeligrosTarea) event.getComponent().getAttributes().get("itemCambiar1");
            RiesgosPeligroTarea r  = (RiesgosPeligroTarea) event.getComponent().getAttributes().get("itemCambiar2");
            int i = 0;
            for (RiesgosPeligroTarea rm : p.getRiesgos()){
                if(rm.getConsecutivo() == r.getConsecutivo()){
                    p.getRiesgos().remove(i);
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el riesgo!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void addRiesgo(){
        RiesgosPeligroTarea riesgoPeligro = new RiesgosPeligroTarea();
        riesgoPeligro.setConsecutivo(peligro.getRiesgos().size() + 1);
        riesgoPeligro.setNombre(riesgo);
        riesgoPeligro.setPeligrosTarea(peligro);
        peligro.getRiesgos().add(riesgoPeligro);
        renderRiesgo = false;
        riesgo = "";
    }


    //Métodos de control de visualización de Peligros
    public void setPeligro(ActionEvent event){
        setPeligro((PeligrosTarea) event.getComponent().getAttributes().get("itemCambiar"));
        renderRiesgo = true;
    }

    public void addPeligro(){
        Peligro p1 = peligroServices.find(idPeligro);
        PeligrosTarea pt = new PeligrosTarea();
        pt.setPeligro(p1);
        pt.setTarea(tarea);
        tarea.getPeligros().add(pt);
    }
    
    
    //Métodos de control de visualización de tareas del permiso de trabajo
    public void borrarTarea(ActionEvent event){
        try {
            Tarea r  = (Tarea) event.getComponent().getAttributes().get("itemCambiar");
            permiso.getTareasVista().remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al agregar área afectada!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void addTarea(){
        Tarea t = new Tarea();
        t.setConsecutivo(permiso.getTareasVista().size());
        t.setPermiso(permiso.getPermiso());
        permiso.getTareasVista().add(getTarea());
    }

    public void setTarea(ActionEvent event){
        setTarea((Tarea) event.getComponent().getAttributes().get("itemCambiar"));
        System.out.println("Mi tarea: "+getTarea().getDatos());
    }

    //Métodos de control de visualización sectores
    public void borrarSectorAfectado(ActionEvent event){
        try {
            Sector r  = (Sector) event.getComponent().getAttributes().get("itemCambiar");
            getPermiso().getPermiso().getSectoresAfectados().remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al agregar área afectada!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void agregarSectorAfectado(){
        Sector r = sectoresServices.find(idSector);
        if(!permiso.getPermiso().getSectoresAfectados().contains(r)){
            getPermiso().getPermiso().getSectoresAfectados().add(r);
        }
    }


    /**
     * @return the permisoPendiente
     */
    public PermisoTrabajoTO getPermiso() {
        return permiso;
    }

    /**
     * @param permisoPendiente the permisoPendiente to set
     */
    public void setPermiso(PermisoTrabajoTO permisoPendiente) {
        this.permiso = permisoPendiente;
    }

    /**
     * @return the permisosPendientes
     */
    public List<PermisoTrabajo> getPermisosPendientes() {
        return permisosPendientes;
    }

    /**
     * @param permisosPendientes the permisosPendientes to set
     */
    public void setPermisosPendientes(List<PermisoTrabajo> permisosPendientes) {
        this.permisosPendientes = permisosPendientes;
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
     * @return the disciplinas
     */
    public List<SelectItem> getDisciplinas() {
        return disciplinas;
    }

    /**
     * @param disciplinas the disciplinas to set
     */
    public void setDisciplinas(List<SelectItem> disciplinas) {
        this.disciplinas = disciplinas;
    }

    /**
     * @return the peligros
     */
    public List<SelectItem> getPeligros() {
        return peligros;
    }

    /**
     * @param peligros the peligros to set
     */
    public void setPeligros(List<SelectItem> peligros) {
        this.peligros = peligros;
    }

    /**
     * @return the idSector
     */
    public int getIdSector() {
        return idSector;
    }

    /**
     * @param idSector the idSector to set
     */
    public void setIdSector(int idSector) {
        this.idSector = idSector;
    }

    /**
     * @return the tarea
     */
    public Tarea getTarea() {
        return tarea;
    }

    /**
     * @param tarea the tarea to set
     */
    public void setTarea(Tarea tarea) {
        this.tarea = tarea;
    }

    /**
     * @return the idPeligro
     */
    public int getIdPeligro() {
        return idPeligro;
    }

    /**
     * @param idPeligro the idPeligro to set
     */
    public void setIdPeligro(int idPeligro) {
        this.idPeligro = idPeligro;
    }

    /**
     * @return the peligro
     */
    public PeligrosTarea getPeligro() {
        return peligro;
    }

    /**
     * @param peligro the peligro to set
     */
    public void setPeligro(PeligrosTarea peligro) {
        this.peligro = peligro;
    }

    /**
     * @return the riesgo
     */
    public String getRiesgo() {
        return riesgo;
    }

    /**
     * @param riesgo the riesgo to set
     */
    public void setRiesgo(String riesgo) {
        this.riesgo = riesgo;
    }

    /**
     * @return the renderRiesgo
     */
    public boolean isRenderRiesgo() {
        return renderRiesgo;
    }

    /**
     * @param renderRiesgo the renderRiesgo to set
     */
    public void setRenderRiesgo(boolean renderRiesgo) {
        this.renderRiesgo = renderRiesgo;
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
