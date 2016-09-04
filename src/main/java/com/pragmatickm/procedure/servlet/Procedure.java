/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2014, 2015, 2016  AO Industries, Inc.
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
 * along with pragmatickm-procedure-servlet.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pragmatickm.procedure.servlet;

import com.pragmatickm.procedure.servlet.impl.ProcedureImpl;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.CurrentPage;
import com.semanticcms.core.servlet.Element;
import com.semanticcms.core.servlet.PageContext;
import com.semanticcms.core.servlet.PageIndex;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.SkipPageException;

public class Procedure extends Element<com.pragmatickm.procedure.model.Procedure> {

	public Procedure(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		super(
			servletContext,
			request,
			response,
			new com.pragmatickm.procedure.model.Procedure()
		);
	}

	public Procedure(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response,
		String label
	) {
		this(servletContext, request, response);
		element.setLabel(label);
	}

	/**
	 * Creates a new procedure in the current page context.
	 *
	 * @see  PageContext
	 */
	public Procedure() {
		this(
			PageContext.getServletContext(),
			PageContext.getRequest(),
			PageContext.getResponse()
		);
	}

	/**
	 * @see  #Procedure()
	 */
	public Procedure(String label) {
		this();
		element.setLabel(label);
	}

	@Override
	public Procedure id(String id) {
		super.id(id);
		return this;
	}

	private String style;
	public Procedure style(String style) {
		this.style = style;
		return this;
	}

	public Procedure label(String label) {
		element.setLabel(label);
		return this;
	}

	private PageIndex pageIndex;
	@Override
	protected void doBody(CaptureLevel captureLevel, Body<? super com.pragmatickm.procedure.model.Procedure> body) throws ServletException, IOException, SkipPageException {
		final Page currentPage = CurrentPage.getCurrentPage(request);
		if(currentPage == null) throw new ServletException("Procedure must be nested inside a Page.");
		pageIndex = PageIndex.getCurrentPageIndex(request);
		// Label defaults to page short title
		if(element.getLabel() == null) {
			element.setLabel(currentPage.getShortTitle());
		}
		super.doBody(captureLevel, body);
	}

	@Override
	public void writeTo(Writer out, ElementContext context) throws IOException, ServletException, SkipPageException {
		ProcedureImpl.writeProcedureTable(pageIndex, out, context, style, element);
	}
}