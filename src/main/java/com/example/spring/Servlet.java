package com.example.spring;


import com.vaadin.server.VaadinServletConfiguration;
import com.vaadin.shared.ui.Transport;
import com.vaadin.spring.SpringServlet;
import com.vaadin.ui.Push;
import com.vaadin.ui.UI;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;


@ServletComponentScan
@WebServlet(urlPatterns = "/*", name = "UIServlet", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = Servlet.PushUI.class)
public class Servlet extends SpringServlet {

    public Servlet(ApplicationContext context) {
        super(context);
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    @Push(transport = Transport.LONG_POLLING)
    public static class PushUI extends UI {

    }
}