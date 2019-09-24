package org.gluu.oxtrust.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@WebServlet(urlPatterns = "/servlet/indicator")
public class BusyIndicatorImageServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5281033529284850972L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/gif");
		response.setDateHeader("Expires", new Date().getTime() + 1000L * 7200);
		try (InputStream in = getServletContext().getResourceAsStream("/WEB-INF/static/images/indicator.gif");
				OutputStream out = response.getOutputStream()) {
			IOUtils.copy(in, out);
		} catch (IOException e) {
		}
	}

}
