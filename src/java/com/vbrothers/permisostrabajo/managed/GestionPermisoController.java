package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.herramientas.services.CertificadosServicesLocal;
import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Certificado;
import com.vbrothers.permisostrabajo.dominio.Control;
import com.vbrothers.permisostrabajo.dominio.ControlesPeligroTarea;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="gestionPermisoController")
@SessionScoped
public class GestionPermisoController implements Serializable{
    ServiceLocator locator;
    @EJB
    SectoresServicesLocal sectoresServices;
    @EJB
    CertificadosServicesLocal certificadosServices;
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
    private List<SelectItem> certificados;
    private List<SelectItem> disciplinas;
    private int idSector;//Para poder agregar varios sectores afectados
    private int idCertificado;//Para poder agregar varios certificados
    private StreamedContent checklistDown;//Para descargar los checklist de los certificados agregados
    
    //Permite diligenciar los pasos del permiso
    private Tarea tarea;
    private boolean habMasTareas = false;
    
    //Permite diligenciar los riesgos del permiso
    private List<SelectItem> peligros;
    private Peligro peligro;
    private String control;
    private String riesgo;
    
    //Permite agregar mas usuarios o grupos para aprobación
    private List<SelectItem> usrsGrupos;
    private String usrGrupo;
    
    
    //Datos de trazabilidad
    private List<TrazabilidadPermiso> traz;
       
