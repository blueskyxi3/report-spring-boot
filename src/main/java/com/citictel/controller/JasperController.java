package com.citictel.controller;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.util.FileBufferedOutputStream;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.*;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;

/**
 * Created by Jason on 2017/4/15.
 */
@Controller
@RequestMapping("/jasper")
public class JasperController {

	@Resource
	private DataSource dataSource;

    @RequestMapping(value = "/html")
    public void html(HttpServletRequest request,
                     HttpServletResponse response){
        try
        {
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
			/*
			 * File reportFile = new File(
			 * JasperController.class.getClassLoader().getResource("").getPath()+
			 * "jasper/demo1.jasper"); if (!reportFile.exists()) throw new
			 * JRRuntimeException("File WebappReport.jasper not found. The report design must be compiled first."
			 * );
			 */
        	ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + "idd_daily_check1" + ".jasper");
    		InputStream jasperStream = resource.getInputStream();
    		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
   
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("ReportTitle", "Address Report");
          //  parameters.put("BaseDir", reportFile.getParentFile());

            JasperPrint jasperPrint =
                    JasperFillManager.fillReport(
                            jasperReport,
                            parameters,
                            dataSource.getConnection()
                    );
            request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(out);
            output.setImageHandler(new WebHtmlResourceHandler("../jasper/image?image={0}"));
            exporter.setExporterOutput(output);

            exporter.exportReport();
        }
        catch (Exception e)
        {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>JasperReports - Web Application Sample</title>");
            out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../stylesheet.css\" title=\"Style\">");
            out.println("</head>");

            out.println("<body bgcolor=\"white\">");

            out.println("<span class=\"bnew\">JasperReports encountered this error :</span>");
            out.println("<pre>");

            e.printStackTrace(out);

            out.println("</pre>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    @RequestMapping(value = "/pdf")
    public void pdf(HttpServletRequest request,
                     HttpServletResponse response) throws ServletException, SQLException, IOException, JRException {
        File reportFile = new File(
                JasperController.class.getClassLoader().getResource("").getPath()+"jasper/demo1.jasper");
        if (!reportFile.exists())
            throw new JRRuntimeException("File WebappReport.jasper not found. The report design must be compiled first.");

        JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ReportTitle", "Address Report");
        parameters.put("BaseDir", reportFile.getParentFile());

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource.getConnection()
                );
        request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
        FileBufferedOutputStream fbos = new FileBufferedOutputStream();
        JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fbos));
        try
        {
            exporter.exportReport();
            fbos.close();

            if (fbos.size() > 0)
            {
                response.setContentType("application/pdf");
                response.setContentLength(fbos.size());
                ServletOutputStream outputStream = response.getOutputStream();

                try
                {
                    fbos.writeData(outputStream);
                    fbos.dispose();
                    outputStream.flush();
                }
                finally
                {
                    if (outputStream != null)
                    {
                        try
                        {
                            outputStream.close();
                        }
                        catch (IOException ex)
                        {
                        }
                    }
                }
            }
        }
        catch (JRException e)
        {
            throw new ServletException(e);
        }
        finally
        {
            fbos.close();
            fbos.dispose();
        }

    }

    @RequestMapping(value = "/word")
    public void word(HttpServletRequest request,
                    HttpServletResponse response) throws ServletException, SQLException, IOException, JRException {
		/*
		 * File reportFile = new File(
		 * JasperController.class.getClassLoader().getResource("").getPath()+
		 * "jasper/demo1.jasper"); if (!reportFile.exists()) throw new
		 * JRRuntimeException("File WebappReport.jasper not found. The report design must be compiled first."
		 * );
		 * 
		 * JasperReport jasperReport = (JasperReport)
		 * JRLoader.loadObjectFromFile(reportFile.getPath());
		 */
    	ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + "demo1" + ".jasper");
		InputStream jasperStream = resource.getInputStream();
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ReportTitle", "Address Report");
       // parameters.put("BaseDir", reportFile.getParentFile());

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource.getConnection()
                );
        request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
        List<JasperPrint> jasperPrintList = new ArrayList<>();
        jasperPrintList.add(jasperPrint);
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "inline; filename=\"file.docx\"");

        JRDocxExporter exporter = new JRDocxExporter(DefaultJasperReportsContext.getInstance());
        exporter.setExporterInput(SimpleExporterInput.getInstance(jasperPrintList));
        exporter.setConfiguration(new SimpleDocxExporterConfiguration());
        OutputStream outputStream = response.getOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        try
        {
            exporter.exportReport();
        }
        catch (JRException e)
        {
            throw new ServletException(e);
        }
        finally
        {
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
    }
    protected  JasperPrint fill(HttpServletRequest request,
                         HttpServletResponse response) throws IOException, JRException, SQLException {

        File reportFile = new File(
                JasperController.class.getClassLoader().getResource("").getPath()+"jasper/demo1.jasper");
        if (!reportFile.exists())
            throw new JRRuntimeException("File WebappReport.jasper not found. The report design must be compiled first.");

        JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(reportFile.getPath());

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ReportTitle", "Address Report");
        parameters.put("BaseDir", reportFile.getParentFile());

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(
                        jasperReport,
                        parameters,
                        dataSource.getConnection()
                );
        request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);
        return jasperPrint;
    }


}