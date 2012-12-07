/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Peligro;
import com.vbrothers.permisostrabajo.dominio.PeligrosTarea;
import com.vbrothers.permisostrabajo.dominio.Sector;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.RiesgosPeligroTarea;
import com.vbrothers.permisostrabajo.dominio.Tarea;
import com.vbrothers.permisostrabajo.services.CreacionPermisoServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.permisostrabajo.to.PermisoTrabajoTO;
import com.vbrothers.usuarios.managed.SessionController;
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
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="permisoDiligenciadorController")
@SessionScoped
public class DiligenciaPermisoController {
    ServiceLocator locator;
    private PermisoTrabajoTO permisoDiligenciar;
    private List<PermisoTrabajo> permisosPendientes;
    private List<SelectItem> sectores;
    private List<SelectItem> disciplinas;
    private List<SelectItem> peligros;

    private Tarea tarea;
    private PeligrosTarea peligro;
    private String riesgo;
    private boolean renderRiesgo = false;

    //Ids para combos
    private int idSector = 1;
    private int idPeligro = 1;

    @EJB
    SectoresServicesLocal sectoresServices;

    @EJB
    CreacionPermisoServicesLocal permisoServices;

    @EJB
    PeligrosServicesLocal peligroServices;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        permisoDiligenciar = new PermisoTrabajoTO();
        permisoDiligenciar.setPermiso(new PermisoTrabajo());
        tarea = new Tarea();

        SessionController sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        setPermisosPendientes(permisoServices.findPermisosPendientes(sesion.getUsuario()));
        setSectores(FacesUtil.getSelectsItem(locator.getReferenceTable(ServiceLocator.COMB_ID_SECTOR)));
        setDisciplinas(FacesUtil.getSelectsItem(locator.getReferenceTable(ServiceLocator.COMB_ID_DISCIPLINA)));
        setPeligros(FacesUtil.getSelectsItem(locator.getReferenceTable(ServiceLocator.COMB_ID_PELIGRO)));
        permisoDiligenciar.setUsr(sesion.getUsuario());
    }

    public void mostrarPermiso(ActionEvent event){
        SessionController sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        PermisoTrabajo r  = (PermisoTrabajo) event.getComponent().getAttributes().get("itemCambiar");
        permisoDiligenciar = permisoServices.findPermisoTrabajo(r.getId());
        permisoDiligenciar.setUsr(sesion.getUsuario());
    }
    
    public String navDiligenciar(){
        return "/permisostrabajo/permiso_diligenciar.xhtml";
    }
    
    public void guardar(){
        try {
            permisoServices.guardarPermiso(getPermisoDiligenciar());
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String solicitarAprobacion(){
        try {
            permisoServices.solicitarAprobacion(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Solicitud de aprobación enviada!!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String aprobar(){
        try {
            permisoServices.aprobarPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso aprobado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String noAprobar(){
        try {
            permisoServices.noAprobarPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso no aprobado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String terminar(){
        try {
            permisoServices.terminarPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso terminado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }


    public String cancelar(){
        try {
            permisoServices.cancelarPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permisos_diligenciar.xhtml";
    }

    public String finalizar(){
        try {
            permisoServices.finalizarPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
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
            permisoDiligenciar.getTareasVista().remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al agregar área afectada!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void addTarea(){
        Tarea t = new Tarea();
        t.setConsecutivo(permisoDiligenciar.getTareasVista().size());
        t.setPermiso(permisoDiligenciar.getPermiso());
        permisoDiligenciar.getTareasVista().add(getTarea());
    }

    public void setTarea(ActionEvent event){
        setTarea((Tarea) event.getComponent().getAttributes().get("itemCambiar"));
        System.out.println("Mi tarea: "+getTarea().getDatos());
    }

    //Métodos de control de visualización sectores
    public void borrarSectorAfectado(ActionEvent event){
        try {
            Sector r  = (Sector) event.getComponent().getAttributes().get("itemCambiar");
            getPermisoDiligenciar().getPermiso().getSectoresAfectados().remove(r);
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al agregar área afectada!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void agregarSectorAfectado(){
        Sector r = sectoresServices.find(idSector);
        if(!permisoDiligenciar.getPermiso().getSectoresAfectados().contains(r)){
            getPermisoDiligenciar().getPermiso().getSectoresAfectados().add(r);
        }
    }




    /**
     * @return the permisoPendiente
     */
    public PermisoTrabajoTO getPermisoDiligenciar() {
        return permisoDiligenciar;
    }

    /**
     * @param permisoPendiente the permisoPendiente to set
     */
    public void setPermisoDiligenciar(PermisoTrabajoTO permisoPendiente) {
        this.permisoDiligenciar = permisoPendiente;
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


    /*public String desbloquear(){
        try {
            permisoServices.desbloquearPermiso(getPermisoDiligenciar());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso diligenciado con exito!!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            return null;
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return "/permisostrabajo/permiso_diligenciar.xhtml";
    }*/
    

    

}
