
package com.vbrothers.contratistas.managed;


import com.vbrothers.exceptions.EmpActivoOtroContException;
import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vi.comun.exceptions.ParametroException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vbrothers.permisostrabajo.services.EmpleadoServicesLocal;
import com.vbrothers.usuarios.managed.SessionController;
import com.vi.usuarios.services.UsuariosServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import com.vbrothers.util.SpringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
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
    @EJB
    UsuariosServicesLocal usrService;

    //Para gestion de datos del empleado
    private Empleado empleado;
    private List<SelectItem> contratistas;
    private List<SelectItem> epss;
    private String contrasena = "" ;
    private boolean certMedicoCargado = false;
    private boolean certTACargado = false;
    private boolean asignarClave = true;

    //Para la carga de los certificados médicos y de trabajo en alturas!
    private UploadedFile certificadoMedico;
    private StreamedContent certificadoMedicoDown;
    private UploadedFile certificadoTrabajoAlturas;
    private StreamedContent trabajoAlturasDown;
    
    //Permite mostrar los empleados de un contratista, en la pagina empleados.xhtml
    private List<Empleado> empleadosContratista;
    
    //Para manejar la session
    SessionController sessionControler;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        empleado = new Empleado();
        Contratista contratista = new Contratista(-1l);
        sessionControler = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        List<Contratista> contratistasList = new ArrayList<Contratista>();
        if(sessionControler.getUsuario().getRolesUsr().contains(locator.getParameter("rolContratista"))){
            contratista = contratistaService.findByUser(sessionControler.getUsuario().getUsr());
            contratistasList.add(contratista);
            empleadosContratista = empleadoService.findEmpleadosXContratita(contratista.getId());
        }else{
            contratistasList = contratistaService.findAll();
            empleadosContratista = empleadoService.findEmpleadosXContratita(-1l);
        }
        empleado.setContratista(contratista);
        setContratistas(FacesUtil.getSelectsItem(contratistasList));
        epss = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_COD_EPS));
        certMedicoCargado = false;
        certTACargado = false;
        asignarClave = true;
    }
    
    public String crearNuevo(){
        init();
        return "/contratistas/empleado.xhtml";
    }
    
    public String createEmpleado(){
        try {
            /*if(!empleado.getCertificadoMedico() && empleado.getCertMedico() == null){
                FacesUtil.addMessage(FacesUtil.ERROR, "Es necesario cargar el certificado médico del empleado");
                return null;
            }*/
            if(empleado.getNumId() == 0l){
                FacesUtil.addMessage(FacesUtil.ERROR, "Ingrese un número de identificación valido");
                return null;
            }
            empleado.setPwd(contrasena == null || contrasena.equals("")?empleado.getPwd():SpringUtils.getPasswordEncoder().encodePassword(contrasena, null));
            if(empleado.getContratista().getId() == -1){//Por convencion si el empleado es de planta, selecciona -1
                empleado.setContratista(null);
            }
            empleadoService.guardar(empleado);
            FacesUtil.addMessage(FacesUtil.INFO, "Empleado guardado con exito!");
            crearNuevo();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el empleado");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        init();
        return "/contratistas/empleados.xhtml";
    }
    
    public void cargarCertMedico(FileUploadEvent event){
        try {
            empleado.setCertMedico(event.getFile().getInputstream());
            empleado.setExtCM(event.getFile().getFileName().replaceAll( ".*\\.(.*)", "$1"));
            certMedicoCargado = true;
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al cargar el archivo");
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
            certificadoMedicoDown = new DefaultStreamedContent(stream, "application/"+empleado.getRutaCertCm().replaceAll( ".*\\.(.*)", "$1")
                    ,empleado.getRutaCertCm().replaceAll(
                    ".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
    }
    
    public void cargarCertTA(FileUploadEvent event){
        try {
            empleado.setCertTrabAlt(event.getFile().getInputstream());
            empleado.setExtCTA(event.getFile().getFileName().replaceAll( ".*\\.(.*)", "$1"));
            certTACargado = true;
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al cargar el archivo");
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
            trabajoAlturasDown = new DefaultStreamedContent(stream, "application/"+empleado.getRutaCertTA().replaceAll( ".*\\.(.*)", "$1"),
                    empleado.getRutaCertTA().replaceAll(".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public void dispUsuario(){
        FacesContext fc = FacesContext.getCurrentInstance();
        if(!usrService.isUsuarioDisponible(empleado.getUsuario())){
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "El nombre de usuario ya existe, seleccione otro", "El nombre de usuario ya existe, seleccione otro");
            fc.addMessage(":form1:email", fm);
        }
    }
    
    
    public String consultarEmpleado(Empleado emp){
        empleado = empleadoService.find(emp.getId());
        if(empleado.getContratista() == null){
            empleado.setContratista(new Contratista(-1l));
        }
        asignarClave = false;
        return "/contratistas/empleado.xhtml";
    }
    
    public void cambiarClaveEmp(){
        asignarClave = true;
    }
    
    public void eliminarCM(){
        empleado.setCertificadoMedico(false);
        empleado.setRutaCertCm(null);
    }
    
    public void eliminarCA(){
        empleado.setTrabajoAlturas(false);
        empleado.setRutaCertTA(null);
    }

    
    public void cargarEmpleadosContratista() {    
        empleadosContratista = empleadoService.findEmpleadosXContratita(empleado.getContratista().getId());
    }
    
    
    public void activarEmpleado(ValueChangeEvent evento){
        try {   
            Empleado emp  = (Empleado) evento.getComponent().getAttributes().get("empleado");
            emp.setActivo((Boolean)evento.getNewValue());
            empleadoService.activarEmpleado(emp, sessionControler.getUsuario().getUsr());
        } catch( EmpActivoOtroContException e){
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al tratar de activar los empleados ");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public void betarEmpleado(ValueChangeEvent evento){
        try {         
            Empleado emp  = (Empleado) evento.getComponent().getAttributes().get("empleado");
            emp.setBetado((Boolean)evento.getNewValue());
            empleadoService.betarEmpleado(emp, sessionControler.getUsuario().getUsr());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al tratar de activar los empleados ");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void cambiarFechaEmpleado(ValueChangeEvent evento){
        try {
            Empleado emp  = (Empleado) evento.getComponent().getAttributes().get("empleado");
            emp.setFechaInduccion((Date)evento.getNewValue());
            empleadoService.cambiarFechaInduccion(emp, sessionControler.getUsuario().getUsr());
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

    /**
     * @return the epss
     */
    public List<SelectItem> getEpss() {
        return epss;
    }

    /**
     * @return the certMedicoCargado
     */
    public boolean isCertMedicoCargado() {
        return certMedicoCargado;
    }

    /**
     * @param certMedicoCargado the certMedicoCargado to set
     */
    public void setCertMedicoCargado(boolean certMedicoCargado) {
        this.certMedicoCargado = certMedicoCargado;
    }

    /**
     * @return the certTACargado
     */
    public boolean isCertTACargado() {
        return certTACargado;
    }

    /**
     * @param certTACargado the certTACargado to set
     */
    public void setCertTACargado(boolean certTACargado) {
        this.certTACargado = certTACargado;
    }

    /**
     * @return the asignarClave
     */
    public boolean isAsignarClave() {
        return asignarClave;
    }

    /**
     * @param asignarClave the asignarClave to set
     */
    public void setAsignarClave(boolean asignarClave) {
        this.asignarClave = asignarClave;
    }
    
}
