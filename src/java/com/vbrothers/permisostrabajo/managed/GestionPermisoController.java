package com.vbrothers.permisostrabajo.managed;

import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.herramientas.services.CertificadosServicesLocal;
import com.vbrothers.herramientas.services.PeligrosServicesLocal;
import com.vbrothers.herramientas.services.SectoresServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Certificado;
import com.vbrothers.permisostrabajo.dominio.CertificadosTrabajo;
import com.vbrothers.permisostrabajo.dominio.Control;
import com.vbrothers.permisostrabajo.dominio.ControlesPeligroTarea;
import com.vbrothers.permisostrabajo.dominio.NotasPermiso;
import com.vbrothers.permisostrabajo.dominio.Peligro;
import com.vbrothers.permisostrabajo.dominio.PeligrosTarea;
import com.vbrothers.permisostrabajo.dominio.PermisoTrabajo;
import com.vbrothers.permisostrabajo.dominio.RiesgosPeligroTarea;
import com.vbrothers.permisostrabajo.dominio.Sector;
import com.vbrothers.permisostrabajo.dominio.Tarea;
import com.vbrothers.permisostrabajo.dominio.TrazabilidadPermiso;
import com.vbrothers.permisostrabajo.services.PermisoServicesLocal;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.EtapaPermiso;
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
    private PermisoTrabajo permiso;
    
    //Permite listar los permisos de trabajo pendientes
    private List<PermisoTrabajo> permisosPendientes;
    
    //Permiten diligenciar los datos generales del permiso
    private List<SelectItem> sectores;
    private List<SelectItem> certificados;
    private List<SelectItem> disciplinas;
    private int idSector;//Para poder agregar varios sectores afectados
    private int idCertificado;//Para poder agregar varios certificados
    private String nota;//Permite agregar varias notas al permiso de trabajo.
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
    String PAG_NOTAS = "/permisostrabajo/gestion_notas.xhtml";
    
    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        sesion = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        
        permisosPendientes = permisoServices.findPermisosPendientes(sesion.getUsuario());
       
        certificados = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_CERTIFICADO));
        disciplinas = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_DISCIPLINA));
        peligros = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_PELIGRO));
        
        permiso = new PermisoTrabajo(sesion.getUsuario());
        peligro = new Peligro();
        tarea = new Tarea();
    }
    
    //Métodos para el manejo de eventos de la página mis_permisos.xhtml
    public String consultarTrazabilidad(PermisoTrabajo r){
        permiso = permisoServices.findPermisoForGestion(r.getId());
        permiso.setUsuario(sesion.getUsuario());
        sectores = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_ID_SECTOR));
        for(SelectItem item: sectores){
            int idItem = (Integer)item.getValue();
            if(permiso.getSector().getId() == idItem){
                sectores.remove(item);
                break;
            }
        }
        traz = permisoServices.findTrazabilidadPermiso(r);
        usrsGrupos = FacesUtil.getSelectsItem(permisoServices.findGruposAprobadores(permiso), "getCodigo", "getCodigo");
        return PAG_TRAZABILIDAD;
    }
    
    //Métodos para el manejo de eventos de la página gestion_trazabilidad.xhtml
    public String gestionarPermiso(){
        String rolValidador = locator.getParameter("rolValidador");
        if(rolValidador == null){
            FacesUtil.addMessage(FacesUtil.ERROR, "No existe el parámetro rolValidador, debe ser ingresado (Consulte con el administrador)");
            return null;
        }
        if(sesion.getUsuario().getRolesUsr().contains(rolValidador)){
            return PAG_NOTAS;
        }
        if(permiso.getEtapa() == EtapaPermiso.TERMINAR || permiso.getEtapa() == EtapaPermiso.CANCELAR || permiso.getEtapa() == EtapaPermiso.FINALIZAR ){
            return PAG_EJECUCION;
        }
        guardarGestionTareas();//Permite ordenar 6 tareas por defecto en la página
        return PAG_DATOS;
    }
    
    //Métodos para el control de eventos de la página gestion_datos.xhtml
    public void borrarSectorAfectado(Sector sector){
       permiso.getSectoresAfectados().remove(sector);
    }

    public void agregarSectorAfectado(){
        Sector r = sectoresServices.find(idSector);
        if(!permiso.getSectoresAfectados().contains(r)){
            permiso.getSectoresAfectados().add(r);
        }
    }

    public void addTarea(){
        Tarea t = new Tarea();
        t.setConsecutivo(permiso.getTareas().size()+1);
        t.setPermiso(permiso);
        permiso.getTareas().add(t);
    }

    public void borrarTarea(Tarea tarea){
        try {
            for (int i = 0; i < permiso.getTareas().size(); i++) {
                if(tarea.getConsecutivo() == permiso.getTareas().get(i).getConsecutivo()){
                    permiso.getTareas().remove(i);
                    break;
                }
            }
            if(tarea.getId() != null && tarea.getId() != 0){
                permisoServices.borrarTarea(tarea);
            }
            guardarGestionTareas();
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
        Tarea t = permiso.getTareas().get(0);
        if(t.getDatos() == null || t.getDatos().equals("")){
            FacesUtil.addMessage(FacesUtil.ERROR, "Debe agregar al menos un paso para diligenciar el permiso");
            return null;
        }
        String SIG_PAGINA = PAG_CONSIDERACIONES;
        if(permiso.getEtapa() == EtapaPermiso.APROBAR){
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
        PeligrosTarea pelTarea = peligroServices.findPeligroTarea(getPeligro(), getTarea());
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
                break;
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
                 break;
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
                 break;
            }
        }
    }
    
    public String guardarGestionTareas(){
        try {
            habMasTareas = true;
            removerTareasNoGestionadas();
            permisoServices.guardarGestion(permiso);
            cargar6TareasDefecto();
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar la gestion del permiso de trabajo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return PAG_DATOS;
    }

    //Eventos de la página gestion_consideraciones.xhtml
   
    public void agregarCertificado(){
        Certificado r = certificadosServices.find(idCertificado);
        CertificadosTrabajo ct = new CertificadosTrabajo();
        ct.setPermiso(permiso);
        ct.setCertificado(r);
        if(!permiso.getCertificados().contains(ct)){
            permiso.getCertificados().add(ct);
        }
    }
    
    
    public void borrarCertificado(CertificadosTrabajo cert){
       permiso.getCertificados().remove(cert);
    }
    
    public void descargarChecklist(CertificadosTrabajo cert){
        try {
            FileInputStream stream = new FileInputStream(cert.getCertificado().getRutaCheckList());
            String separador = File.separator.equals("/")?"/":"\\";
            checklistDown = new DefaultStreamedContent(stream, "application/pdf",
                    cert.getCertificado().getRutaCheckList().replaceAll(".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String solicitarAprobacion(){
        try {
            removerTareasNoGestionadas();
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
            usrsGrupos = FacesUtil.getSelectsItem(permisoServices.findGruposAprobadores(permiso), "getCodigo", "getCodigo");
        }else{
            usrsGrupos = FacesUtil.getSelectsItem(permisoServices.findUsersAprobadores(permiso), "getUsr", "getUsr");
        }
    }
    
    public void removeAprobador(String aprobador){
       permiso.getOtrosAprobadores().remove(aprobador);
    }
    
    public void agregarAprobador(){
        if(!permiso.getOtrosAprobadores().contains(usrGrupo)){
            permiso.getOtrosAprobadores().add(usrGrupo);
        }
    }

    public String aprobar(){
        try {
            removerTareasNoGestionadas();
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
            removerTareasNoGestionadas();
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
    
    //Eventos de la página gestion_notas.xhtml
    public void cambiarEstado(ValueChangeEvent event){
        int estado = (int) (Integer)event.getNewValue();
        if(permiso.getEstadoPermiso().getId() != estado){
            permisoServices.cambiarEstado(permiso, estado);
        }
        permisosPendientes.remove(permiso);
        permisosPendientes.add(permiso);
    }
    
    public String agregarNota(){
        NotasPermiso n = new NotasPermiso();
        n.setNota(nota);
        n.setPermiso(permiso);
        n.setUsr(sesion.getUsuario().getUsr());
        permiso.getNotas().add(n);
        return null;
    }
    
    public void borrarNota(NotasPermiso nota){
        permisoServices.borrarNota(nota);
        permiso.getNotas().remove(nota);
    }
    
    public String guardarValidacion(){
        try {
            permisoServices.actualizarPermiso(permiso);
            FacesUtil.addMessage(FacesUtil.INFO, "Permiso actualizado con exito");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar validacion");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }
        return PAG_PERMISOS;
    }
    
    //Métodos para administración de las 6 tareas visibles por defecto
    public void removerTareasNoGestionadas(){
        for (int i = 0; i < permiso.getTareas().size(); i++) {
            Tarea t = permiso.getTareas().get(i);
            if(t.getDatos() == null || t.getDatos().equals("")){
                permiso.getTareas().remove(i);
                i--;
            }
        }
        for (int i = 0; i < permiso.getTareas().size(); i++) {
            Tarea t = permiso.getTareas().get(i);
            t.setConsecutivo(i+1);
        }
    }
    
    public void cargar6TareasDefecto(){
        while(permiso.getTareas().size() < 6){
            Tarea t = new Tarea();
            t.setConsecutivo(permiso.getTareas().size()+1);
            t.setPermiso(permiso);
            permiso.getTareas().add(t);
            habMasTareas = false;
        }
    }

    /**
     * @return the permisoPendiente
     */
    public PermisoTrabajo getPermiso() {
        return permiso;
    }

    /**
     * @param permisoPendiente the permisoPendiente to set
     */
    public void setPermiso(PermisoTrabajo permisoPendiente) {
        this.permiso = permisoPendiente;
    }

    /**
     * @return the permisosPendientes
     */
    public List<PermisoTrabajo> getPermisosPendientes() {
        return permisosPendientes;
    }

    /**
     * @return the sectores
     */
    public List<SelectItem> getSectores() {
        return sectores;
    }

    /**
     * @return the disciplinas
     */
    public List<SelectItem> getDisciplinas() {
        return disciplinas;
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

    /**
     * @return the nota
     */
    public String getNota() {
        return nota;
    }

    /**
     * @param nota the nota to set
     */
    public void setNota(String nota) {
        this.nota = nota;
    }
}
