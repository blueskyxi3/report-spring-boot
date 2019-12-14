package com.citictel.controller;

import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.j2ee.servlets.ImageServlet;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;

@RestController
public class ReportController {

	@Resource
	private DataSource dataSource;

	/**
	 * 转换为pdf展示
	 *
	 * @param reportName
	 * @param parameters
	 * @param response
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws JRException
	 * @throws IOException
	 */
	@GetMapping("/{reportName}")
	public void getReportByParam(@PathVariable("reportName") final String reportName,
			@RequestParam(required = false) Map<String, Object> parameters, HttpServletResponse response)
			throws SQLException, ClassNotFoundException, JRException, IOException {

		parameters = parameters == null ? new HashMap<>() : parameters;
		// 获取文件流
		ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + reportName + ".jasper");
		InputStream jasperStream = resource.getInputStream();

		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource.getConnection());
		// JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null,
		// new JREmptyDataSource());
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "inline;");
		final OutputStream outputStream = response.getOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
	}

	@GetMapping("/xls/{reportName}")
	public void getReportByXls(@PathVariable("reportName") final String reportName,
			@RequestParam(required = false) Map<String, Object> parameters, HttpServletResponse response)
			throws SQLException, ClassNotFoundException, JRException, IOException {

		parameters = parameters == null ? new HashMap<>() : parameters;
		// 获取文件流
		ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + reportName + ".jasper");
		InputStream jasperStream = resource.getInputStream();

		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource.getConnection());

		JRXlsxExporter exporter = new JRXlsxExporter();
		exporter.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRXlsExporterParameter.OUTPUT_FILE_NAME, "./tmp/" + reportName + ".xlsx");
		exporter.exportReport();
		System.out.println("-------------------------");
		
		// String mimeType = request.getServletContext().getMimeType(reportName);
		//指明这是一个下载的respond
		 response.setContentType("application/xlsx");
		// log.info("要下载的文件名:"+filename);
		 response.setHeader("Content-Disposition", 
					"attachment;filename="+URLEncoder.encode(reportName+".xlsx", "UTF-8"));
		 String path = this.getClass().getClassLoader().getResource(".").getPath()+"../../tmp/"+reportName+".xlsx";
    	 //log.info("要下载的文件路径:"+path);
		 System.out.println("下载文件了..."+path);
    	 File file = new File(path);
    	 //如果文件不存在
    	 if(!file.exists()){
    		 System.out.println("file don't exist!");
    	      return ;
    	 }

		FileInputStream in = new FileInputStream(file);
		
		
		// 创建输出流
		OutputStream out = response.getOutputStream();
		// 缓存区
		byte buffer[] = new byte[1024];
		int length = 0;
		System.out.print("下载文件了...");
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
			out.flush();
		}
		// 关闭
		out.close();
		in.close();
		file.delete();

		/*
		 * response.setContentType("application/pdf");
		 * response.setHeader("Content-Disposition", "inline;"); final OutputStream
		 * outputStream = response.getOutputStream();
		 * JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		 */
	}

	@RequestMapping(value = "/html/{reportName}")
	public void html(@PathVariable("reportName") final String reportName, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			/*
			 * File reportFile = new File(
			 * JasperController.class.getClassLoader().getResource("").getPath()+
			 * "jasper/demo1.jasper"); if (!reportFile.exists()) throw new
			 * JRRuntimeException("File WebappReport.jasper not found. The report design must be compiled first."
			 * );
			 */
			ClassPathResource resource = new ClassPathResource("jaspers" + File.separator + reportName + ".jasper");
			InputStream jasperStream = resource.getInputStream();
			JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("ReportTitle", "Address Report");
			// parameters.put("BaseDir", reportFile.getParentFile());

			JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters,
					dataSource.getConnection());
			request.getSession().setAttribute(ImageServlet.DEFAULT_JASPER_PRINT_SESSION_ATTRIBUTE, jasperPrint);

			HtmlExporter exporter = new HtmlExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(out);
			output.setImageHandler(new WebHtmlResourceHandler("../jasper/image?image={0}"));
			exporter.setExporterOutput(output);

			exporter.exportReport();
		} catch (Exception e) {
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
}
