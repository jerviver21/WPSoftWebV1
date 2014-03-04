package com.vbrothers.contratistas.managed;

import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vi.comun.exceptions.ParametroException;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.permisostrabajo.dominio.Contratista;
import com.vbrothers.permisostrabajo.dominio.Empleado;
import com.vbrothers.permisostrabajo.services.ContratistaServicesLocal;
import com.vi.usuarios.services.UsuariosServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import com.vbrothers.util.SpringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="contratistaController")
@SessionScoped
public class ContratistaController {
    ServiceLocator locator;
    
    //Servicios
    @EJB
    private ContratistaServicesLocal contratistasService;
    
    @EJB
    private UsuariosServicesLocal usrService;
    
    //Atributos para la gestion de datos del contratista
    private List<Contratista> contratistas;
    private Contratista contratista;
    private String contrasena = "";
    private boolean permitirAddEmpleados = false;
    private List<SelectItem> arps;
    private boolean asignarClave = true;

    public ContratistaController() {
    }

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        contratistas = contratistasService.findAll();
        arps = FacesUtil.getSelectsItem(locator.getDataForCombo(ServiceLocator.COMB_COD_ARP));
        asignarClave = true;
    }

    public void initContratista(){
        setContratista(new Contratista(null));
        getContratista().setEmpleados(new ArrayList<Empleado>());
        permitirAddEmpleados = false;
    }    

    public String guardar(){
        try {
            if(contratista.getNumId() == 0l){
                FacesUtil.addMessage(FacesUtil.ERROR, "Ingrese un número de identificación valido");
                return null;
            }
 
            contrasena = contrasena == null || contrasena.equals("")?contratista.getPwd():SpringUtils.getPasswordEncoder().encodePassword(contrasena, null);
            contratista.setPwd(contrasena);
            contratistasService.guardar(getContratista());
            FacesUtil.addMessage(FacesUtil.INFO,"Contratista guardado con exito!!");
            permitirAddEmpleados = true;
            crearNuevo();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (ParametroException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR,"Error al guardar el contratista");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        initContratista();
        init();
        return "/contratistas/contratistas.xhtml";
    }
    
    
    //Eventos de la página contratistas.xhtml
    public String crearNuevo(){
        initContratista();
        permitirAddEmpleados = false;
        return "/contratistas/contratista.xhtml";
    }

    public String consultarContratista(Contratista c){
         contratista  = c;
         contratista = contratistasService.find(contratista.getId());
         permitirAddEmpleados = true;
         asignarClave = false;
         return "/contratistas/contratista.xhtml";
    }
    
    public void dispUsuario(){
        FacesContext fc = FacesContext.getCurrentInstance();
        if(!usrService.isUsuarioDisponible(contratista.getUsuario())){
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, "El nombre de usuario ya existe, seleccione otro", "El nombre de usuario ya existe, seleccione otro");
            fc.addMessage("form:usr", fm);
        }
    }
    
    public void cambiarClaveCont(){
        asignarClave = true;
    }


    /**
     * @return the contratista
     */
    public Contratista getContratista() {
        return contratista;
    }

    /**
     * @param contratista the contratista to set
     */
    public void setContratista(Contratista contratista) {
        this.contratista = contratista;
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
     * @return the permitirAddEmpleados
     */
    public boolean isPermitirAddEmpleados() {
        return permitirAddEmpleados;
    }

    /**
     * @param permitirAddEmpleados the permitirAddEmpleados to set
     */
    public void setPermitirAddEmpleados(boolean permitirAddEmpleados) {
        this.permitirAddEmpleados = permitirAddEmpleados;
    }


    /**
     * @return the contratistas
     */
    public List<Contratista> getContratistas() {
        return contratistas;
    }

    /**
     * @param contratistas the contratistas to set
     */
    public void setContratistas(List<Contratista> contratistas) {
        this.contratistas = contratistas;
    }

    /**
     * @return the arps
     */
    public List<SelectItem> getArps() {
        return arps;
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
