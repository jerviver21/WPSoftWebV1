package com.vbrothers.common.managed;

import com.vbrothers.common.dominio.ParametrosReporte;
import com.vbrothers.common.dominio.Reporte;
import com.vbrothers.common.dto.ResultReporteDTO;
import com.vbrothers.common.exceptions.ParametroException;
import com.vbrothers.common.services.ReporteServicesLocal;
import com.vbrothers.locator.ServiceLocator;
import com.vbrothers.usuarios.managed.SessionController;
import com.vbrothers.util.FacesUtil;
import com.vbrothers.util.Log;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * @author Jerson Viveros Aguirre
 */

@ManagedBean(name="reportesController")
@SessionScoped
public class ReportesController {

    //Tipos de DATA, deben encontrarse en la tabla data
    final int TIPO_ID = 1;

    //Tipos de parametros de reporte, deben estar en la tabla tipo_parametro_reporte
    final int TEXT = 2;
    final int NUMERIC = 1;
    final int DATE = 3;
    final int TIME = 4;
    final int DATA = 5;

    

    //Tipos de proceso, deben estar en la tabla procesos_reportes y hacen referencia en realidad a la página desde donde se llamen (Esto para diferenciar reportes, de archivos y otros)
    private final int REPORTE = 1;
    private final int ARCHIVO = 2;
    private int proceso;

    @EJB
    ReporteServicesLocal reporteService;
    
    private int codReporte;
    private Reporte reporte;
    private List<SelectItem> reportes;

    //Panel en donde se van a guardar los parámetros de entrada para el reporte
    private HtmlPanelGrid contenedor;

    //Mapa para controlar los campos de los parámetros que contiene el reporte seleccionado
    Map<ParametrosReporte, Object> parametros;

    //Para permitir la descarga de archivos
    private String rutaArchivo;
    private StreamedContent file;
    private boolean renderDownload = false;
    ServiceLocator locator;



    @PostConstruct
    public void init(){
        SessionController sessionControler = (SessionController)FacesUtil.getManagedBean("#{sessionController}");
        locator = ServiceLocator.getInstance();
        if(FacesUtil.getPaginaRequest().matches(".*reportes.xhtml.*")){
            proceso = REPORTE;
        }
        setReportes(FacesUtil.getSelectsItem(reporteService.getReportesByProcesoAndRol(proceso, sessionControler.getRoles())));
    }

    public ReportesController() {
    }

    public void desplegarParametros(ValueChangeEvent event) {
        List<UIComponent> componentes = contenedor.getChildren();
        componentes.clear();
        reporte = reporteService.find((Integer) event.getNewValue());
        parametros = new HashMap<ParametrosReporte, Object>();
        if(reporte == null){
            FacesUtil.addMessage(FacesUtil.ERROR, "El reporte no existe!!");
            return;
        }
        for(ParametrosReporte parametro : reporte.getParametrosReporte()){
            int tipo = parametro.getTipo().getId();
            HtmlOutputLabel label = new HtmlOutputLabel();
            label.setId("E"+parametro.getNombre()+""+parametro.getId());
            label.setValue(parametro.getEtiqueta());
            componentes.add(label);
            //De acuerdo a los parámetros que contenga el reporte se despliegan en la interfaz los campos correspondientes
            switch (tipo) {
                case DATE:
                    org.primefaces.component.calendar.Calendar fecha = new org.primefaces.component.calendar.Calendar();
                    fecha.setId(parametro.getNombre()+""+parametro.getId());
                    fecha.setValue(null);
                    fecha.setPattern("yyyy-MM-dd");
                    componentes.add(fecha);
                    parametros.put(parametro, fecha);
                    break;
                case DATA:
                    HtmlSelectOneMenu menu = new HtmlSelectOneMenu();
                    UISelectItems items = new UISelectItems();
                    items.setValue(FacesUtil.getSelectsItem(locator.getDataForCombo(parametro.getData().getId())));
                    menu.setId(parametro.getNombre()+""+parametro.getId());
                    menu.getChildren().add(items);
                    componentes.add(menu);
                    parametros.put(parametro, menu);
                    break;
                case TIME:
                    label.setValue(parametro.getEtiqueta()+" (HH:mm)");
                    HtmlInputText inputTime = new HtmlInputText();
                    inputTime.setId(parametro.getNombre()+""+parametro.getId());
                    inputTime.setValue("");
                    componentes.add(inputTime);
                    parametros.put(parametro, inputTime);
                    break;
                default:
                    HtmlInputText inputTxt = new HtmlInputText();
                    inputTxt.setId(parametro.getNombre()+""+parametro.getId());
                    inputTxt.setValue("");
                    componentes.add(inputTxt);
                    parametros.put(parametro, inputTxt);//Ponemos cada parametro yu su valor en el mapa, para el momento de la generación
                    break;
            }
        }
    }

