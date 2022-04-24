/*
 * pragmatickm-procedure-servlet - Procedures nested within SemanticCMS pages and elements in a Servlet environment.
 * Copyright (C) 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

package com.pragmatickm.procedure.servlet.impl;

import com.aoapps.html.any.AnyListContent;
import com.aoapps.html.any.AnyPalpableContent;
import com.aoapps.net.URIEncoder;
import static com.aoapps.taglib.AttributeUtils.resolveValue;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.el.ELContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class ProcedureTreeImpl {

  /** Make no instances. */
  private ProcedureTreeImpl() {
    throw new AssertionError();
  }

  // TODO: as traversal
  private static boolean findProcedures(
      ServletContext servletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      Set<PageRef> pagesWithProcedures,
      Page page
  ) throws ServletException, IOException {
    boolean hasProcedure = false;
    for (Element element : page.getElements()) {
      if (element instanceof Procedure) {
        hasProcedure = true;
        break;
      }
    }
    for (ChildRef childRef : page.getChildRefs()) {
      PageRef childPageRef = childRef.getPageRef();
      // Child not in missing book
      if (childPageRef.getBook() != null) {
        Page child = CapturePage.capturePage(servletContext, request, response, childPageRef, CaptureLevel.META);
        if (
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
    if (hasProcedure) {
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
      AnyListContent<?, ?> content,
      PageRef parentPageRef,
      Page page
  ) throws IOException, ServletException {
    final PageRef pageRef = page.getPageRef();
    if (currentNode != null) {
      // Add page links
      currentNode.addPageLink(pageRef);
    }
    List<Procedure> procedures = new ArrayList<>();
    for (Element element : page.getElements()) {
      if (element instanceof Procedure) {
        procedures.add((Procedure) element);
      }
    }

    // Make the main link point to the procedure when:
    //   1) Page has only one procedure
    //   2) The one procedure has same "label" as page "shortTitle"
    boolean mainLinkToProcedure =
        procedures.size() == 1
            && procedures.get(0).getLabel().equals(page.getShortTitle())
    ;

    if (content != null) {
      SemanticCMS semanticCMS = SemanticCMS.getInstance(servletContext);
      Integer index = pageIndex == null ? null : pageIndex.getPageIndex(pageRef);
      StringBuilder href = new StringBuilder();
      if (index != null) {
        href.append('#');
        URIEncoder.encodeURIComponent(
            PageIndex.getRefId(
                index,
                mainLinkToProcedure ? procedures.get(0).getId() : null
            ),
            href
        );
      } else {
        URIEncoder.encodeURI(request.getContextPath(), href);
        URIEncoder.encodeURI(pageRef.getServletPath(), href);
        if (mainLinkToProcedure) {
          href.append('#');
          URIEncoder.encodeURIComponent(procedures.get(0).getId(), href);
        }
      }
      content.li__any(li -> {
        li.a()
            .clazz(mainLinkToProcedure ? semanticCMS.getLinkCssClass(procedures.get(0)) : null)
            .href(response.encodeURL(href.toString()))
            .__(a -> {
              a.text(PageUtils.getShortTitle(parentPageRef, page));
              if (index != null) {
                a.sup__any(sup -> sup
                        .text('[').text(index + 1).text(']')
                );
              }
            });
        if (!mainLinkToProcedure) {
          if (!procedures.isEmpty()) {
            for (Procedure procedure : procedures) {
              href.setLength(0);
              if (index != null) {
                href.append('#');
                URIEncoder.encodeURIComponent(
                    PageIndex.getRefId(
                        index,
                        procedure.getId()
                    ),
                    href
                );
              } else {
                URIEncoder.encodeURI(request.getContextPath(), href);
                URIEncoder.encodeURI(pageRef.getServletPath(), href);
                href.append('#');
                URIEncoder.encodeURIComponent(procedure.getId(), href);
              }
              li.div__any(div -> div
                      .a()
                      .clazz(semanticCMS.getLinkCssClass(procedure))
                      .href(response.encodeURL(href.toString()))
                      .__(a -> {
                        a.text(procedure);
                        if (index != null) {
                          a.sup__any(sup -> sup
                                  .text('[').text(index + 1).text(']')
                          );
                        }
                      })
              );
            }
          }
        }
        List<ChildRef> childRefs = NavigationTreeImpl.filterPages(
            page.getChildRefs(),
            pagesWithProcedures
        );
        if (!childRefs.isEmpty()) {
          li.ul__any(ul -> {
            // TODO: traversal
            for (ChildRef childRef : childRefs) {
              PageRef childPageRef = childRef.getPageRef();
              assert childPageRef.getBook() != null
                  : "pagesWithProcedures does not contain anything from missing books";
              Page child = CapturePage.capturePage(servletContext, request, response, childPageRef, CaptureLevel.META);
              writePage(servletContext, request, response, currentNode, pagesWithProcedures, pageIndex, ul, pageRef, child);
            }
          });
        }
      });
    } else {
      List<ChildRef> childRefs = NavigationTreeImpl.filterPages(
          page.getChildRefs(),
          pagesWithProcedures
      );
      if (!childRefs.isEmpty()) {
        // TODO: traversal
        for (ChildRef childRef : childRefs) {
          PageRef childPageRef = childRef.getPageRef();
          assert childPageRef.getBook() != null
              : "pagesWithProcedures does not contain anything from missing books";
          Page child = CapturePage.capturePage(servletContext, request, response, childPageRef, CaptureLevel.META);
          writePage(servletContext, request, response, currentNode, pagesWithProcedures, pageIndex, null, pageRef, child);
        }
      }
    }
  }

  /**
   * @param content  optional, null if no output needs to be written
   */
  public static void writeProcedureTree(
      ServletContext servletContext,
      HttpServletRequest request,
      HttpServletResponse response,
      AnyPalpableContent<?, ?> content,
      Page root
  ) throws ServletException, IOException {
    writeProcedureTree(
        servletContext,
        null,
        request,
        response,
        content,
        root
    );
  }

  /**
   * @param content  optional, null if no output needs to be written
   * @param root  either Page of ValueExpression that returns Page
   */
  public static void writeProcedureTree(
      ServletContext servletContext,
      ELContext elContext,
      HttpServletRequest request,
      HttpServletResponse response,
      AnyPalpableContent<?, ?> content,
      Object root
  ) throws ServletException, IOException {
    // Get the current capture state
    final CaptureLevel captureLevel = CaptureLevel.getCaptureLevel(request);
    if (captureLevel.compareTo(CaptureLevel.META) >= 0) {
      // Evaluate expressions
      Page rootPage = resolveValue(root, Page.class, elContext);

      // Filter by has procedures
      final Set<PageRef> pagesWithProcedures = new HashSet<>();
      findProcedures(servletContext, request, response, pagesWithProcedures, rootPage);

      Node currentNode = CurrentNode.getCurrentNode(request);
      PageIndex pageIndex = PageIndex.getCurrentPageIndex(request);

      if (content != null) {
        content.ul__any(ul -> writePage(
            servletContext,
            request,
            response,
            currentNode,
            pagesWithProcedures,
            pageIndex,
            ul,
            null,
            rootPage
        ));
      } else {
        writePage(
            servletContext,
            request,
            response,
            currentNode,
            pagesWithProcedures,
            pageIndex,
            null,
            null,
            rootPage
        );
      }
    }
  }
}
