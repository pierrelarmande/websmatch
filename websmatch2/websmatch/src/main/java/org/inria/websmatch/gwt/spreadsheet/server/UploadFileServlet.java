package org.inria.websmatch.gwt.spreadsheet.server;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.inria.websmatch.utils.L;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/** * Servlet implementation class for Servlet: UploadFileServlet */

public class UploadFileServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 8305367618713715640L;

	private String baseXLSDir;// = getServletContext().getInitParameter("xlsStorageDir");
	//private String baseXLSDir = "/tmp";

	public void init(){
		baseXLSDir = getServletContext().getInitParameter("xlsStorageDir");
	}

	protected void doPost(HttpServletRequest request,
						  HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");

		FileItem uploadItem = getFileItem(request);

		if (uploadItem == null) {
			response.getWriter().write("NO-SCRIPT-DATA");
			return;
		}

		// we write the file in tmp
		File targetFile = new File(baseXLSDir+"/"+uploadItem.getName());
		L.Debug(UploadFileServlet.class.getSimpleName(), targetFile.getAbsolutePath(), true);
		if(targetFile.exists()) targetFile.delete();
		targetFile.createNewFile();
		try {
			uploadItem.write(targetFile);
		} catch (Exception e) {
			L.Error(e.getMessage(), e);
		}

		response.getWriter().write(uploadItem.getName());
	}

	private FileItem getFileItem(HttpServletRequest request) {

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		try {
			List<?> items = upload.parseRequest(request);
			Iterator<?> it = items.iterator();

			while (it.hasNext()) {
				FileItem item = (FileItem) it.next();
				if (!item.isFormField()
						&& "uploadFormElement".equals(item.getFieldName())) {
					return item;
				}
			}
		} catch (FileUploadException e) {
			return null;
		}
		return null;
	}
}