    public void generar() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            if(parametros == null){
                return;
            }
            //Lectura de los parámetros de la página, se almacenan en un mapa para poder generar el jasper
            Set<ParametrosReporte> pks = parametros.keySet();
            for (ParametrosReporte parametro : pks) {
                String nombreParametro = parametro.getNombre();
                int tipo = parametro.getTipo().getId();
                Object param = "";
                switch (tipo) {
                    case DATA:
                        HtmlSelectOneMenu combo =(HtmlSelectOneMenu) parametros.get(parametro);
                        param = Integer.parseInt(combo.getValue().toString());
                        break;
                    case DATE:
                        org.primefaces.component.calendar.Calendar campoFecha =
                                (org.primefaces.component.calendar.Calendar) parametros.get(parametro);
                        param = format.format((Date)campoFecha.getValue());
                        break;
                    case NUMERIC:
                        HtmlInputText campoNum = (HtmlInputText) parametros.get(parametro);
                        if(!campoNum.getValue().toString().matches("\\d+")){
                            FacesUtil.addMessage(FacesUtil.ERROR, "El campo: "+parametro.getEtiqueta()+" debe ser numérico");
                            return;
                        }
                        param = Long.parseLong(campoNum.getValue().toString());
                        break;
                    default:
                        HtmlInputText input = (HtmlInputText) parametros.get(parametro);
                        param = input.getValue().toString();
                        break;
                }
                System.out.println(parametro.getNombre()+" : "+param);
                params.put(nombreParametro, param);
            }
            ResultReporteDTO resultado = reporteService.generarReporte(reporte, params);
            rutaArchivo = resultado.getRutaZip();
            renderDownload = true;
        } catch (ParametroException p){
            Log.getLogger().log(Level.SEVERE, p.getMessage(), p);
            FacesUtil.addMessage(FacesUtil.ERROR, p.getMessage());
        } catch (Exception e) {
            Log.getLogger().log(Level.SEVERE, "Error  al tratar de generar reporte", e);
            FacesUtil.addMessage(FacesUtil.ERROR, "Error al tratar de generar reporte");
        }
        List<UIComponent> componentes = contenedor.getChildren();
        componentes.clear();
        codReporte = -1;
        
    }

    public void descargar(ActionEvent evt)throws Exception{
        if(rutaArchivo == null){
            return;
        }
        FileInputStream stream = new FileInputStream(rutaArchivo);
        file = new DefaultStreamedContent(stream, "application/zip", reporte.getNombre().replaceAll("\\s", "")+".zip");
        renderDownload = false;
    }


    /**
     * @return the codReporte
     */
    public int getCodReporte() {
        return codReporte;
    }

    /**
     * @param codReporte the codReporte to set
     */
    public void setCodReporte(int codReporte) {
        this.codReporte = codReporte;
    }

    /**
     * @return the reportes
     */
    public List<SelectItem> getReportes() {
        return reportes;
    }

    /**
     * @param reportes the reportes to set
     */
    public void setReportes(List<SelectItem> reportes) {
        this.reportes = reportes;
    }

    /**
     * @return the contenedor
     */
    public HtmlPanelGrid getContenedor() {
        return contenedor;
    }

    /**
     * @param contenedor the contenedor to set
     */
    public void setContenedor(HtmlPanelGrid contenedor) {
        this.contenedor = contenedor;
    }

    /**
     * @return the file
     */
    public StreamedContent getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(StreamedContent file) {
        this.file = file;
    }

    /**
     * @return the renderDownload
     */
    public boolean isRenderDownload() {
        return renderDownload;
    }

    /**
     * @param renderDownload the renderDownload to set
     */
    public void setRenderDownload(boolean renderDownload) {
        this.renderDownload = renderDownload;
    }

    

    /**
     * @return the REPORTE
     */
    public int getREPORTE() {
        return REPORTE;
    }

    /**
     * @return the ARCHIVO
     */
    public int getARCHIVO() {
        return ARCHIVO;
    }

}