    String PAG_PERMISOS = "/permisostrabajo/mis_permisos.xhtml";
    String PAG_TRAZABILIDAD = "/permisostrabajo/gestion_trazabilidad.xhtml";
    String PAG_DATOS = "/permisostrabajo/gestion_datos.xhtml";
    String PAG_RIESGOS = "/permisostrabajo/gestion_riesgos.xhtml";
    String PAG_CONSIDERACIONES = "/permisostrabajo/gestion_consideraciones.xhtml";
    String PAG_APR_OTROS = "/permisostrabajo/gestion_aprob_extras.xhtml";
    String PAG_EJECUCION = "/permisostrabajo/gestion_ejecutar.xhtml";
    
    
    
    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        setPermisosPendientes(permisoServices.findPermisosPendientes(sesion.getUsuario()));
        setSectores(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR)));
        certificados = FacesUtil.getSelectsItem(certificadosServices.findAll());
        setDisciplinas(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_DISCIPLINA)));
        permiso = new PermisoTrabajoTO(sesion.getUsuario());
        setPeligros(FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_PELIGRO)));
        setPeligro(new Peligro());
        tarea = new Tarea();
    }
    
    //Métodos para el manejo de eventos de la página mis_permisos.xhtml
    public String consultarTrazabilidad(PermisoTrabajo r){
        permiso = permisoServices.findPermisoForGestion(r.getId());
        permiso.setUsr(sesion.getUsuario());
        for(SelectItem item: sectores){
            int idItem = (Integer)item.getValue();
            if(permiso.getPermiso().getSector().getId() == idItem){
                sectores.remove(item);
                break;
            }
        }
        setTraz(permisoServices.findTrazabilidadPermiso(r));
        setUsrsGrupos(FacesUtil.getSelectsItem(permisoServices.findGruposAprobadores(permiso.getPermiso()), "getCodigo", "getCodigo"));
        return PAG_TRAZABILIDAD;
    }
    
    //Métodos para el manejo de eventos de la página gestion_trazabilidad.xhtml
    public String gestionarPermiso(){
        if(permiso.getEtapa() == permiso.getTERMINAR() 
                || permiso.getEtapa() == permiso.getCANCELAR()
                || permiso.getEtapa() == permiso.getFINALIZAR()){
            return PAG_EJECUCION;
        }
        return PAG_DATOS;
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
            
            if(tarea.getId() != null && tarea.getId() != 0){
                permisoServices.borrarTarea(tarea);
                permisoServices.guardarGestion(permiso);
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
    
    public String terminaGestionDatos(){
        Tarea t = permiso.getTareasVista().get(0);
        if(t.getDatos() == null || t.getDatos().equals("")){
            FacesUtil.addMessage(FacesUtil.ERROR, "Debe agregar al menos un paso para diligenciar el permiso");
            return null;
        }
        String SIG_PAGINA = PAG_CONSIDERACIONES;
        if(permiso.getEtapa() == permiso.getAPROBAR()){
            SIG_PAGINA = PAG_APR_OTROS;
        }
        return SIG_PAGINA;
    }
    
    //Métodos para el control de eventos de la página gestion_riesgos.xhtml
    public void agregarPeligro() { 
        for(PeligrosTarea pt : tarea.getPeligros()){
            if(pt.getPeligro().getId()== getPeligro().getId()){
                return;
            }
        }
        Peligro p = peligroServices.find(getPeligro().getId());
        PeligrosTarea pelTarea = new PeligrosTarea();
        pelTarea.setTarea(tarea);
        pelTarea.setPeligro(p);
        List<ControlesPeligroTarea> controles = new ArrayList<ControlesPeligroTarea>();
        for(Control c : p.getControles()){
            ControlesPeligroTarea cpt = new ControlesPeligroTarea();
            cpt.setControl(c.getNombre());
            cpt.setPeligrosTarea(pelTarea);
            controles.add(cpt);
        }
        pelTarea.setControles(controles);
        pelTarea.setRiesgos(new ArrayList<RiesgosPeligroTarea>());
        tarea.getPeligros().add(pelTarea);
    }  
    
    public void borrarPeligro(PeligrosTarea pt) { 
        for (int i = 0; i < tarea.getPeligros().size(); i++) {
            PeligrosTarea pt1 = tarea.getPeligros().get(i);
            if(pt1.getPeligro().getId() == pt.getPeligro().getId()){
                tarea.getPeligros().remove(i);
                if(pt1.getId() != null && pt1.getId() != 0){
                    permisoServices.borrarPeligro(pt1);
                }
            }
        }
    } 
    
    
    public void agregarControl(PeligrosTarea pt){
        ControlesPeligroTarea cpt = new ControlesPeligroTarea();
        cpt.setControl(control);
        cpt.setPeligrosTarea(pt);
        pt.getControles().add(cpt);
        control = "";
    }
    
    public void borrarControl(PeligrosTarea pt, ControlesPeligroTarea ctr){
        for (int i = 0; i < pt.getControles().size(); i++) {
            ControlesPeligroTarea ctrpt = pt.getControles().get(i);
            if(ctr.getControl().equals(ctrpt.getControl())){
                 pt.getControles().remove(i);
                 if(ctrpt.getId() != null && ctrpt.getId() != 0){
                    permisoServices.borrarControl(ctrpt);
                 }
            }
        }
    }
    
    public void agregarRiesgo(PeligrosTarea pt){
        RiesgosPeligroTarea rpt = new RiesgosPeligroTarea();
        rpt.setNombre(riesgo);
        rpt.setPeligrosTarea(pt);
        pt.getRiesgos().add(rpt);
        riesgo = "";
    }
    
    public void borrarRiesgo(PeligrosTarea pt, RiesgosPeligroTarea rpt){
        for (int i = 0; i < pt.getRiesgos().size(); i++) {
            RiesgosPeligroTarea rrpt = pt.getRiesgos().get(i);
            if(rpt.getNombre().equals(rrpt.getNombre())){
                 pt.getRiesgos().remove(i);
                 if(rrpt.getId() != null && rrpt.getId() != 0){
                    permisoServices.borrarRiesgo(rrpt);
                 }
            }
        }
    }
    
    public String guardarPeligrosPaso(){
        try {
            permisoServices.guardarGestion(getPermiso());
            while(permiso.getTareasVista().size() < 6){
                Tarea t = new Tarea();
                t.setConsecutivo(permiso.getTareasVista().size()+1);
                t.setPermiso(permiso.getPermiso());
                permiso.getTareasVista().add(t);
            }
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el Permiso de Trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return PAG_DATOS;
    }

    //Eventos de la página gestion_consideraciones.xhtml
    public void borrarCertificado(Certificado cert){
       getPermiso().getPermiso().getCertificados().remove(cert);
    }

    public void agregarCertificado(){
        Certificado r = certificadosServices.find(idCertificado);
        if(!permiso.getPermiso().getCertificados().contains(r)){
            getPermiso().getPermiso().getCertificados().add(r);
        }
    }
    
    public void descargarChecklist(Certificado cert){
        try {
            FileInputStream stream = new FileInputStream(cert.getRutaCheckList());
            String separador = File.separator.equals("/")?"/":"\\";
            checklistDown = new DefaultStreamedContent(stream, "application/pdf",
                    cert.getRutaCheckList().replaceAll(".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String solicitarAprobacion(){
        try {
            permisoServices.solicitarAprobacion(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Solicitud de aprobación enviada!!");
            FacesUtil.restartBean("gestionPermisoController");
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
    
    //Eventos de la página gestion_aprob_otros.xhtml
    public void selectUsroGrupos(ValueChangeEvent event){
        int tipo = Integer.parseInt((String)event.getNewValue());
        if(tipo == 1){
            setUsrsGrupos(FacesUtil.getSelectsItem(permisoServices.findGruposAprobadores(permiso.getPermiso()), "getCodigo", "getCodigo"));
        }else{
            setUsrsGrupos(FacesUtil.getSelectsItem(permisoServices.findUsersAprobadores(permiso.getPermiso()), "getUsr", "getUsr"));
        }
        
    }
    
    public void removeAprobador(String aprobador){
       permiso.getAprobadoresAdicionales().remove(aprobador);
    }
    
    public void agregarAprobador(){
        if(!permiso.getAprobadoresAdicionales().contains(usrGrupo)){
            permiso.getAprobadoresAdicionales().add(usrGrupo);
        }
    }

    public String aprobar(){
        try {
            permisoServices.aprobarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso aprobado");
            FacesUtil.restartBean("gestionPermisoController");
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

    public String noAprobar(){
        try {
            permisoServices.noAprobarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso no aprobado");
            FacesUtil.restartBean("gestionPermisoController");
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
    
    //eventos de la página gestion_ejecutar.xhtml

    public String terminar(){
        try {
            permisoServices.terminarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso terminado");
            FacesUtil.restartBean("gestionPermisoController");
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

    public String cancelar(){
        try {
            permisoServices.cancelarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
            FacesUtil.restartBean("gestionPermisoController");
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

    public String finalizar(){
        try {
            permisoServices.finalizarPermiso(getPermiso());
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso cancelado");
            FacesUtil.restartBean("gestionPermisoController");
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
     * @return the peligro
     */
    public Peligro getPeligro() {
        return peligro;
    }

    /**
     * @param peligro the peligro to set
     */
    public void setPeligro(Peligro peligro) {
        this.peligro = peligro;
    }
    /**
     * @return the control
     */
    public String getControl() {
        return control;
    }

    /**
     * @param control the control to set
     */
    public void setControl(String control) {
        this.control = control;
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
     * @return the usrsGrupos
     */
    public List<SelectItem> getUsrsGrupos() {
        return usrsGrupos;
    }

    /**
     * @param usrsGrupos the usrsGrupos to set
     */
    public void setUsrsGrupos(List<SelectItem> usrsGrupos) {
        this.usrsGrupos = usrsGrupos;
    }

    /**
     * @return the usrGrupo
     */
    public String getUsrGrupo() {
        return usrGrupo;
    }

    /**
     * @param usrGrupo the usrGrupo to set
     */
    public void setUsrGrupo(String usrGrupo) {
        this.usrGrupo = usrGrupo;
    }

    /**
     * @return the certificados
     */
    public List<SelectItem> getCertificados() {
        return certificados;
    }

    /**
     * @return the idCertificado
     */
    public int getIdCertificado() {
        return idCertificado;
    }

    /**
     * @param idCertificado the idCertificado to set
     */
    public void setIdCertificado(int idCertificado) {
        this.idCertificado = idCertificado;
    }

    /**
     * @return the checklistDown
     */
    public StreamedContent getChecklistDown() {
        return checklistDown;
    }

  

    

}
