/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2021, 2022, 2025, 2026  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of pragmatickm-procedure-servlet.
 *
 * pragmatickm-procedure-servlet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pragmatickm-procedure-servlet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pragmatickm-procedure-servlet.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.pragmatickm.procedure.servlet.book;

import com.aoapps.net.URIParametersUtils;
import com.aoapps.servlet.http.HttpServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Performs redirects for conversion to modular apidocs.
 */
@WebServlet("/procedure/servlet/apidocs/com/*")
public class ModularApidocsRedirect extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    HttpServletUtil.sendRedirect(
        HttpServletResponse.SC_MOVED_PERMANENTLY, req, resp,
        "/procedure/servlet/apidocs/com.pragmatickm.procedure.servlet/com" + Objects.toString(req.getPathInfo(), ""),
        URIParametersUtils.of(req.getQueryString()), true, false
    );
  }
}
