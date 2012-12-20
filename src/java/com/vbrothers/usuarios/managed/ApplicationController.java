
package com.vbrothers.usuarios.managed;

import com.vbrothers.common.services.CommonServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

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
    
    

    @EJB
    private CommonServicesLocal commonServices;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        System.out.println("--> Iniciando application controller <--");
        commonServices.updateEstructuraMenus();
        
        
        ROL_ADMIN = locator.getParameter("rolAdmin");
        ROL_MASTER = locator.getParameter("rolMaster");
        ROL_CONTRATISTA = locator.getParameter("rolContratista");
        ROL_VALIDADOR = locator.getParameter("rolValidador");
        ROL_JEFE_SECTOR = locator.getParameter("rolAutArea");
        ROL_GERENTE = locator.getParameter("rolGerente");
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

}
