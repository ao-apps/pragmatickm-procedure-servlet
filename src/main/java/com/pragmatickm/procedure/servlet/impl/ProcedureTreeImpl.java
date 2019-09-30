/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2014, 2015, 2016, 2019  AO Industries, Inc.
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
package com.pragmatickm.procedure.servlet.impl;

import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute;
import static com.aoindustries.encoding.TextInXhtmlAttributeEncoder.textInXhtmlAttributeEncoder;
import static com.aoindustries.encoding.TextInXhtmlEncoder.encodeTextInXhtml;
import com.aoindustries.servlet.ServletUtil;
import com.aoindustries.servlet.URIComponent;
import static com.aoindustries.taglib.AttributeUtils.resolveValue;
import com.pragmatickm.procedure.model.Procedure;
import com.semanticcms.core.model.ChildRef;
import com.semanticcms.core.model.Element;
import com.semanticcms.core.model.Node;
import com.semanticcms.core.model.Page;
import com.semanticcms.core.model.PageRef;
import com.semanticcms.core.servlet.CaptureLevel;
import com.semanticcms.core.servlet.CapturePage;
import com.semanticcms.core.servlet.CurrentNode;
import com.semanticcms.core.servlet.PageIndex;
import com.semanticcms.core.servlet.PageUtils;
import com.semanticcms.core.servlet.SemanticCMS;
import com.semanticcms.core.servlet.impl.NavigationTreeImpl;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.el.ELContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final public class ProcedureTreeImpl {

	// TODO: as traversal
	private static boolean findProcedures(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response,
		Set<PageRef> pagesWithProcedures,
		Page page
	) throws ServletException, IOException {
		boolean hasProcedure = false;
		for(Element element : page.getElements()) {
			if(element instanceof Procedure) {
				hasProcedure = true;
				break;
			}
		}
		for(ChildRef childRef : page.getChildRefs()) {
			PageRef childPageRef = childRef.getPageRef();
			// Child not in missing book
			if(childPageRef.getBook() != null) {
				Page child = CapturePage.capturePage(servletContext, request, response, childPageRef, CaptureLevel.META);
				if(
					findProcedures(
						servletContext,
						request,
						response,
						pagesWithProcedures,
						child
					)
				) {
					hasProcedure = true;
				}
			}
		}
		if(hasProcedure) {
			pagesWithProcedures.add(page.getPageRef());
		}
		return hasProcedure;
	}

	private static void writePage(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response,
		Node currentNode,
		Set<PageRef> pagesWithProcedures,
		PageIndex pageIndex,
		Writer out,
		PageRef parentPageRef,
		Page page
	) throws IOException, ServletException {
		final PageRef pageRef = page.getPageRef();
		if(currentNode != null) {
			// Add page links
			currentNode.addPageLink(pageRef);
		}
		List<Procedure> procedures = new ArrayList<>();
		for(Element element : page.getElements()) {
			if(element instanceof Procedure) procedures.add((Procedure)element);
		}

		// Make the main link point to the procedure when:
		//   1) Page has only one procedure
		//   2) The one procedure has same "label" as page "shortTitle"
		boolean mainLinkToProcedure =
			procedures.size()==1
			&& procedures.get(0).getLabel().equals(page.getShortTitle())
		;

		if(out!=null) {
			SemanticCMS semanticCMS = SemanticCMS.getInstance(servletContext);
			out.write("<li><a");
			if(mainLinkToProcedure) {
				String linkCssClass = semanticCMS.getLinkCssClass(procedures.get(0));
				if(linkCssClass != null) {
					out.write(" class=\"");
					encodeTextInXhtmlAttribute(linkCssClass, out);
					out.write('"');
				}
			}
			out.write(" href=\"");
			Integer index = pageIndex==null ? null : pageIndex.getPageIndex(pageRef);
			if(index != null) {
				out.write('#');
				URIComponent.FRAGMENT.encode(
					PageIndex.getRefId(
						index,
						mainLinkToProcedure ? procedures.get(0).getId() : null
					),
					response,
					out,
					textInXhtmlAttributeEncoder
				);
			} else {
				encodeTextInXhtmlAttribute(
					response.encodeURL(
						ServletUtil.encodeURI(
							request.getContextPath() + pageRef.getServletPath(),
							response
						)
					),
					out
				);
				if(mainLinkToProcedure) {
					encodeTextInXhtmlAttribute('#', out);
					URIComponent.FRAGMENT.encode(procedures.get(0).getId(), response, out, textInXhtmlAttributeEncoder);
				}
			}
			out.write("\">");
			encodeTextInXhtml(PageUtils.getShortTitle(parentPageRef, page), out);
			if(index != null) {
				out.write("<sup>[");
				encodeTextInXhtml(Integer.toString(index+1), out);
				out.write("]</sup>");
			}
			out.write("</a>");
			if(!mainLinkToProcedure) {
				if(!procedures.isEmpty()) {
					for(Procedure procedure : procedures) {
						out.write("\n<div><a");
						String linkCssClass = semanticCMS.getLinkCssClass(procedure);
						if(linkCssClass != null) {
							out.write(" class=\"");
							encodeTextInXhtmlAttribute(linkCssClass, out);
							out.write('"');
						}
						out.write(" href=\"");
						if(index != null) {
							out.write('#');
							URIComponent.FRAGMENT.encode(
								PageIndex.getRefId(
									index,
									procedure.getId()
								),
								response,
								out,
								textInXhtmlAttributeEncoder
							);
						} else {
							encodeTextInXhtmlAttribute(
								response.encodeURL(
									ServletUtil.encodeURI(
										request.getContextPath() + pageRef.getServletPath(),
										response
									)
								),
								out
							);
							// TODO: Include all anchors inside response.encodeURL
							encodeTextInXhtmlAttribute('#', out);
							URIComponent.FRAGMENT.encode(procedure.getId(), response, out, textInXhtmlAttributeEncoder);
						}
						out.write("\">");
						encodeTextInXhtml(procedure.getLabel(), out);
						if(index != null) {
							out.write("<sup>[");
							encodeTextInXhtml(Integer.toString(index+1), out);
							out.write("]</sup>");
						}
						out.write("</a></div>");
					}
				}
			}
		}
		List<ChildRef> childRefs = NavigationTreeImpl.filterPages(
			page.getChildRefs(),
			pagesWithProcedures
		);
		if(!childRefs.isEmpty()) {
			if(out!=null) {
				out.write('\n');
				out.write("<ul>\n");
			}
			// TODO: traversal
			for(ChildRef childRef : childRefs) {
				PageRef childPageRef = childRef.getPageRef();
				assert childPageRef.getBook() != null : "pagesWithProcedures does not contain anything from missing books";
				Page child = CapturePage.capturePage(servletContext, request, response, childPageRef, CaptureLevel.META);
				writePage(servletContext, request, response, currentNode, pagesWithProcedures, pageIndex, out, pageRef, child);
			}
			if(out!=null) out.write("</ul>\n");
		}
		if(out!=null) out.write("</li>\n");
	}

	/**
	 * @param out  optional, null if no output needs to be written
	 */
	public static void writeProcedureTree(
		ServletContext servletContext,
		HttpServletRequest request,
		HttpServletResponse response,
		Writer out,
		Page root
	) throws ServletException, IOException {
		writeProcedureTree(
			servletContext,
			null,
			request,
			response,
			out,
			root
		);
	}

	/**
	 * @param out  optional, null if no output needs to be written
	 * @param root  either Page of ValueExpression that returns Page
	 */
	public static void writeProcedureTree(
		ServletContext servletContext,
		ELContext elContext,
		HttpServletRequest request,
		HttpServletResponse response,
		Writer out,
		Object root
	) throws ServletException, IOException {
		// Get the current capture state
		final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
		if(captureLevel.compareTo(CaptureLevel.META) >= 0) {
			// Evaluate expressions
			Page rootPage = resolveValue(root, Page.class, elContext);

			// Filter by has procedures
			final Set<PageRef> pagesWithProcedures = new HashSet<>();
			findProcedures(servletContext, request, response, pagesWithProcedures, rootPage);

			if(out != null) out.write("<ul>\n");
			writePage(
				servletContext,
				request,
				response,
				CurrentNode.getCurrentNode(request),
				pagesWithProcedures,
				PageIndex.getCurrentPageIndex(request),
				out,
				null,
				rootPage
			);
			if(out != null) out.write("</ul>\n");
		}
	}

	/**
	 * Make no instances.
	 */
	private ProcedureTreeImpl() {
	}
}
