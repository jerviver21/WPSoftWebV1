package com.vbrothers.common.managed;

import com.vi.comun.services.CommonServicesLocal;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped; 

/**
 * @author Jerson Viveros
 */

@ManagedBean(name="bdController")
@SessionScoped
public class BDController {
    @EJB
    CommonServicesLocal commonServices;

    private String sentencia;
    private List<String> resultado;
    private boolean isSelect = false;


    public String ejecutar(){
        try {
            int colsUpdate = 0;
            if(isSelect){
                setResultado(commonServices.executeQuery(sentencia));
            }else{
                colsUpdate = commonServices.executeUpdate(sentencia);
                FacesUtil.addMessage(FacesUtil.INFO, colsUpdate+" Filas afectadas!!");
            }
        } catch (Exception e) {
            FacesUtil.addMessage(FacesUtil.ERROR, " Error al ejecutar la sentencia");
            Log.getLogger().log(Level.SEVERE, " Error al ejecutar la sentencia \n -"+sentencia+"-");
        }
        return null;
    }

    /**
     * @return the sentencia
     */
    public String getSentencia() {
        return sentencia;
    }

    /**
     * @param sentencia the sentencia to set
     */
    public void setSentencia(String sentencia) {
        this.sentencia = sentencia;
    }

    /**
     * @return the isSelect
     */
    public boolean isIsSelect() {
        return isSelect;
    }

    /**
     * @param isSelect the isSelect to set
     */
    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    /**
     * @return the resultado
     */
    public List<String> getResultado() {
        return resultado;
    }

    /**
     * @param resultado the resultado to set
     */
    public void setResultado(List<String> resultado) {
        this.resultado = resultado;
    }

}
