
package com.vbrothers.usuarios.managed;

import com.vi.comun.services.CommonServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.services.PermisoTimerService;
import com.vbrothers.util.EtapaPermiso;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ValueChangeEvent;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="applicationController" ,eager=true)
@ApplicationScoped
public class ApplicationController {
    ServiceLocator locator;
    
    //Roles que condicionan los componentes de la vista
    private String ROL_ADMIN;
    private String ROL_GERENTE;
    private String ROL_EMPLEADO;
    private String ROL_CONTRATISTA;
    private String ROL_CALIDAD;
    private String ROL_SEGURIDAD;
    private String ROL_JEFE_SECTOR;
    private String ROL_VALIDADOR;
    private String ROL_RECEPCIONISTA;
    private String ROL_INGENIERIA;
    private String ROL_MASTER;
    
    //Etapas del permiso de trabajo
    private EtapaPermiso DILIGENCIAR;
    private EtapaPermiso APROBAR;
    private EtapaPermiso TERMINAR;
    private EtapaPermiso CANCELAR;
    private EtapaPermiso FINALIZAR;
    
    //Define la plantilla y layout de la aplicacion
    private String p1 = "../plantilla1.xhtml";
    private String p2 = "../plantilla2.xhtml";
    private String p3 = "../plantilla3.xhtml";
    private String plantilla = "../plantilla2.xhtml";
    private String planIndex = "plantilla2.xhtml";
    private Map<String, String> themes; 
    private String theme = "casablanca";
    

    @EJB
    private CommonServicesLocal commonServices;
    
    @EJB
    private PermisoTimerService timerServices;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        System.out.println("--> Iniciando application controller <--");
        commonServices.updateEstructuraMenus();
        timerServices.initTimer();
        
        
        
        ROL_ADMIN = locator.getParameter("rolAdmin");
        ROL_MASTER = locator.getParameter("rolMaster");
        ROL_CONTRATISTA = locator.getParameter("rolContratista");
        ROL_VALIDADOR = locator.getParameter("rolValidador");
        ROL_JEFE_SECTOR = locator.getParameter("rolAutArea");
        ROL_GERENTE = locator.getParameter("rolGerente");
        
        TERMINAR = EtapaPermiso.TERMINAR;
        CANCELAR = EtapaPermiso.CANCELAR;
        FINALIZAR = EtapaPermiso.FINALIZAR;
        DILIGENCIAR = EtapaPermiso.DILIGENCIAR;
        APROBAR = EtapaPermiso.APROBAR;
        
