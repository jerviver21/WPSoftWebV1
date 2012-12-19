package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Disciplina;
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
import javax.faces.event.ValueChangeEvent;
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
    
    //Permite diligenciar los pasos del permiso
    private Tarea tarea;
    private boolean habMasTareas = false;
    
    //Permite diligenciar los riesgos del permiso
    private List<SelectItem> peligros;
    private PeligrosTarea peligro;
    private String riesgo;
    private boolean renderRiesgo = false;
    private int idPeligro = 1;//Permite agregar varios riesgos
    
    //Datos de trazabilidad
    private List<TrazabilidadPermiso> traz;
    
    String PAG_PERMISOS = "/permisostrabajo/mis_permisos.xhtml";
    String PAG_TRAZABILIDAD = "/permisostrabajo/gestion_trazabilidad.xhtml";
    String PAG_DATOS = "/permisostrabajo/gestion_datos.xhtml";
    String PAG_RIESGOS = "/permisostrabajo/gestion_riesgos.xhtml";
    String PAG_CONSIDERACIONES = "/permisostrabajo/gestion_consideraciones.xhtml";
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
        permiso.setUsr(sesion.getUsuario());
        setTraz(permisoServices.findTrazabilidadPermiso(r));
        return PAG_TRAZABILIDAD;
    }
    
    //Métodos para el manejo de eventos de la página gestion_trazabilidad.xhtml
    public String gestionarPermiso(){
        return PAG_CONSIDERACIONES;
    }
    
    //Métodos para el control de eventos de la página gestion_datos.xhtml
    public void borrarSectorAfectado(Sector sector){
       getPermiso().getPermiso().getSectoresAfectados().remove(sector);
    }

    public void agregarSectorAfectado(){
        Sector r = sectoresServices.find(idSector);
        if(!permiso.getPermiso().getSectoresAfectados().contains(r)){
            getPermiso().getPermiso().getSectoresAfectados().add(r);
        }
    }
    
    public void cambiarDisc(ValueChangeEvent event){
        permiso.getPermiso().setDisciplina(new Disciplina((int) (Integer)event.getNewValue()));
        System.out.println("---> "+permiso.getPermiso().getDisciplina().getId());
    }
    
    public void diligenciarPaso(){
        habMasTareas = true;
        for (int i = 0; i < permiso.getTareasVista().size(); i++) {
            Tarea t = permiso.getTareasVista().get(i);
            if(t.getDatos() == null || t.getDatos().equals("")){
                permiso.getTareasVista().remove(i);
                i--;
            }
        }
        for (int i = 0; i < permiso.getTareasVista().size(); i++) {
            Tarea t = permiso.getTareasVista().get(i);
            t.setConsecutivo(i+1);
        }
        while(permiso.getTareasVista().size() < 6){
            Tarea t = new Tarea();
            t.setConsecutivo(permiso.getTareasVista().size()+1);
            t.setPermiso(permiso.getPermiso());
            permiso.getTareasVista().add(t);
            habMasTareas = false;
        }
    }

    public void addTarea(){
        Tarea t = new Tarea();
        t.setConsecutivo(permiso.getTareasVista().size()+1);
        t.setPermiso(permiso.getPermiso());
        permiso.getTareasVista().add(t);
    }

    public void borrarTarea(Tarea tarea){
        try {
            for (int i = 0; i < permiso.getTareasVista().size(); i++) {
                if(tarea.getConsecutivo() == permiso.getTareasVista().get(i).getConsecutivo()){
                    permiso.getTareasVista().remove(i);
                    break;
                }
            }
            for (int i = 0; i < permiso.getTareasVista().size(); i++) {
                Tarea t = permiso.getTareasVista().get(i);
                t.setConsecutivo(i+1);
            }
            while(permiso.getTareasVista().size() < 6){
                Tarea t = new Tarea();
                t.setConsecutivo(permiso.getTareasVista().size()+1);
                t.setPermiso(permiso.getPermiso());
                permiso.getTareasVista().add(t);
                habMasTareas = false;
                System.out.println("Hab mas tareas: "+habMasTareas);
            }
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al agregar área afectada!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String gestionarPeligros(Tarea tarea){
        setTarea(tarea);
        return PAG_RIESGOS;
    }
    
    public String agregarConsideraciones(){
        Tarea t = permiso.getTareasVista().get(0);
        if(t.getDatos() == null || t.getDatos().equals("")){
            FacesUtil.addMessage(FacesUtil.ERROR, "Debe agregar al menos un paso para diligenciar el permiso");
            return null;
        }
        return PAG_CONSIDERACIONES;
    }
    
    //Métodos para el control de eventos de la página gestion_riesgos.xhtml
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

    
    public void guardar(){
        try {
            permisoServices.guardarPasos(getPermiso());
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String solicitarAprobacion(){
        try {
            permisoServices.solicitarAprobacion(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Solicitud de aprobación enviada!!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return PAG_PERMISOS;
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

    /**
     * @return the habMasTareas
     */
    public boolean isHabMasTareas() {
        return habMasTareas;
    }

    /**
     * @param habMasTareas the habMasTareas to set
     */
    public void setHabMasTareas(boolean habMasTareas) {
        this.habMasTareas = habMasTareas;
    }

}
