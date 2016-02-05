package org.inria.websmatch.debug;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Display a list of registered servlets
 */
public class ServletList extends HttpServlet {

    private static final long serialVersionUID = -7256602549310759826L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        PrintWriter writer = resp.getWriter();

        Map<String, ? extends ServletRegistration> registrations = req
                .getServletContext().getServletRegistrations();

        for (String key : registrations.keySet()) {
            ServletRegistration registration = registrations.get(key);
            writer.write("Name: " + registration.getName()+"\n");
            writer.write("Mappings:\n");
            for (String mapping : registration.getMappings()) {
                writer.write("  - "+mapping+"\n");
            }
            writer.write("  --------- \n");
        }

        // of course you can write that to log or console also depending on your
        // requirement.
    }

}