        themes = new TreeMap<String, String>();  
        themes.put("Afterdark", "afterdark");  
        themes.put("Casablanca", "casablanca");  
        themes.put("South-Street", "south-street");  
        themes.put("UI-Lightness", "ui-lightness");  
    }
    
    public String cambiarLookandfeel(){
        if(plantilla.equalsIgnoreCase(p1)){
            planIndex = "plantilla1.xhtml";
        }else if(plantilla.equalsIgnoreCase(p2)){
            planIndex = "plantilla2.xhtml";
        }else{
            planIndex = "plantilla3.xhtml";
        }
        return null;
    }
    
    public void cambiarTema(ValueChangeEvent event){
        System.out.println("Nuevo tema: "+event.getNewValue());
        theme = (String)event.getNewValue();
    }
    
    public String preCargar(){
        return null;
    }

    /**
     * @return the ROL_ADMIN
     */
    public String getROL_ADMIN() {
        return ROL_ADMIN;
    }

    /**
     * @param ROL_ADMIN the ROL_ADMIN to set
     */
    public void setROL_ADMIN(String ROL_ADMIN) {
        this.ROL_ADMIN = ROL_ADMIN;
    }

    /**
     * @return the ROL_GERENTE
     */
    public String getROL_GERENTE() {
        return ROL_GERENTE;
    }

    /**
     * @param ROL_GERENTE the ROL_GERENTE to set
     */
    public void setROL_GERENTE(String ROL_GERENTE) {
        this.ROL_GERENTE = ROL_GERENTE;
    }

    /**
     * @return the ROL_EMPLEADO
     */
    public String getROL_EMPLEADO() {
        return ROL_EMPLEADO;
    }

    /**
     * @param ROL_EMPLEADO the ROL_EMPLEADO to set
     */
    public void setROL_EMPLEADO(String ROL_EMPLEADO) {
        this.ROL_EMPLEADO = ROL_EMPLEADO;
    }

    /**
     * @return the ROL_CONTRATISTA
     */
    public String getROL_CONTRATISTA() {
        return ROL_CONTRATISTA;
    }

    /**
     * @param ROL_CONTRATISTA the ROL_CONTRATISTA to set
     */
    public void setROL_CONTRATISTA(String ROL_CONTRATISTA) {
        this.ROL_CONTRATISTA = ROL_CONTRATISTA;
    }

    /**
     * @return the ROL_CALIDAD
     */
    public String getROL_CALIDAD() {
        return ROL_CALIDAD;
    }

    /**
     * @param ROL_CALIDAD the ROL_CALIDAD to set
     */
    public void setROL_CALIDAD(String ROL_CALIDAD) {
        this.ROL_CALIDAD = ROL_CALIDAD;
    }

    /**
     * @return the ROL_SEGURIDAD
     */
    public String getROL_SEGURIDAD() {
        return ROL_SEGURIDAD;
    }

    /**
     * @param ROL_SEGURIDAD the ROL_SEGURIDAD to set
     */
    public void setROL_SEGURIDAD(String ROL_SEGURIDAD) {
        this.ROL_SEGURIDAD = ROL_SEGURIDAD;
    }

    /**
     * @return the ROL_JEFE_SECTOR
     */
    public String getROL_JEFE_SECTOR() {
        return ROL_JEFE_SECTOR;
    }

    /**
     * @param ROL_JEFE_SECTOR the ROL_JEFE_SECTOR to set
     */
    public void setROL_JEFE_SECTOR(String ROL_JEFE_SECTOR) {
        this.ROL_JEFE_SECTOR = ROL_JEFE_SECTOR;
    }

    /**
     * @return the ROL_VALIDADOR
     */
    public String getROL_VALIDADOR() {
        return ROL_VALIDADOR;
    }

    /**
     * @param ROL_VALIDADOR the ROL_VALIDADOR to set
     */
    public void setROL_VALIDADOR(String ROL_VALIDADOR) {
        this.ROL_VALIDADOR = ROL_VALIDADOR;
    }

    /**
     * @return the ROL_RECEPCIONISTA
     */
    public String getROL_RECEPCIONISTA() {
        return ROL_RECEPCIONISTA;
    }

    /**
     * @param ROL_RECEPCIONISTA the ROL_RECEPCIONISTA to set
     */
    public void setROL_RECEPCIONISTA(String ROL_RECEPCIONISTA) {
        this.ROL_RECEPCIONISTA = ROL_RECEPCIONISTA;
    }

    /**
     * @return the ROL_INGENIERIA
     */
    public String getROL_INGENIERIA() {
        return ROL_INGENIERIA;
    }

    /**
     * @param ROL_INGENIERIA the ROL_INGENIERIA to set
     */
    public void setROL_INGENIERIA(String ROL_INGENIERIA) {
        this.ROL_INGENIERIA = ROL_INGENIERIA;
    }

    /**
     * @return the ROL_MASTER
     */
    public String getROL_MASTER() {
        return ROL_MASTER;
    }

    /**
     * @param ROL_MASTER the ROL_MASTER to set
     */
    public void setROL_MASTER(String ROL_MASTER) {
        this.ROL_MASTER = ROL_MASTER;
    }

    /**
     * @return the TERMINAR
     */
    public EtapaPermiso getTERMINAR() {
        return TERMINAR;
    }

    /**
     * @return the CANCELAR
     */
    public EtapaPermiso getCANCELAR() {
        return CANCELAR;
    }

    /**
     * @return the FINALIZAR
     */
    public EtapaPermiso getFINALIZAR() {
        return FINALIZAR;
    }

    /**
     * @return the DILIGENCIAR
     */
    public EtapaPermiso getDILIGENCIAR() {
        return DILIGENCIAR;
    }

    /**
     * @return the APROBAR
     */
    public EtapaPermiso getAPROBAR() {
        return APROBAR;
    }

    /**
     * @return the plantilla
     */
    public String getPlantilla() {
        return plantilla;
    }

    /**
     * @param plantilla the plantilla to set
     */
    public void setPlantilla(String plantilla) {
        this.plantilla = plantilla;
    }

    /**
     * @return the p1
     */
    public String getP1() {
        return p1;
    }

    /**
     * @param p1 the p1 to set
     */
    public void setP1(String p1) {
        this.p1 = p1;
    }

    /**
     * @return the p2
     */
    public String getP2() {
        return p2;
    }

    /**
     * @param p2 the p2 to set
     */
    public void setP2(String p2) {
        this.p2 = p2;
    }

    /**
     * @return the p3
     */
    public String getP3() {
        return p3;
    }

    /**
     * @param p3 the p3 to set
     */
    public void setP3(String p3) {
        this.p3 = p3;
    }

    /**
     * @return the planIndex
     */
    public String getPlanIndex() {
        return planIndex;
    }

    /**
     * @param planIndex the planIndex to set
     */
    public void setPlanIndex(String planIndex) {
        this.planIndex = planIndex;
    }

    /**
     * @return the themes
     */
    public Map<String, String> getThemes() {
        return themes;
    }

    /**
     * @param themes the themes to set
     */
    public void setThemes(Map<String, String> themes) {
        this.themes = themes;
    }

    /**
     * @return the theme
     */
    public String getTheme() {
        return theme;
    }

    /**
     * @param theme the theme to set
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

}
