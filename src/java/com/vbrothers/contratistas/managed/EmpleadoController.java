
package com.vbrothers.contratistas.managed;

import com.vbrothers.common.exceptions.EmpActivoOtroContException;
import com.vbrothers.common.exceptions.LlaveDuplicadaException;
import com.vbrothers.common.exceptions.ParametroException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import com.vbrothers.util.SpringUtils;
import java.io.File;
import java.io.FileInputStream;
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
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="empleadoController")
@SessionScoped
public class EmpleadoController {
    ServiceLocator locator;
    @EJB
    ContratistaServicesLocal contratistaService;
    @EJB
    EmpleadoServicesLocal empleadoService;

    
    private Empleado empleado;
    private List<SelectItem> contratistas;
    private String contrasena = "" ;
    private long numId;

    
    private UploadedFile certificadoMedico;
    private StreamedContent certificadoMedicoDown;
    private UploadedFile certificadoTrabajoAlturas;
    private StreamedContent trabajoAlturasDown;
    private List<UploadedFile> otrosCertificados;
    
    private List<Empleado> empleadosContratista;
    
    
    //Constantes
    private final String nombreArchCM = "CM";
    private final String nombreArchTA = "TA";


    
    
    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        empleado = new Empleado();
        Contratista contratista = new Contratista(-1l);
        SessionController sessionControler = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        Map contratistasMap = null;
        if(sessionControler.getRoles().contains(locator.getParameter("rolContratista")) &&
                !sessionControler.getRoles().contains(locator.getParameter("rolMaster"))){
            contratista = contratistaService.findByUser(sessionControler.getUsuario().getUsr());
            contratistasMap = locator.getConditionalRefTable(ServiceLocator.CONTRATISTAS_X_ID, contratista.getUsuario(), null, null);
            empleadosContratista = empleadoService.findEmpleadosXContratita(contratista.getId());
        }else{
            contratistasMap = locator.getConditionalRefTable(ServiceLocator.CONTRATISTAS_X_ID, null, null, null);
            empleadosContratista = empleadoService.findEmpleadosXContratita(-2l);
        }
        empleado.setContratista(contratista);
        setContratistas(FacesUtil.getSelectsItem(contratistasMap));
        
    }
    
    public String nuevoEmpleado(){
        init();
        return "/contratistas/empleado.xhtml";
    }
    
    public String createEmpleado(){
        
        try {
            if(!empleado.getCertificadoMedico()){
                FacesUtil.addMessage(FacesUtil.ERROR, "Es necesario cargar el certificado médico del empleado");
                return null;
            }
            if(empleado.getNumId() == 0){
                FacesUtil.addMessage(FacesUtil.ERROR, "Ingrese un número de identificación valido");
                return null;
            }
            contrasena = contrasena.equals("")?empleado.getPwd():SpringUtils.getPasswordEncoder().encodePassword(contrasena, null);
            empleado.setPwd(contrasena);
            empleadoService.guardar(empleado);
            FacesUtil.addMessage(FacesUtil.INFO, "Empleado guardado con exito!");
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el empleado");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        init();
        return "/contratistas/empleados.xhtml";
    }
    
    public void cargarCertMedico(FileUploadEvent event){
        try {
            if(empleado.getNumId() == 0){
                FacesUtil.addMessage(FacesUtil.ERROR, "Ingrese primero todos los datos del empleado!");
                return;
            }
            String rutaCert = empleadoService.cargarCertificado(nombreArchCM+"."+event.getFile().getFileName().replaceAll(
                    ".*\\.(.*)", "$1"), empleado.getNumId(), event.getFile().getInputstream());
            empleado.setRutaCertCm(rutaCert);
            empleado.setCertificadoMedico(true);
        } catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el archivo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        } 
    }
    
    public void descargarCertMedico(){
        try {
            if(empleado.getRutaCertCm() == null){
                return;
            }
            FileInputStream stream = new FileInputStream(empleado.getRutaCertCm());
            String separador = File.separator.equals("/")?"/":"\\";
            certificadoMedicoDown = new DefaultStreamedContent(stream, "application/pdf",empleado.getRutaCertCm().replaceAll(
                    ".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
    }
    
    public void cargarCertTA(FileUploadEvent event){
        try {
            if(empleado.getNumId() == 0){
                FacesUtil.addMessage(FacesUtil.ERROR, "Ingrese primero todos los datos del empleado!");
                return;
            }
            String rutaCert = empleadoService.cargarCertificado(nombreArchTA+"."+event.getFile().getFileName().replaceAll(
                    ".*\\.(.*)", "$1"), empleado.getNumId(), event.getFile().getInputstream());
            empleado.setRutaCertTA(rutaCert);
            empleado.setTrabajoAlturas(true);
        } catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el archivo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        } 
    }
    
    public void descargarCertTA(){
        try {
            if(empleado.getRutaCertTA() == null){
                return;
            }
            FileInputStream stream = new FileInputStream(empleado.getRutaCertTA());
            String separador = File.separator.equals("/")?"/":"\\";
            certificadoMedicoDown = new DefaultStreamedContent(stream, "application/pdf",
                    empleado.getRutaCertTA().replaceAll(".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
    public void mostrarEmpleadoContratista(ActionEvent event){
        empleado  = (Empleado) event.getComponent().getAttributes().get("empleadoCambiar");
        empleado = empleadoService.find(empleado.getId());
        if(empleado.getContratista() == null){
            empleado.setContratista(new Contratista(-1l));
        }
    }
    
    public String navEmpleado(){
        return "/contratistas/empleado.xhtml";
    }
    
    public void cargarEmpleadosContratista() {    
        empleadosContratista = empleadoService.findEmpleadosXContratita(empleado.getContratista().getId());
    }
    
    
    public void activarEmpleado(ValueChangeEvent evento){
        try {         
            Empleado emp  = (Empleado) evento.getComponent().getAttributes().get("empleado");
            emp.setActivo((Boolean)evento.getNewValue());
            empleadoService.activarEmpleado(emp);
        } catch( EmpActivoOtroContException e){
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al tratar de activar los empleados ");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void cambiarFechaEmpleado(ValueChangeEvent evento){
        try {
            Empleado emp  = (Empleado) evento.getComponent().getAttributes().get("empleado");
            emp.setFechaInduccion((Date)evento.getNewValue());
            empleadoService.edit(emp);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al tratar de cambiar la fecha al empleado");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }


    /**
     * @return the empleado
     */
    public Empleado getEmpleado() {
        return empleado;
    }

    /**
     * @param empleado the empleado to set
     */
    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
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
     * @return the certificadoMedico
     */
    public UploadedFile getCertificadoMedico() {
        return certificadoMedico;
    }

    /**
     * @param certificadoMedico the certificadoMedico to set
     */
    public void setCertificadoMedico(UploadedFile certificadoMedico) {
        this.certificadoMedico = certificadoMedico;
    }

    /**
     * @return the certificadoTrabajoAlturas
     */
    public UploadedFile getCertificadoTrabajoAlturas() {
        return certificadoTrabajoAlturas;
    }

    /**
     * @param certificadoTrabajoAlturas the certificadoTrabajoAlturas to set
     */
    public void setCertificadoTrabajoAlturas(UploadedFile certificadoTrabajoAlturas) {
        this.certificadoTrabajoAlturas = certificadoTrabajoAlturas;
    }

    /**
     * @return the otrosCertificados
     */
    public List<UploadedFile> getOtrosCertificados() {
        return otrosCertificados;
    }

    /**
     * @param otrosCertificados the otrosCertificados to set
     */
    public void setOtrosCertificados(List<UploadedFile> otrosCertificados) {
        this.otrosCertificados = otrosCertificados;
    }

    /**
     * @return the certificadoMedicoDown
     */
    public StreamedContent getCertificadoMedicoDown() {
        return certificadoMedicoDown;
    }

    /**
     * @param certificadoMedicoDown the certificadoMedicoDown to set
     */
    public void setCertificadoMedicoDown(StreamedContent certificadoMedicoDown) {
        this.certificadoMedicoDown = certificadoMedicoDown;
    }

    /**
     * @return the trabajoAlturasDown
     */
    public StreamedContent getTrabajoAlturasDown() {
        return trabajoAlturasDown;
    }

    /**
     * @param trabajoAlturasDown the trabajoAlturasDown to set
     */
    public void setTrabajoAlturasDown(StreamedContent trabajoAlturasDown) {
        this.trabajoAlturasDown = trabajoAlturasDown;
    }

    /**
     * @return the contrasena
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * @param contrasena the contrasena to set
     */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    /**
     * @return the numId
     */
    public long getNumId() {
        return numId;
    }

    /**
     * @param numId the numId to set
     */
    public void setNumId(long numId) {
        this.numId = numId;
    }

    /**
     * @return the empleadosContratista
     */
    public List<Empleado> getEmpleadosContratista() {
        return empleadosContratista;
    }

    /**
     * @param empleadosContratista the empleadosContratista to set
     */
    public void setEmpleadosContratista(List<Empleado> empleadosContratista) {
        this.empleadosContratista = empleadosContratista;
    }
    
}
