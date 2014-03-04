package com.vbrothers.common.managed;


import com.vi.comun.exceptions.LlaveDuplicadaException;
import com.vi.comun.services.ParamsServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vi.comun.dominio.Parametro;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;

/**
 * @author Jerson Viveros
 */
@ManagedBean(name="paramsController")
@SessionScoped
public class ParamsController {
    @EJB
    ParamsServicesLocal paramService;
    private Parametro parametro;
    private List<Parametro> parametros;
    private Map contexto;
    ServiceLocator locator;

    @PostConstruct
    public void init(){
        locator = ServiceLocator.getInstance();
        setParametro(new Parametro());
        setParametros(paramService.findAll());
        setContexto(locator.getDataForCombo(ServiceLocator.PARAMETROS));
    }

    public String create(){
        try {
            paramService.edit(parametro);
            locator.restartCache();
            FacesUtil.addMessage(FacesUtil.INFO, "Parámetro guardado con exito!");
            init();
        } catch (LlaveDuplicadaException e) {
            FacesUtil.addMessage(FacesUtil.ERROR, e.getMessage());
        }catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al guardar el parametro");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public void borrar(ActionEvent event){
        try {
            Parametro r  = (Parametro) event.getComponent().getAttributes().get("parametroCambiar");
            paramService.remove(r);
            parametros.remove(r);
            FacesUtil.addMessage(FacesUtil.INFO, "Parametro borrado con exito!");
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al borrar el Parametro");
            Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

    }

    public void actualizar(ActionEvent event){
        Parametro r  = (Parametro) event.getComponent().getAttributes().get("parametroCambiar");
        this.parametro = r;
    }

    public String recargarContexto(){
        locator.restartCache();
        contexto = locator.getDataForCombo(ServiceLocator.PARAMETROS);
        return null;
    }

    /**
     * @return the parametro
     */
    public Parametro getParametro() {
        return parametro;
    }

    /**
     * @param parametro the parametro to set
     */
    public void setParametro(Parametro parametro) {
        this.parametro = parametro;
    }

    /**
     * @return the parametros
     */
    public List<Parametro> getParametros() {
        return parametros;
    }

    /**
     * @param parametros the parametros to set
     */
    public void setParametros(List<Parametro> parametros) {
        this.parametros = parametros;
    }

    /**
     * @return the contexto
     */
    public Map getContexto() {
        return contexto;
    }

    /**
     * @param contexto the contexto to set
     */
    public void setContexto(Map contexto) {
        this.contexto = contexto;
    }

}
