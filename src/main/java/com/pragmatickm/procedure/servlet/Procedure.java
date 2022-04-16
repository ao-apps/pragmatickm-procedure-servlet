/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2014, 2015, 2016, 2017, 2020, 2021, 2022  AO Industries, Inc.
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

package com.pragmatickm.procedure.servlet;

import com.aoapps.html.servlet.DocumentEE;
import com.pragmatickm.procedure.renderer.html.ProcedureHtmlRenderer;
import com.semanticcms.core.model.ElementContext;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.pages.CaptureLevel;
import com.semanticcms.core.pages.local.CurrentPage;
import com.semanticcms.core.pages.local.PageContext;
import com.semanticcms.core.renderer.html.PageIndex;
import com.semanticcms.core.servlet.Element;
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
		HttpServletResponse response,
		com.pragmatickm.procedure.model.Procedure element
	) {
		super(
			servletContext,
			request,
			response,
			element
		);
	}

	public Procedure(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response
	) {
		this(
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
		com.pragmatickm.procedure.model.Procedure element,
		String label
	) {
		this(servletContext, request, response, element);
		element.setLabel(label);
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
	public Procedure(com.pragmatickm.procedure.model.Procedure element) {
		this(
			PageContext.getServletContext(),
			PageContext.getRequest(),
			PageContext.getResponse(),
			element
		);
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
	public Procedure(
		com.pragmatickm.procedure.model.Procedure element,
		String label
	) {
		this(element);
		element.setLabel(label);
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

	private Object style;
	public Procedure style(Object style) {
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
		ProcedureHtmlRenderer.writeProcedureTable(
			pageIndex,
			new DocumentEE(servletContext, request, response, out),
			context,
			style,
			element
		);
	}
}
