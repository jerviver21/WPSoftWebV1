package com.vbrothers.herramientas.managed;

import com.vi.comun.exceptions.ParametroException;
import com.vbrothers.herramientas.services.CertificadosServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Certificado;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="certificadoController")
@SessionScoped
public class CertificadoController {
    ServiceLocator locator;
    @EJB
    CertificadosServicesLocal certificadoServices;
    
    private UploadedFile checklist;
    private StreamedContent checklistDown;
    private Certificado certificado;
    private List<Certificado> certificados;
    
    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        certificado = new Certificado();
        certificados = certificadoServices.findAll();
    }
    
    
    public void cargarChecklist(FileUploadEvent event){
        try {
            checklist = event.getFile();
            certificado.setDatosArchivo(checklist.getInputstream());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el archivo");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        } 
    }
    
    public void descargarChecklist(Certificado cert){
        try {
            FileInputStream stream = new FileInputStream(cert.getRutaCheckList());
            String separador = File.separator.equals("/")?"/":"\\\\";
            checklistDown = new DefaultStreamedContent(stream, "application/pdf",
                    cert.getRutaCheckList().replaceAll(".*"+separador+"(.*)", "$1"));
            System.out.println("Ruta: "+cert.getRutaCheckList()+ " - Nombre: "+cert.getRutaCheckList().replaceAll(".*"+separador+"(.*)", "$1"));
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "El archivo a descargar no existe!");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String guardarCertificado(){
        try {
            if(certificado.getNombre() == null || certificado.getDatosArchivo() == null){
                FacesUtil.addMessage(FacesUtil.ERROR, "Diligencie el nombre y cargue el check list en formato pdf");
                return null;
            }
            certificadoServices.guardarCertificado(certificado);
            FacesUtil.addMessage(FacesUtil.INFO, "Check List cargado con exito");
            certificado = new Certificado();
            certificados = certificadoServices.findAll();
            locator.restartCache();
        }catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el certificado");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        
        return null;
    }
    
    public void borrarCertificado(Certificado cert){
        try {
            certificadoServices.borrarCertificado(cert);
            FacesUtil.addMessage(FacesUtil.INFO, "Certificado borrado con exito");
            certificados.remove(cert);
            locator.restartCache();
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el certificado");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
    

    /**
     * @return the checklist
     */
    public UploadedFile getChecklist() {
        return checklist;
    }

    /**
     * @param checklist the checklist to set
     */
    public void setChecklist(UploadedFile checklist) {
        this.checklist = checklist;
    }

    /**
     * @return the checklistDown
     */
    public StreamedContent getChecklistDown() {
        return checklistDown;
    }

    /**
     * @param checklistDown the checklistDown to set
     */
    public void setChecklistDown(StreamedContent checklistDown) {
        this.checklistDown = checklistDown;
    }

    /**
     * @return the certificado
     */
    public Certificado getCertificado() {
        return certificado;
    }

    /**
     * @param certificado the certificado to set
     */
    public void setCertificado(Certificado certificado) {
        this.certificado = certificado;
    }

    /**
     * @return the certificados
     */
    public List<Certificado> getCertificados() {
        return certificados;
    }

    /**
     * @param certificados the certificados to set
     */
    public void setCertificados(List<Certificado> certificados) {
        this.certificados = certificados;
    }
    
}
